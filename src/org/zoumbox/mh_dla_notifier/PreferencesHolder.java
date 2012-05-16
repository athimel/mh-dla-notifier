package org.zoumbox.mh_dla_notifier;

import android.content.Context;
import android.content.SharedPreferences;
import org.zoumbox.mh_dla_notifier.profile.ProfileProxy;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class PreferencesHolder {

    protected static final String PREFS_NOTIFICATION_DELAY = "prefs.notification_delay";
    protected static final String PREFS_VIBRATION_MODE = "prefs.vibration_mode";
    protected static final String PREFS_NOTIFY_WITHOUT_PA = "prefs.notify_without_pa";

    public int notificationDelay;
    public boolean notifyWithoutPA;
//    public VibrationMode mode;

    public static PreferencesHolder load(Context context) {
        PreferencesHolder result = new PreferencesHolder();
        SharedPreferences preferences = context.getSharedPreferences(ProfileProxy.PREFS_NAME, 0);

        result.notificationDelay = preferences.getInt(PREFS_NOTIFICATION_DELAY, Constants.DEFAULT_NOTIFICATION_DELAY);
        result.notifyWithoutPA = preferences.getBoolean(PREFS_NOTIFY_WITHOUT_PA, Constants.DEFAULT_NOTIFY_WITHOUT_PA);
        return result;
    }

    public void save(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(ProfileProxy.PREFS_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREFS_NOTIFICATION_DELAY, notificationDelay);
        editor.putBoolean(PREFS_NOTIFY_WITHOUT_PA, notifyWithoutPA);
        editor.commit();
    }
}
