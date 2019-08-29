/*
 * #%L
 * MountyHall DLA Notifier
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2012 - 2014 Zoumbox.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package org.zoumbox.mh_dla_notifier;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.zoumbox.mh_dla_notifier.profile.MissingLoginPasswordException;
import org.zoumbox.mh_dla_notifier.profile.ProfileProxy;
import org.zoumbox.mh_dla_notifier.profile.v2.ProfileProxyV2;
import org.zoumbox.mh_dla_notifier.troll.Troll;
import org.zoumbox.mh_dla_notifier.troll.Trolls;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Classe appelée lorsqu'un réveil de l'application survient (approche d'une DLA ou vérification intermédiaire)
 *
 * @author Arno <arno@zoumbox.org>
 */
public class Receiver extends BroadcastReceiver {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + Receiver.class.getSimpleName();

    public static final long[] VIBRATE_PATTERN = new long[]{100, 200, 100, 200, 100, 550};
    public static final long[] NO_VIBRATION = new long[]{100};

    private ProfileProxy profileProxy;

    public ProfileProxy getProfileProxy() {
        if (profileProxy == null) {
            profileProxy = new ProfileProxyV2();
        }
        return profileProxy;
    }

    protected boolean needsNotificationAccordingToPA(PreferencesHolder preferences, int pa) {
        boolean result = pa > 0 || preferences.notifyWithoutPA;
        return result;
    }

    protected Predicate<Context> justRestarted() {
        Predicate<Context> result = new Predicate<Context>() {
            @Override
            public boolean apply(Context context) {
                // Check if the device just restarted
                long elapsedSinceLastRestartCheck = getProfileProxy().getElapsedSinceLastRestartCheck(context);
                Log.d(TAG, "Elapsed since last restart check: " + elapsedSinceLastRestartCheck + "ms ~= " + (elapsedSinceLastRestartCheck / 60000) + "min");

                long uptime = SystemClock.uptimeMillis();
                Log.d(TAG, "Uptime: " + uptime + "ms ~= " + (uptime / 60000) + "min");

                boolean result = elapsedSinceLastRestartCheck > uptime;
                if (result) {
                    getProfileProxy().restartCheckDone(context);
                }
                Log.d(TAG, "Device restarted since last check: " + result);
                return result;
            }
        };
        return result;
    }

    protected Predicate<Context> shouldUpdateBecauseOfRestart(final String trollId) {
        Predicate<Context> predicate = new Predicate<Context>() {
            @Override
            public boolean apply(Context context) {
                boolean result = false;
                // Check if the device restarted since last update
                Long elapsedSinceLastSuccess = getProfileProxy().getElapsedSinceLastUpdateSuccess(context, trollId);
                if (elapsedSinceLastSuccess != null) {
                    Log.d(TAG, "Elapsed since last update success: " + elapsedSinceLastSuccess + "ms ~= " + (elapsedSinceLastSuccess / 60000) + "min");

                    long uptime = SystemClock.uptimeMillis();
                    Log.d(TAG, "Uptime: " + uptime + "ms ~= " + (uptime / 60000) + "min");

                    result = elapsedSinceLastSuccess > uptime; // Device restarted since last update
                    Log.d(TAG, "shouldUpdateBecauseOfRestart: " + result);
                    result &= elapsedSinceLastSuccess > (1000l * 60l * 60l * 2l); // 2 hours
                    Log.d(TAG, "shouldUpdateBecauseOfRestart (<=2hours): " + result);
                }
                return result;
            }
        };
        return predicate;
    }

