/*
 * #%L
 * MountyHall DLA Notifier
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2012 - 2013 Zoumbox.org
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

    public static final long[] VIBRATE_PATTERNN = new long[] {100, 100, 100, 100, 100, 700};

    private ProfileProxy profileProxy;

    public ProfileProxy getProfileProxy() {
        if (profileProxy == null) {
//            profileProxy = new ProfileProxyV1();
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
                Log.i(TAG, "Elapsed since last restart check: " + elapsedSinceLastRestartCheck + "ms ~= " + (elapsedSinceLastRestartCheck / 60000) + "min");

                long uptime = SystemClock.uptimeMillis();
                Log.i(TAG, "Uptime: " + uptime + "ms ~= " + (uptime / 60000) + "min");

                boolean result = elapsedSinceLastRestartCheck > uptime;
                if (result) {
                    getProfileProxy().restartCheckDone(context);
                }
                Log.i(TAG, "Device restarted since last check: " + result);
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
                    Log.i(TAG, "Elapsed since last update success: " + elapsedSinceLastSuccess + "ms ~= " + (elapsedSinceLastSuccess / 60000) + "min");

                    long upTime = SystemClock.uptimeMillis();
                    Log.i(TAG, "Uptime: " + upTime + "ms ~= " + (upTime / 60000) + "min");

                    result = elapsedSinceLastSuccess > upTime; // Device restarted since last update
                    Log.i(TAG, "shouldUpdateBecauseOfRestart: " + result);
                    result &= elapsedSinceLastSuccess > (1000l * 60l * 60l * 2l); // 2 hours
                    Log.i(TAG, "shouldUpdateBecauseOfRestart (<=2hours): " + result);
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
                Log.i(TAG, "lastUpdateResult: " + lastUpdateResult);
                boolean result = lastUpdateResult != null && lastUpdateResult.startsWith("NETWORK ERROR");
                Log.i(TAG, "shouldUpdateBecauseOfNetworkFailure: " + result);
                return result;
            }
        };
        return predicate;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String intentAction = intent.getAction();
        Log.i(TAG, String.format("<<< %s#onReceive action=%s", getClass().getName(), intentAction));

        boolean connectivityChanged = ConnectivityManager.CONNECTIVITY_ACTION.equals(intentAction);
        boolean justGotConnection = false;
        if (connectivityChanged) {

            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            justGotConnection = activeNetwork != null && activeNetwork.isConnected();
            Log.i(TAG, "Connectivity change. isConnected=" + justGotConnection);
        }

        if (connectivityChanged && !justGotConnection) {
            Log.i(TAG, "Just lost connectivity, nothing to do");
            return;
        }

        String trollId = intent.getStringExtra(Alarms.EXTRA_TROLL_ID);
        if (Strings.isNullOrEmpty(trollId)) {
            Set<String> trollIds = getProfileProxy().getTrollIds(context);
            if (trollIds.isEmpty()) {
                Log.i(TAG, "No troll registered, exiting...");
                return;
            }
            trollId = trollIds.iterator().next();
            Log.i(TAG, "TrollId not defined, using the fist one: " + trollId);
        }

        if (!getProfileProxy().isPasswordDefined(context, trollId)) {
            Log.i(TAG, "Troll password is not defined, exiting...");
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

        Log.i(TAG, String.format("requestUpdate=%b ; requestAlarmRegistering=%b", requestUpdate, requestAlarmRegistering));

        // FIXME AThimel 14/02/14 Remove ASAP
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            if (requestUpdate) {
                refreshDla(context, trollId, requestUpdate);
            } else if (requestAlarmRegistering) {
                Troll troll = getProfileProxy().fetchTrollWithoutUpdate(context, trollId).left();
                trollLoaded(troll, context, requestUpdate);
            } else {
                Log.i(TAG, "Skip loading Troll");
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
            try {
                Map<AlarmType, Date> alarms = Alarms.getAlarms(receivedContext, getProfileProxy(), trollId);

                Date now = new Date();

                Date beforeCurrentDla = alarms.get(AlarmType.CURRENT_DLA);
                Date currentDla = troll.getDla();
                Date beforeNextDla = alarms.get(AlarmType.NEXT_DLA);
                Date nextDla = Trolls.GET_NEXT_DLA.apply(troll);

                if (now.after(beforeCurrentDla) && now.before(currentDla)) {
                    Log.i(TAG, String.format("Need to notify DLA='%s' about to expire", currentDla));
                    int pa = troll.getPa();
                    boolean willNotify = needsNotificationAccordingToPA(preferences, pa);
                    Log.i(TAG, String.format("PA=%d. Will notify? %b", pa, willNotify));
                    if (willNotify) {
                        notifyCurrentDlaAboutToExpire(receivedContext, currentDla, pa, preferences);
                    }
                } else {
                    Log.i(TAG, String.format("No need to notify for DLA=%s", currentDla));
                }

                if (now.after(beforeNextDla) && now.before(nextDla)) {
                    Log.i(TAG, String.format("Need to notify NDLA='%s' about to expire", nextDla));
                    notifyNextDlaAboutToExpire(receivedContext, nextDla, preferences);
                } else {
                    Log.i(TAG, String.format("No need to notify for NDLA=%s", nextDla));
                }

                if (requestUpdate && troll.getPvVariation() < 0 && preferences.notifyOnPvLoss) {
                    int pvLoss = Math.abs(troll.getPvVariation());
                    Log.i(TAG, String.format("Troll lost %d PV", pvLoss));
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

    protected void refreshDla(Context context, String trollId, boolean requestUpdate) {
        Troll troll = null;
        try {
            troll = getProfileProxy().refreshDLA(context, trollId);
        } catch (MissingLoginPasswordException e) {
            Log.w(TAG, "Missing trollId and/or password, unable to refresh DLA...");
        }
        trollLoaded(troll, context, requestUpdate);
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
                String dlaText = Trolls.GET_WIDGET_DLA_TEXT.apply(troll);

                for (int appWidgetId : appWidgetIds) {

                    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.home_screen_widget);
                    views.setTextViewText(R.id.widgetDla, dlaText);

                    // Tell the AppWidgetManager to perform an update on the current app widget
                    widgetManager.updateAppWidget(appWidgetId, views);
                }
            }

        } catch (Exception eee) {
            Log.e(TAG, "Unable to update widget(s)", eee);
        }

    }

    protected boolean shouldVibrate(Context context, PreferencesHolder preferences) {
        switch (preferences.silentNotification) {
            case ALWAYS:
                return false;
            case NEVER:
                return true;
            case BY_NIGHT:
                Calendar now = Calendar.getInstance();
                return (now.get(Calendar.HOUR_OF_DAY) >= 7 || now.get(Calendar.HOUR_OF_DAY) < 23);
            case WHEN_SILENT:
                AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                return am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
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

        boolean vibrate = shouldVibrate(context, preferences);

        displayNotification(context, NotificationType.DLA, notifTitle, notifText, vibrate);
    }

    protected void notifyNextDlaAboutToExpire(Context context, Date dla, PreferencesHolder preferences) {
        CharSequence notifTitle = context.getText(R.string.next_dla_expiring_title);
        String format = context.getText(R.string.next_dla_expiring_text).toString();
        CharSequence notifText = String.format(format, MhDlaNotifierUtils.formatHour(dla));

        boolean vibrate = shouldVibrate(context, preferences);

        displayNotification(context, NotificationType.DLA, notifTitle, notifText, vibrate);
    }

    protected void notifyPvLoss(Context context, int pvLoss, int pv, PreferencesHolder preferences) {
        String titleFormat = context.getText(R.string.pv_loss_title).toString();
        CharSequence notifTitle = String.format(titleFormat, pvLoss);
        String messageFormat = context.getText(R.string.pv_remaining_text).toString();
        CharSequence notifText = String.format(messageFormat, pv);

        boolean vibrate = shouldVibrate(context, preferences);

        displayNotification(context, NotificationType.PV_LOSS, notifTitle, notifText, vibrate);
    }

    protected void displayNotification(Context context, NotificationType type, CharSequence title, CharSequence text, boolean vibrate) {

        // The PendingIntent to launch our activity if the user selects this notification
        Intent main = new Intent(context, MainActivity.class);
        main.putExtra(MainActivity.EXTRA_FROM_NOTIFICATION, true);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, main, 0);

        long now = System.currentTimeMillis();

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.trarnoll_square_transparent_128);

        long[] vibratePattern = VIBRATE_PATTERNN;
        if (!vibrate) {
            vibratePattern = new long[] {100L};
        }

//        if (vibrate) {
//            notification.defaults |= Notification.DEFAULT_SOUND;
//            notification.defaults |= Notification.DEFAULT_VIBRATE;
//        }

        int notificationId = type.name().hashCode();
        Notification notification = new NotificationCompat.Builder(context)
                .setOnlyAlertOnce(true)
                .setVibrate(vibratePattern)
                .setLights(Color.YELLOW, 1500, 1500)
                .setSmallIcon(R.drawable.trarnoll_square_transparent_128)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setContentText(text)
                .setWhen(now)
                .setContentIntent(contentIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);
    }

}
