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
import org.zoumbox.mh_dla_notifier.profile.v1.ProfileProxyV1;
import org.zoumbox.mh_dla_notifier.troll.Troll;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import android.app.AlarmManager;
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
 * @author Arno <arno@zoumbox.org>
 */
public class Receiver extends BroadcastReceiver {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + Receiver.class.getSimpleName();

    protected static final Predicate<Date> IS_IN_THE_FUTURE = new Predicate<Date>() {
        @Override
        public boolean apply(Date dla) {
            if (dla == null) {
                return false;
            }
            boolean result = dla.after(new Date());
            return result;
        }
    };

    protected boolean needsNotificationAccordingToPA(PreferencesHolder preferences, int pa) {
        boolean result = pa > 0 || preferences.notifyWithoutPA;
        return result;
    }

    protected static final Predicate<Context> JUST_RESTARTED = new Predicate<Context>() {
        @Override
        public boolean apply(Context context) {
            // Check if the device just restarted
            long elapsedSinceLastRestartCheck = ProfileProxyV1.getElapsedSinceLastRestartCheck(context);
            Log.i(TAG, "Elapsed since last restart check: " + elapsedSinceLastRestartCheck + "ms ~= " + (elapsedSinceLastRestartCheck/60000) + "min");

            long uptime = SystemClock.uptimeMillis();
            Log.i(TAG, "Uptime: " + uptime + "ms ~= " + (uptime/60000) + "min");

            boolean result = elapsedSinceLastRestartCheck > uptime;
            if (result) {
                ProfileProxyV1.restartCheckDone(context);
            }
            Log.i(TAG, "Device restarted since last check: " + result);
            return result;
        }
    };

    protected static final Predicate<Context> SHOULD_UPDATE_BECAUSE_OF_RESTART = new Predicate<Context>() {
        @Override
        public boolean apply(Context context) {
            boolean result = false;
            // Check if the device restarted since last update
            Long elapsedSinceLastSuccess = ProfileProxyV1.getElapsedSinceLastUpdateSuccess(context);
            if (elapsedSinceLastSuccess != null) {
                Log.i(TAG, "Elapsed since last update success: " + elapsedSinceLastSuccess + "ms ~= " + (elapsedSinceLastSuccess / 60000) + "min");

                long upTime = SystemClock.uptimeMillis();
                Log.i(TAG, "Uptime: " + upTime + "ms ~= " + (upTime/60000) + "min");

                result = elapsedSinceLastSuccess > upTime; // Device restarted since last update
                Log.i(TAG, "shouldUpdateBecauseOfRestart: " + result);
                result &= elapsedSinceLastSuccess > (1000l * 60l * 60l * 2l); // 2 hours
                Log.i(TAG, "shouldUpdateBecauseOfRestart (<=2hours): " + result);
            }
            return result;
        }
    };