    protected Predicate<Context> shouldUpdateBecauseOfNetworkFailure(final String trollId) {
        Predicate<Context> predicate = new Predicate<Context>() {
            @Override
            public boolean apply(Context context) {
                String lastUpdateResult = getProfileProxy().getLastUpdateResult(context, trollId);
                Log.d(TAG, "lastUpdateResult: " + lastUpdateResult);
                boolean result = lastUpdateResult != null && lastUpdateResult.startsWith("NETWORK ERROR");
                Log.d(TAG, "shouldUpdateBecauseOfNetworkFailure: " + result);
                return result;
            }
        };
        return predicate;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String intentAction = intent.getAction();
        Log.d(TAG, String.format("<<< %s#onReceive action=%s", getClass().getName(), intentAction));

        boolean connectivityChanged = ConnectivityManager.CONNECTIVITY_ACTION.equals(intentAction);
        boolean justGotConnection = false;
        if (connectivityChanged) {

            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            justGotConnection = activeNetwork != null && activeNetwork.isConnected();
            Log.d(TAG, "Connectivity change. isConnected=" + justGotConnection);
        }

        if (connectivityChanged && !justGotConnection) {
            Log.d(TAG, "Just lost connectivity, nothing to do");
            return;
        }

        String trollId = intent.getStringExtra(Alarms.EXTRA_TROLL_ID);
        if (Strings.isNullOrEmpty(trollId)) {
            Set<String> trollIds = getProfileProxy().getTrollIds(context);
            if (trollIds.isEmpty()) {
                Log.d(TAG, "No troll registered, exiting...");
                return;
            }
            trollId = trollIds.iterator().next();
            Log.d(TAG, "TrollId not defined, using the fist one: " + trollId);
        }

        if (!getProfileProxy().isPasswordDefined(context, trollId)) {
            Log.d(TAG, "Troll password is not defined, exiting...");
            return;
        }

        // If type is provided, request for an update
        String type = intent.getStringExtra(Alarms.EXTRA_TYPE);
        boolean requestUpdate = !Strings.isNullOrEmpty(type);

        // If device just started, request for wakeups registration
        boolean requestAlarmRegistering = justRestarted().apply(context);

        // Just go the internet connection back. Update will be necessary if
        //  - device restarted since last update and last update in more than 2 hours ago
        //  - last update failed because of network error
        if (!requestUpdate && justGotConnection) { // !requestUpdate because no need to check if update is already requested
            requestUpdate = Predicates.or(
                    shouldUpdateBecauseOfRestart(trollId),
                    shouldUpdateBecauseOfNetworkFailure(trollId)
            ).apply(context);
        }

        Log.d(TAG, String.format("requestUpdate=%b ; requestAlarmRegistering=%b", requestUpdate, requestAlarmRegistering));

        // FIXME AThimel 14/02/14 Remove ASAP
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            Troll troll = getProfileProxy().fetchTrollWithoutUpdate(context, trollId).left();
            if (requestUpdate) {
                // If the current DLA already has no more PA, skip update
                boolean skipUpdate = false;
                if (AlarmType.CURRENT_DLA.name().equals(type)) {
                    skipUpdate = troll.getPa() == 0 && MhDlaNotifierUtils.IS_IN_THE_FUTURE.apply(troll.getDla());
                }
                if (skipUpdate) {
                    trollLoaded(troll, context, false);
                } else {
                    refreshDla(context, trollId);
                }
            } else if (requestAlarmRegistering) {
                trollLoaded(troll, context, false);
            } else {
                Log.d(TAG, "Skip loading Troll");
            }
        } catch (MissingLoginPasswordException mde) {
            Log.w(TAG, "Missing trollId and/or password, exiting...");
        }
    }

    protected void trollLoaded(Troll troll, Context receivedContext, boolean requestUpdate) {

        if (troll != null) {
            PreferencesHolder preferences = PreferencesHolder.load(receivedContext);

            // Re-schedule them (in case it changed)
            String trollId = troll.getNumero();
            if (!Strings.isNullOrEmpty(trollId)) {
                try {
                    Map<AlarmType, Date> alarms = Alarms.getAlarms(receivedContext, getProfileProxy(), trollId);

                    Date now = new Date();

                    Date beforeCurrentDla = alarms.get(AlarmType.CURRENT_DLA);
                    Date currentDla = troll.getDla();
                    Date beforeNextDla = alarms.get(AlarmType.NEXT_DLA);
                    Date nextDla = Trolls.GET_NEXT_DLA.apply(troll);
                    Date nextDlaActivation = alarms.get(AlarmType.NEXT_DLA_ACTIVATION);

                    if (between(now, beforeCurrentDla, currentDla)) {
                        Log.d(TAG, String.format("Need to notify DLA='%s' about to expire", currentDla));
                        int pa = troll.getPa();
                        boolean willNotify = needsNotificationAccordingToPA(preferences, pa);
                        Log.d(TAG, String.format("PA=%d. Will notify? %b", pa, willNotify));
                        if (willNotify) {
                            notifyCurrentDlaAboutToExpire(receivedContext, currentDla, pa, preferences);
                        }
                    } else {
                        Log.d(TAG, String.format("No need to notify for DLA=%s", currentDla));
                    }

                    if (between(now, nextDlaActivation, beforeNextDla)) {
                        Log.d(TAG, String.format("Need to notify NDLA='%s' not activated", nextDla));
                        notifyNextDlaNotActivated(receivedContext, nextDla, preferences);
                    } else {
                        Log.d(TAG, String.format("No need to notify for NDLA=%s", nextDla));
                    }

                    if (between(now, beforeNextDla, nextDla)) {
                        Log.d(TAG, String.format("Need to notify NDLA='%s' about to expire", nextDla));
                        notifyNextDlaAboutToExpire(receivedContext, nextDla, preferences);
                    } else {
                        Log.d(TAG, String.format("No need to notify for NDLA=%s", nextDla));
                    }

                    if (requestUpdate && troll.getPvVariation() < 0 && preferences.notifyOnPvLoss) {
                        int pvLoss = Math.abs(troll.getPvVariation());
                        Log.d(TAG, String.format("Troll lost %d PV", pvLoss));
                        notifyPvLoss(receivedContext, pvLoss, troll.getPv(), preferences);
                    }

                    checkForWidgetsUpdate(receivedContext, troll);
                } catch (MissingLoginPasswordException e) {
                    e.printStackTrace();
                }

                // Re-schedule them (in case it changed due to update)
                try {
                    Alarms.scheduleAlarms(receivedContext, getProfileProxy(), trollId);
                } catch (MissingLoginPasswordException e) {
                    Log.w(TAG, "Missing trollId and/or password, unable to schedule alarms...");
                }
            }
        }

    }

    protected boolean between(Date date, Date lowerBound, Date upperBound) {
        if (date == null) {
            return false;
        }
        try {
            boolean afterLowerBound = date.compareTo(lowerBound) >= 0;
            boolean beforeUpperBound = date.compareTo(upperBound) <= 0;
            return afterLowerBound && beforeUpperBound;
        } catch (Exception eee) {
            Log.e(TAG, "Unable to compare dates. Date=" + date + " ; lowerBound=" + lowerBound + " ; upperBound=" + upperBound, eee);
            return false;
        }
    }

    protected void refreshDla(Context context, String trollId) {
        Troll troll = null;
        try {
            troll = getProfileProxy().refreshDLA(context, trollId);
        } catch (MissingLoginPasswordException e) {
            Log.w(TAG, "Missing trollId and/or password, unable to refresh DLA...");
        }
        trollLoaded(troll, context, true);
    }

