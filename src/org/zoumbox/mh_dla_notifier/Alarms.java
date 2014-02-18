package org.zoumbox.mh_dla_notifier;

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

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.zoumbox.mh_dla_notifier.profile.MissingLoginPasswordException;
import org.zoumbox.mh_dla_notifier.profile.ProfileProxy;
import org.zoumbox.mh_dla_notifier.troll.Troll;
import org.zoumbox.mh_dla_notifier.troll.Trolls;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class Alarms {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + Alarms.class.getSimpleName();

    protected static final String EXTRA_TYPE = "type";
    protected static final String EXTRA_TROLL_ID = "trollId";

    private static Map<AlarmType, Date> getAlarms(Troll troll, int notificationDelay) {
        Preconditions.checkNotNull(troll);

        Date currentDla = troll.getDla();
        Map<AlarmType, Date> result = Maps.newLinkedHashMap();

        if (currentDla != null) {
            Date nextDla = Trolls.GET_NEXT_DLA.apply(troll);

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

            result.put(AlarmType.CURRENT_DLA, MhDlaNotifierUtils.substractMinutes(currentDla, notificationDelay));
            result.put(AlarmType.AFTER_CURRENT_DLA, afterCurrentDla);
            result.put(AlarmType.NEXT_DLA, MhDlaNotifierUtils.substractMinutes(nextDla, notificationDelay));
            result.put(AlarmType.AFTER_NEXT_DLA, afterNextDla);
            result.put(AlarmType.DLA_EVEN_AFTER, MhDlaNotifierUtils.substractMinutes(dlaEvenAfter, notificationDelay));
        }

        Log.i(TAG, "Computed wakeups: " + result);

        return result;
    }

    private static Map<AlarmType, Date> scheduleAlarms(Context context, Map<AlarmType, Date> alarms, String trollId) {

        Map<AlarmType, Date> result = Maps.newLinkedHashMap();
        for (Map.Entry<AlarmType, Date> entry : alarms.entrySet()) {
            AlarmType alarmType = entry.getKey();
            Date alarmDate = entry.getValue();
            boolean isScheduled = scheduleAlarm(context, alarmDate, alarmType, trollId);
            if (isScheduled) {
                result.put(alarmType, alarmDate);
            }
        }

        return result;
    }

    private static boolean scheduleAlarm(Context context, Date alarmDate, AlarmType type, String trollId) {
        if (alarmDate != null && MhDlaNotifierUtils.IS_IN_THE_FUTURE.apply(alarmDate)) {
            Intent intent = new Intent(context, Receiver.class);
            intent.putExtra(EXTRA_TYPE, type.name());
            intent.putExtra(EXTRA_TROLL_ID, trollId);
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

    public static Map<AlarmType, Date> scheduleAlarms(Context context, ProfileProxy profileProxy, String trollId) throws MissingLoginPasswordException {
        PreferencesHolder preferences = PreferencesHolder.load(context);
        Troll troll = profileProxy.fetchTrollWithoutUpdate(context, trollId).left();

        Map<AlarmType, Date> alarms = getAlarms(troll, preferences.notificationDelay);
        Map<AlarmType, Date> scheduledAlarms = scheduleAlarms(context, alarms, trollId);
        return scheduledAlarms;
    }

    public static Map<AlarmType, Date> getAlarms(Context context, ProfileProxy profileProxy, String trollId) throws MissingLoginPasswordException {
        PreferencesHolder preferences = PreferencesHolder.load(context);
        Troll troll = profileProxy.fetchTrollWithoutUpdate(context, trollId).left();

        Map<AlarmType, Date> alarms = getAlarms(troll, preferences.notificationDelay);
        return alarms;
    }
}