    protected static final Predicate<Context> SHOULD_UPDATE_BECAUSE_OF_NETWORK_FAILURE = new Predicate<Context>() {
        @Override
        public boolean apply(Context context) {
            String lastUpdateResult = ProfileProxyV1.getLastUpdateResult(context);
            Log.i(TAG, "lastUpdateResult: " + lastUpdateResult);
            boolean result = lastUpdateResult != null && lastUpdateResult.startsWith("NETWORK ERROR");
            Log.i(TAG, "shouldUpdateBecauseOfNetworkFailure: " + result);
            return result;
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {

        String intentAction = intent.getAction();
        Log.i(TAG, String.format("<<< %s#onReceive action=%s", getClass().getName(), intentAction));

        boolean connectivityChanged = ConnectivityManager.CONNECTIVITY_ACTION.equals(intentAction);
        boolean justGotConnection = false;
        if (connectivityChanged) {

            ConnectivityManager cm =
                    (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            justGotConnection = activeNetwork != null && activeNetwork.isConnected();
            Log.i(TAG, "Connectivity change. isConnected=" + justGotConnection);
        }

        if (connectivityChanged && !justGotConnection) {
            Log.i(TAG, "Just lost connectivity, nothing to do");
            return;
        }

        if (ProfileProxyV1.areTrollIdentifiersUndefined(context)) {
            Log.i(TAG, "TrollId not defined, exiting...");
            return;
        }

        // If type is provided, request for an update
        String type = intent.getStringExtra("type");
        boolean requestUpdate = !Strings.isNullOrEmpty(type);

        // If device just started, request for wakeups registration
        boolean requestAlarmRegistering = JUST_RESTARTED.apply(context);

        // Just go the internet connection back. Update will be necessary if
        //  - device restarted since last update and last update in more than 2 hours ago
        //  - last update failed because of network error
        if (!requestUpdate && justGotConnection) { // !requestUpdate because no need to check if update is already requested

            boolean shouldUpdateBecauseOfRestart = SHOULD_UPDATE_BECAUSE_OF_RESTART.apply(context);
            boolean shouldUpdateBecauseOfNetworkFailure = SHOULD_UPDATE_BECAUSE_OF_NETWORK_FAILURE.apply(context);

            requestUpdate = shouldUpdateBecauseOfRestart || shouldUpdateBecauseOfNetworkFailure;
        }

        Log.i(TAG, String.format("requestUpdate=%b ; requestAlarmRegistering=%b", requestUpdate, requestAlarmRegistering));

        Troll troll = null;
        try {
            if (requestUpdate) {
                troll = ProfileProxyV1.refreshDLA(context);
            } else if (requestAlarmRegistering) {
                troll = ProfileProxyV1.fetchTrollWithoutUpdate(context);
            } else {
                Log.i(TAG, "Skip loading Troll");
            }
        } catch (MissingLoginPasswordException mde) {
            Log.w(TAG, "Missing trollId and/or password, exiting...");
            return;
        }

        if (troll != null) {

            PreferencesHolder preferences = PreferencesHolder.load(context);

            // Compute alarms
            Map<AlarmType, Date> alarms = getAlarms(troll, preferences.notificationDelay);

            // Re-schedule them (in case it changed)
            scheduleAlarms(context, alarms);

            Date now = new Date();

            Date beforeCurrentDla = alarms.get(AlarmType.CURRENT_DLA);
            Date currentDla = troll.getDla();
            Date beforeNextDla = alarms.get(AlarmType.NEXT_DLA);
            Date nextDla = troll.getComputedNextDla();

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

    protected static Map<AlarmType, Date> getAlarms(Troll troll, int notificationDelay) {
        Preconditions.checkNotNull(troll);

        Date currentDla = troll.getDla();
        Date nextDla = troll.getComputedNextDla();

        Log.i(TAG, String.format("Computing wakeups for [DLA=%s] [NDLA=%s]", currentDla, nextDla));

        long millisecondsBetween = nextDla.getTime() - currentDla.getTime();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentDla.getTime());
        calendar.add(Calendar.MILLISECOND, new Long(millisecondsBetween / 2).intValue());
        Date afterCurrentDla = calendar.getTime();

        calendar.setTimeInMillis(nextDla.getTime());
        calendar.add(Calendar.MILLISECOND, new Long(millisecondsBetween / 2).intValue());
        Date afterNextDla = calendar.getTime();

        calendar.setTimeInMillis(nextDla.getTime());
        calendar.add(Calendar.MILLISECOND, new Long(millisecondsBetween * 9 / 10).intValue());
        Date dlaEvenAfter = calendar.getTime();

        Map<AlarmType, Date> result = Maps.newLinkedHashMap();

        result.put(AlarmType.CURRENT_DLA, MhDlaNotifierUtils.substractMinutes(currentDla, notificationDelay));
        result.put(AlarmType.AFTER_CURRENT_DLA, afterCurrentDla);
        result.put(AlarmType.NEXT_DLA, MhDlaNotifierUtils.substractMinutes(nextDla, notificationDelay));
        result.put(AlarmType.AFTER_NEXT_DLA, afterNextDla);
        result.put(AlarmType.DLA_EVEN_AFTER, MhDlaNotifierUtils.substractMinutes(dlaEvenAfter, notificationDelay));

        Log.i(TAG, "Computed wakeups: " + result);

        return result;
    }

    public static Map<AlarmType, Date> scheduleAlarms(Context context) throws MissingLoginPasswordException {
        PreferencesHolder preferences = PreferencesHolder.load(context);
        Troll troll = ProfileProxyV1.fetchTrollWithoutUpdate(context);

        Map<AlarmType, Date> alarms = getAlarms(troll, preferences.notificationDelay);
        Map<AlarmType, Date> scheduledAlarms = scheduleAlarms(context, alarms);
        return scheduledAlarms;
    }

    protected static Map<AlarmType, Date> scheduleAlarms(Context context, Map<AlarmType, Date> alarms) {

        Map<AlarmType, Date> result = Maps.newLinkedHashMap();
        for (Map.Entry<AlarmType, Date> entry : alarms.entrySet()) {
            AlarmType alarmType = entry.getKey();
            Date alarmDate = entry.getValue();
            boolean isScheduled = scheduleAlarm(context, alarmDate, alarmType);
            if (isScheduled) {
                result.put(alarmType, alarmDate);
            }
        }

        return result;
    }

    private static boolean scheduleAlarm(Context context, Date alarmDate, AlarmType type) {
        if (alarmDate != null && IS_IN_THE_FUTURE.apply(alarmDate)) {
            Intent intent = new Intent(context, Receiver.class);
            intent.putExtra("type", type.name());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, type.getIdentifier(), intent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmDate.getTime(), pendingIntent);

            Log.i(TAG, String.format("Scheduled wakeup [%s] at '%s'", type, alarmDate));
            return true;
        } else {
            Log.i(TAG, String.format("No wakeup [%s] scheduled at '%s'", type, alarmDate));
            return false;
        }

    }

}
