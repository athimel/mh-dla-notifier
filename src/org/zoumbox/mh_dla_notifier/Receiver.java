/*
 * #%L
 * MountyHall DLA Notifier
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2012 Zoumbox.org
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

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import com.google.common.base.Predicate;
import org.zoumbox.mh_dla_notifier.profile.MissingLoginPasswordException;
import org.zoumbox.mh_dla_notifier.profile.ProfileProxy;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptException;

import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class Receiver extends BroadcastReceiver {

    private static final String TAG = Constants.LOG_PREFIX + Receiver.class.getSimpleName();

    protected static final Predicate<Date> IS_IN_THE_FUTURE = new Predicate<Date>() {
        @Override
        public boolean apply(@Nullable Date dla) {
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

    protected boolean mustRegisterAlarm(PreferencesHolder preferences, Date dla) {
        if (dla == null) {
            return false;
        }
        Date dlaMinusDelay = MhDlaNotifierUtils.substractMinutes(dla, preferences.notificationDelay);
        boolean result = new Date().before(dlaMinusDelay);
        return result;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "Alarm received : " + Math.random());

        Pair<Date, Integer> dlaPaPair = null;
        try {
            dlaPaPair = ProfileProxy.refreshDLA(context);
        } catch (MissingLoginPasswordException mlpe) {
            // Nothing to do
        } catch (PublicScriptException e) {
            // Nothing to do
        }

        if (dlaPaPair != null) {
            Date dla = dlaPaPair.left();
            Integer pa = dlaPaPair.right();

            PreferencesHolder preferences = PreferencesHolder.load(context);

            if (mustRegisterAlarm(preferences, dla)) {
                registerDlaAlarm(context, dla, preferences);
            } else
                // On vérifie que la date est bien dans le futur et dans moins de 5 min
                if (IS_IN_THE_FUTURE.apply(dla) && needsNotificationAccordingToPA(preferences, pa)) {
                    notifyDlaAboutToExpire(context, dla, pa, preferences);
                } else {
                    notifyDlaExpired(context, dla, pa, preferences);
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

    protected void notifyDlaAboutToExpire(Context context, Date dla, Integer pa, PreferencesHolder preferences) {
        CharSequence notifTitle = context.getText(R.string.dla_expiring_title);
        String format = context.getText(R.string.dla_expiring_text).toString();
        CharSequence notifText = String.format(format, MhDlaNotifierUtils.formatHour(dla), pa);

        boolean vibrate = shouldVibrate(context, preferences);

        displayNotification(context, notifTitle, notifText, vibrate);
    }

    protected void notifyDlaExpired(Context context, Date dla, Integer pa, PreferencesHolder preferences) {
        if (pa == 0 && !IS_IN_THE_FUTURE.apply(dla)) {
            CharSequence notifTitle = context.getText(R.string.dla_expired_title);
            String format = context.getText(R.string.dla_expired_text).toString();
            CharSequence notifText = String.format(format, MhDlaNotifierUtils.formatHour(dla));

            boolean vibrate = shouldVibrate(context, preferences);
            displayNotification(context, notifTitle, notifText, vibrate);
        }
    }

    protected void displayNotification(Context context, CharSequence title, CharSequence text, boolean vibrate) {
        long now = System.currentTimeMillis();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.trarnoll_square_transparent_128, title, now);
        if (vibrate) {
            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }

        // The PendingIntent to launch our activity if the user selects this notification
        Intent main = new Intent(context, MhDlaNotifierImpl.class);
        main.putExtra(MhDlaNotifierImpl.EXTRA_FROM_NOTIFICATION, true);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, main, 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(context, title, text, contentIntent);

        notificationManager.notify(0, notification);
    }

    public static Date registerDlaAlarm(Context context) {
        PreferencesHolder preferencesHolder = PreferencesHolder.load(context);
        Date dla = ProfileProxy.getDLA(context);
        Date result = registerDlaAlarm(context, dla, preferencesHolder);
        return result;
    }

    private static Date registerDlaAlarm(Context context, Date dla, PreferencesHolder preferences) {
        Date nextAlarm = null;
        if (dla != null && IS_IN_THE_FUTURE.apply(dla)) {
            Date dlaAlarm = MhDlaNotifierUtils.substractMinutes(dla, preferences.notificationDelay);

            if (IS_IN_THE_FUTURE.apply(nextAlarm)) {
                Intent intent = new Intent(context, Receiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context, 86956675, intent, 0);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, dlaAlarm.getTime(), pendingIntent);

                nextAlarm = dlaAlarm;
            }
        }
        return nextAlarm;

    }
}
