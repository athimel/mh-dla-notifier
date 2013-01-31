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

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class MhPreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        PreferencesHolder preferences = PreferencesHolder.load(this);

        {
            Preference preference = findPreference("prefs.notify_without_pa");
            String summary = getText(R.string.prefs_notify_without_pa_false).toString();
            if (preferences.notifyWithoutPA) {
                summary = getText(R.string.prefs_notify_without_pa_true).toString();
            }
            preference.setSummary(summary);
        }

        {
            Preference preference = findPreference("prefs.silent_notification");
            String summary;
            switch (preferences.silentNotification) {
                case NEVER:
                    summary = getText(R.string.prefs_silent_notification_never).toString();
                    break;
                case ALWAYS:
                    summary = getText(R.string.prefs_silent_notification_always).toString();
                    break;
                case WHEN_SILENT:
                    summary = getText(R.string.prefs_silent_notification_when_silent).toString();
                    break;
                default:
                    summary = getText(R.string.prefs_silent_notification_by_night).toString();
                    break;
            }
            preference.setSummary(summary);
        }

        {
            Preference preference = findPreference("prefs.notification_delay");
            String summaryFormat = getText(R.string.prefs_notification_delay_summary).toString();
            String summary = String.format(summaryFormat, preferences.notificationDelay);
            preference.setSummary(summary);
        }

        {
            Preference preference = findPreference("prefs.use_smartphone_interface");
            String summary = getText(R.string.prefs_use_smartphone_interface_false).toString();
            if (preferences.useSmartphoneInterface) {
                summary = getText(R.string.prefs_use_smartphone_interface_true).toString();
            }
            preference.setSummary(summary);
        }

    }
}
