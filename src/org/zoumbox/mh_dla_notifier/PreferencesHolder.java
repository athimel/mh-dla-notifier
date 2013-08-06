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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class PreferencesHolder {

    public static final String PREFS_ALLOW_AUTOMATIC_UPDATES = "prefs.allow_automatic_updates";
    public static final String PREFS_NOTIFICATION_DELAY = "prefs.notification_delay";
    public static final String PREFS_SILENT_NOTIFICATION = "prefs.silent_notification";
    public static final String PREFS_NOTIFY_WITHOUT_PA = "prefs.notify_without_pa";
    public static final String PREFS_NOTIFY_ON_PV_LOSS = "prefs.notify_on_pv_loss";

    public static final String PREFS_SMARTPHONE_INTERFACE = "prefs.use_smartphone_interface";

    public static final String SKIP_LEGACY_PASSWORD_CHECK_UNTIL = "prefs.skip_legacy_password_check_until";

    public int notificationDelay;
    public boolean notifyWithoutPA;
    public boolean notifyOnPvLoss;
    public SilentNotification silentNotification;

    public boolean useSmartphoneInterface;

    public boolean enableAutomaticUpdates;

    public long skipLegacyPasswordCheckUntil;


    public static PreferencesHolder load(Context context) {

        PreferencesHolder result = new PreferencesHolder();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        result.notificationDelay = Integer.parseInt(prefs.getString(PREFS_NOTIFICATION_DELAY, "" + MhDlaNotifierConstants.DEFAULT_NOTIFICATION_DELAY));
        result.notifyWithoutPA = prefs.getBoolean(PREFS_NOTIFY_WITHOUT_PA, MhDlaNotifierConstants.DEFAULT_NOTIFY_WITHOUT_PA);

        String silentNotificationValue = prefs.getString(PREFS_SILENT_NOTIFICATION, SilentNotification.BY_NIGHT.name());
        result.silentNotification = SilentNotification.valueOf(silentNotificationValue);

        result.useSmartphoneInterface = prefs.getBoolean(PREFS_SMARTPHONE_INTERFACE, MhDlaNotifierConstants.DEFAULT_USE_SMARTPHONE_INTERFACE);

        result.notifyOnPvLoss = prefs.getBoolean(PREFS_NOTIFY_ON_PV_LOSS, MhDlaNotifierConstants.DEFAULT_NOTIFY_ON_PV_LOSS);

        result.enableAutomaticUpdates = prefs.getBoolean(PREFS_ALLOW_AUTOMATIC_UPDATES, MhDlaNotifierConstants.DEFAULT_ALLOW_AUTOMATIC_UPDATES);

        result.skipLegacyPasswordCheckUntil = prefs.getLong(SKIP_LEGACY_PASSWORD_CHECK_UNTIL, 0l);

        return result;
    }

    public static void skipLegacyPasswordCheckForToday(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = prefs.edit();

        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY, 23);
        instance.set(Calendar.MINUTE, 59);
        instance.set(Calendar.SECOND, 59);
        instance.set(Calendar.MILLISECOND, 59);

        editor.putLong(SKIP_LEGACY_PASSWORD_CHECK_UNTIL, instance.getTimeInMillis());
        editor.commit();

    }

}
