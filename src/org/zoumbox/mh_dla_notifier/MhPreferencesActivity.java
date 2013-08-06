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

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class MhPreferencesActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    protected static final Function<Context, String> GET_NOTIFY_WITHOUT_PA_SUMMARY = new Function<Context, String>() {
        @Override
        public String apply(Context context) {
            PreferencesHolder preferences = PreferencesHolder.load(context);
            String result = context.getText(R.string.prefs_notify_without_pa_false).toString();
            if (preferences.notifyWithoutPA) {
                result = context.getText(R.string.prefs_notify_without_pa_true).toString();
            }
            return result;
        }
    };

    protected static final Function<Context, String> GET_NOTIFY_ON_PV_LOSS_SUMMARY = new Function<Context, String>() {
        @Override
        public String apply(Context context) {
            PreferencesHolder preferences = PreferencesHolder.load(context);
            String result = context.getText(R.string.boolean_false).toString();
            if (preferences.notifyOnPvLoss) {
                result = context.getText(R.string.boolean_true).toString();
            }
            return result;
        }
    };

    protected static final Function<Context, String> GET_SILENT_NOTIF_SUMMARY = new Function<Context, String>() {
        @Override
        public String apply(Context context) {
            PreferencesHolder preferences = PreferencesHolder.load(context);
            String result;
            switch (preferences.silentNotification) {
                case NEVER:
                    result = context.getText(R.string.prefs_silent_notification_never).toString();
                    break;
                case ALWAYS:
                    result = context.getText(R.string.prefs_silent_notification_always).toString();
                    break;
                case WHEN_SILENT:
                    result = context.getText(R.string.prefs_silent_notification_when_silent).toString();
                    break;
                default:
                    result = context.getText(R.string.prefs_silent_notification_by_night).toString();
                    break;
            }
            return result;
        }
    };

    protected static final Function<Context, String> GET_NOTIF_DELAY_SUMMARY = new Function<Context, String>() {
        @Override
        public String apply(Context context) {
            PreferencesHolder preferences = PreferencesHolder.load(context);
            String summaryFormat = context.getText(R.string.prefs_notification_delay_summary).toString();
            String result = String.format(summaryFormat, preferences.notificationDelay);
            return result;
        }
    };

    protected static final Function<Context, String> GET_USE_SMARTPHONE_SUMMARY = new Function<Context, String>() {
        @Override
        public String apply(Context context) {
            PreferencesHolder preferences = PreferencesHolder.load(context);
            String result = context.getText(R.string.boolean_false).toString();
            if (preferences.useSmartphoneInterface) {
                result = context.getText(R.string.boolean_true).toString();
            }
            return result;
        }
    };

    protected static final Map<String, Function<Context, String>> preferencesFunctions = Maps.newLinkedHashMap();

    static {
        preferencesFunctions.put(PreferencesHolder.PREFS_NOTIFY_WITHOUT_PA, GET_NOTIFY_WITHOUT_PA_SUMMARY);
        preferencesFunctions.put(PreferencesHolder.PREFS_SILENT_NOTIFICATION, GET_SILENT_NOTIF_SUMMARY);
        preferencesFunctions.put(PreferencesHolder.PREFS_NOTIFICATION_DELAY, GET_NOTIF_DELAY_SUMMARY);
        preferencesFunctions.put(PreferencesHolder.PREFS_SMARTPHONE_INTERFACE, GET_USE_SMARTPHONE_SUMMARY);
        preferencesFunctions.put(PreferencesHolder.PREFS_NOTIFY_ON_PV_LOSS, GET_NOTIFY_ON_PV_LOSS_SUMMARY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        for (Map.Entry<String, Function<Context, String>> entry : preferencesFunctions.entrySet()) {
            String key = entry.getKey();
            String summary = entry.getValue().apply(this);

            Preference preference = findPreference(key);
            preference.setSummary(summary);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Function<Context, String> function = preferencesFunctions.get(key);
        String summary = function.apply(this);

        Preference preference = findPreference(key);
        preference.setSummary(summary);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(this); // To make sure it is registered only once
    }
}