//    protected void refreshDla(Context context, String trollId, boolean requestUpdate) {
//        new RefreshDlaTask(context, requestUpdate).doInBackground(trollId);
//    }
//
//    private class RefreshDlaTask extends AsyncTask<String, Void, Troll> {
//
//        protected Context context;
//        protected boolean requestUpdate;
//
//        private RefreshDlaTask(Context context, boolean requestUpdate) {
//            this.context = context;
//            this.requestUpdate = requestUpdate;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            // nothing to do
//        }
//
//        @Override
//        protected Troll doInBackground(String ... params) {
//            Troll troll = null;
//            try {
//                String trollId = params[0];
//
//                troll = getProfileProxy().refreshDLA(context, trollId);
//            } catch (MhDlaException e) {
//                e.printStackTrace();
//            }
//            return troll;
//        }
//
//        @Override
//        protected void onPostExecute(Troll result) {
//            trollLoaded(result, context, requestUpdate);
//        }
//    }

    protected void checkForWidgetsUpdate(Context context, Troll troll) {

        try {
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);

            ComponentName componentName = new ComponentName(context, HomeScreenWidget.class);
            int[] appWidgetIds = widgetManager.getAppWidgetIds(componentName);

            if (appWidgetIds != null && appWidgetIds.length > 0) {

                // FIXME AThimel 14/02/14 Remove ASAP
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                String dlaText = Trolls.getWidgetDlaTextFunction(context).apply(troll);
                Bitmap blason = MhDlaNotifierUtils.loadBlasonForWidget(troll.getBlason(), context.getCacheDir());

                for (int appWidgetId : appWidgetIds) {

                    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.home_screen_widget);
                    views.setTextViewText(R.id.widgetDla, dlaText);

                    if (blason == null) {
                        views.setImageViewResource(R.id.widgetImage, R.drawable.trarnoll_square_transparent_128);
                    } else {
                        views.setImageViewBitmap(R.id.widgetImage, blason);
                    }

                    // Tell the AppWidgetManager to perform an update on the current app widget
                    widgetManager.updateAppWidget(appWidgetId, views);
                }
            }

        } catch (Exception eee) {
            Log.e(TAG, "Unable to update widget(s)", eee);
        }

    }

    protected Pair<Boolean, Boolean> soundAndVibrate(Context context, PreferencesHolder preferences) {
        switch (preferences.silentNotification) {
            case ALWAYS: { // Toujours silencieux (pas de son, pas de vibration)
                Pair<Boolean, Boolean> result = Pair.of(false, false);
                return result;
            }
            case NEVER: { // Jamais silencieux (son + vibration)
                Pair<Boolean, Boolean> result = Pair.of(true, true);
                return result;
            }
            case BY_NIGHT: { // Ni son, ni vibration entre 23h et 7h
                Calendar now = Calendar.getInstance();
                int hour = now.get(Calendar.HOUR_OF_DAY);
                boolean soundAndVibrate = hour >= 7 && hour < 23;
                Pair<Boolean, Boolean> result = Pair.of(soundAndVibrate, soundAndVibrate);
                return result;
            }
            case WHEN_SILENT: { // Dépend du système
                AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                int ringerMode = am.getRingerMode();
                Pair<Boolean, Boolean> result = Pair.of(
                        ringerMode == AudioManager.RINGER_MODE_NORMAL, // son
                        ringerMode == AudioManager.RINGER_MODE_NORMAL || ringerMode == AudioManager.RINGER_MODE_VIBRATE // vibration
                );
                return result;
            }
            default:
                Log.w(TAG, "Unexpected mode : " + preferences.silentNotification);
                throw new IllegalStateException("Unexpected mode : " + preferences.silentNotification);
        }
    }

    enum NotificationType {
        DLA,
        PV_LOSS
    }

    protected void notifyCurrentDlaAboutToExpire(Context context, Date dla, Integer pa, PreferencesHolder preferences) {
        CharSequence notifTitle = context.getText(R.string.current_dla_expiring_title);
        if (pa != null && pa == 0) {
            notifTitle = context.getText(R.string.current_dla_expiring_title_noPA);
        }
        String format = context.getText(R.string.current_dla_expiring_text).toString();
        CharSequence notifText = String.format(format, MhDlaNotifierUtils.formatHour(dla), pa);

        Pair<Boolean, Boolean> soundAndVibrate = soundAndVibrate(context, preferences);

        displayNotification(context, NotificationType.DLA, notifTitle, notifText, soundAndVibrate);
    }

    protected void notifyNextDlaNotActivated(Context context, Date dla, PreferencesHolder preferences) {
        CharSequence notifTitle = context.getText(R.string.next_dla_not_activated_title);
        String format = context.getText(R.string.next_dla_not_activated_text).toString();
        CharSequence notifText = String.format(format, MhDlaNotifierUtils.formatHour(dla));

        Pair<Boolean, Boolean> soundAndVibrate = soundAndVibrate(context, preferences);

        displayNotification(context, NotificationType.DLA, notifTitle, notifText, soundAndVibrate);
    }

    protected void notifyNextDlaAboutToExpire(Context context, Date dla, PreferencesHolder preferences) {
        CharSequence notifTitle = context.getText(R.string.next_dla_expiring_title);
        String format = context.getText(R.string.next_dla_expiring_text).toString();
        CharSequence notifText = String.format(format, MhDlaNotifierUtils.formatHour(dla));

        Pair<Boolean, Boolean> soundAndVibrate = soundAndVibrate(context, preferences);

        displayNotification(context, NotificationType.DLA, notifTitle, notifText, soundAndVibrate);
    }

    protected void notifyPvLoss(Context context, int pvLoss, int pv, PreferencesHolder preferences) {
        String titleFormat = context.getText(R.string.pv_loss_title).toString();
        CharSequence notifTitle = String.format(titleFormat, pvLoss);
        String messageFormat = context.getText(R.string.pv_remaining_text).toString();
        CharSequence notifText = String.format(messageFormat, pv);

        Pair<Boolean, Boolean> soundAndVibrate = soundAndVibrate(context, preferences);

        displayNotification(context, NotificationType.PV_LOSS, notifTitle, notifText, soundAndVibrate);
    }

    protected NotificationCompat.Builder newNotificationBuilder(Context context, boolean sound, boolean vibrate) {

        NotificationCompat.Builder result;

        // À partir d'Android O (API 26), il est nécessaire de créer un Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            String channelId = String.format("%s-%s-%s", MhDlaNotifierConstants.NOTIFICATION_CHANNEL_ID, sound, vibrate);

            NotificationChannel existingChannel = notificationManager.getNotificationChannel(channelId);
            if (existingChannel != null) {
                Log.w(TAG, "Il y a déjà un channel, on le supprime");
                notificationManager.deleteNotificationChannel(channelId);
            }

            if (notificationManager.getNotificationChannel(channelId) != null) {
                Log.e(TAG, "Le channel n'est pas supprimé");
            }

            NotificationChannel androidChannel = new NotificationChannel(channelId,
                    "MhDlaNotifier", NotificationManager.IMPORTANCE_DEFAULT);

            // Sets whether notifications posted to this channel should display notification lights
            androidChannel.enableLights(true);

            if (!sound) {
                androidChannel.setSound(null, null);
            }

            if (vibrate) {
                // Sets whether notification posted to this channel should vibrate.
                androidChannel.setVibrationPattern(VIBRATE_PATTERN);
            }

            // Sets whether notifications posted to this channel appear on the lockscreen or not
            androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            notificationManager.createNotificationChannel(androidChannel);

            result = new NotificationCompat.Builder(context, channelId);

        } else {

            result = new NotificationCompat.Builder(context);

            if (sound) {
                Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                result.setSound(defaultSound);
            }

            if (vibrate) {
                result.setVibrate(VIBRATE_PATTERN);
            } else {
                result.setVibrate(NO_VIBRATION);
            }
        }

        return result;

    }

    protected void displayNotification(Context context, NotificationType type, CharSequence title, CharSequence text, Pair<Boolean, Boolean> soundAndVibrate) {

        // The PendingIntent to launch our activity if the user selects this notification
        Intent mainActivity = new Intent(context, MainActivity.class);
        mainActivity.putExtra(MainActivity.EXTRA_FROM_NOTIFICATION, true);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, mainActivity, 0);

        Intent playIntent = MhDlaNotifierUtils.GET_PLAY_INTENT.apply(context);
        PendingIntent playPendingIntent = PendingIntent.getActivity(context, 0, playIntent, 0);
        long now = System.currentTimeMillis();

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.trarnoll_square_transparent_128);

        NotificationCompat.Builder builder = newNotificationBuilder(context, soundAndVibrate.left(), soundAndVibrate.right())
                .setOnlyAlertOnce(true)
                .setLights(Color.YELLOW, 1500, 1500)
                .setSmallIcon(R.drawable.trarnoll_square_transparent_128)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setContentText(text)
                .setWhen(now)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setTicker(title)
                .addAction(R.drawable.ic_action_play, context.getText(R.string.play), playPendingIntent);

        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int notifId = type.name().hashCode();
        notificationManager.notify(notifId, notification);
    }

}
