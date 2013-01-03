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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class PreferencesHolder {

    protected static final String PREFS_NOTIFICATION_DELAY = "prefs.notification_delay";
    protected static final String PREFS_SILENT_NOTIFICATION = "prefs.silent_notification";
    protected static final String PREFS_NOTIFY_WITHOUT_PA = "prefs.notify_without_pa";

    protected static final String PREFS_SMARTPHONE_INTERFACE = "prefs.use_smartphone_interface";

    public int notificationDelay;
    public boolean notifyWithoutPA;
    public SilentNotification silentNotification;

    public boolean useSmartphoneInterface;

    public static PreferencesHolder load(Context context) {

        PreferencesHolder result = new PreferencesHolder();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        result.notificationDelay = Integer.parseInt(prefs.getString(PREFS_NOTIFICATION_DELAY, "" + Constants.DEFAULT_NOTIFICATION_DELAY));
        result.notifyWithoutPA = prefs.getBoolean(PREFS_NOTIFY_WITHOUT_PA, Constants.DEFAULT_NOTIFY_WITHOUT_PA);

        String silentNotificationValue = prefs.getString(PREFS_SILENT_NOTIFICATION, SilentNotification.BY_NIGHT.name());
        result.silentNotification = SilentNotification.valueOf(silentNotificationValue);

        result.useSmartphoneInterface = prefs.getBoolean(PREFS_SMARTPHONE_INTERFACE, true);

        return result;
    }

}
