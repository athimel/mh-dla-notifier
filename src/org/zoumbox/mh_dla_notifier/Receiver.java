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

import org.zoumbox.mh_dla_notifier.profile.MissingLoginPasswordException;
import org.zoumbox.mh_dla_notifier.profile.ProfileProxy;
import org.zoumbox.mh_dla_notifier.profile.v1.ProfileProxyV1;
import org.zoumbox.mh_dla_notifier.profile.v2.ProfileProxyV2;
import org.zoumbox.mh_dla_notifier.troll.Troll;
import org.zoumbox.mh_dla_notifier.troll.Trolls;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.util.Log;

/**
 * Classe appelée lorsqu'un réveil de l'application survient (approche d'une DLA ou vérification intermédiaire)
 *
 * @author Arno <arno@zoumbox.org>
 */
public class Receiver extends BroadcastReceiver {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + Receiver.class.getSimpleName();

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

        if (getProfileProxy().areTrollIdentifiersUndefined(context)) {
            Log.i(TAG, "No troll registered, exiting...");
            return;
        }

        String trollId = intent.getStringExtra(Alarms.EXTRA_TROLL_ID);
        if (Strings.isNullOrEmpty(trollId)) {
            Log.i(TAG, "TrollId not defined, exiting...");
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

        Troll troll = null;
        try {
            if (requestUpdate) {
                troll = getProfileProxy().refreshDLA(context, null);
            } else if (requestAlarmRegistering) {
                troll = getProfileProxy().fetchTrollWithoutUpdate(context, null).left();
            } else {
                Log.i(TAG, "Skip loading Troll");
            }
        } catch (MissingLoginPasswordException mde) {
            Log.w(TAG, "Missing trollId and/or password, exiting...");
            return;
        }

        if (troll != null) {

            PreferencesHolder preferences = PreferencesHolder.load(context);

            // Re-schedule them (in case it changed)
            Map<AlarmType, Date> alarms;
            try {
                alarms = Alarms.scheduleAlarms(context, getProfileProxy(), trollId);
            } catch (MissingLoginPasswordException e) {
                Log.w(TAG, "Missing trollId and/or password, exiting...");
                return;
            }

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
                    notifyCurrentDlaAboutToExpire(context, currentDla, pa, preferences);
                }
            } else {
                Log.i(TAG, String.format("No need to notify for DLA=%s", currentDla));
            }

            if (now.after(beforeNextDla) && now.before(nextDla)) {
                Log.i(TAG, String.format("Need to notify NDLA='%s' about to expire", nextDla));
                notifyNextDlaAboutToExpire(context, nextDla, preferences);
            } else {
                Log.i(TAG, String.format("No need to notify for NDLA=%s", nextDla));
            }

            if (requestUpdate && troll.getPvVariation() < 0 && preferences.notifyOnPvLoss) {
                int pvLoss = Math.abs(troll.getPvVariation());
                Log.i(TAG, String.format("Troll lost %d PV", pvLoss));
                notifyPvLoss(context, pvLoss, troll.getPv(), preferences);
            }
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
        String messageFormat = context.getText(R.string.pv_loss_text).toString();
        CharSequence notifText = String.format(messageFormat, pv);

        boolean vibrate = shouldVibrate(context, preferences);

        displayNotification(context, NotificationType.PV_LOSS, notifTitle, notifText, vibrate);
    }

    protected void displayNotification(Context context, NotificationType type, CharSequence title, CharSequence text, boolean vibrate) {
        long now = System.currentTimeMillis();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.trarnoll_square_transparent_128, title, now);
        if (vibrate) {
            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }

        // The PendingIntent to launch our activity if the user selects this notification
        Intent main = new Intent(context, MainActivity.class);
        main.putExtra(MainActivity.EXTRA_FROM_NOTIFICATION, true);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, main, 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(context, title, text, contentIntent);

        notificationManager.notify(type.name().hashCode(), notification);
    }

}
