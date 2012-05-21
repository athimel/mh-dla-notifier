package org.zoumbox.mh_dla_notifier;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class PreferencesHolder {

    protected static final String PREFS_NOTIFICATION_DELAY = "prefs.notification_delay";
    protected static final String PREFS_SILENT_NOTIFICATION = "prefs.silent_notification";
    protected static final String PREFS_NOTIFY_WITHOUT_PA = "prefs.notify_without_pa";

    public int notificationDelay;
    public boolean notifyWithoutPA;
    public SilentNotification silentNotification;

    public static PreferencesHolder load(Context context) {

        PreferencesHolder result = new PreferencesHolder();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        result.notificationDelay = Integer.parseInt(prefs.getString(PREFS_NOTIFICATION_DELAY, ""+Constants.DEFAULT_NOTIFICATION_DELAY));
        result.notifyWithoutPA = prefs.getBoolean(PREFS_NOTIFY_WITHOUT_PA, Constants.DEFAULT_NOTIFY_WITHOUT_PA);

        String silentNotificationValue = prefs.getString(PREFS_SILENT_NOTIFICATION, SilentNotification.BY_NIGHT.name());
        result.silentNotification = SilentNotification.valueOf(silentNotificationValue);

        return result;
    }

}
