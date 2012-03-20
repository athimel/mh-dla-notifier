package org.zoumbox.mh_dla_notifier.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.zoumbox.mh_dla_notifier.Constants;
import org.zoumbox.mh_dla_notifier.MhDlaNotifierUtils;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptException;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptsProxy;
import org.zoumbox.mh_dla_notifier.sp.PublicScript;
import org.zoumbox.mh_dla_notifier.sp.QuotaExceededException;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class ProfileProxy {

    private static final String TAG = Constants.LOG_PREFIX + ProfileProxy.class.getSimpleName();

    public static final String PREFS_NAME = "org.zoumbox.mh.dla.notifier.preferences";

    public static final String PROPERTY_TROLL_ID = "trollId";
    public static final String PROPERTY_TROLL_PASSWORD = "trollPassword";

    public static final String PROPERTY_DLA = "dla";
    public static final String PROPERTY_PA_RESTANT = "paRestant";

    protected static Map<String, PublicScript> properties = Maps.newHashMap();


    static {
        properties.put("nom", PublicScript.ProfilPublic2);
        properties.put("race", PublicScript.ProfilPublic2);
        properties.put("niveau", PublicScript.ProfilPublic2);
        properties.put("dateInscription", PublicScript.ProfilPublic2);
        properties.put("email", PublicScript.ProfilPublic2);
        properties.put("blason", PublicScript.ProfilPublic2);
        properties.put("nbMouches", PublicScript.ProfilPublic2);
        properties.put("nbKills", PublicScript.ProfilPublic2);
        properties.put("nbMorts", PublicScript.ProfilPublic2);
        properties.put("numeroDeGuilde", PublicScript.ProfilPublic2);

        properties.put("posX", PublicScript.Profil2);
        properties.put("posY", PublicScript.Profil2);
        properties.put("posN", PublicScript.Profil2);
        properties.put("pv", PublicScript.Profil2);
        properties.put("pvMax", PublicScript.Profil2);
        properties.put(PROPERTY_PA_RESTANT, PublicScript.Profil2);
        properties.put(PROPERTY_DLA, PublicScript.Profil2);
        properties.put("fatigue", PublicScript.Profil2);
        properties.put("dureeDuTour", PublicScript.Profil2);
        properties.put("bonusDuree", PublicScript.Profil2);

        properties.put("px", PublicScript.Profil3);
        properties.put("pxPerso", PublicScript.Profil3);
        properties.put("pi", PublicScript.Profil3);
        properties.put("gg", PublicScript.Profil3);
    }

    public static boolean needsUpdate(Context context, PublicScript script, String trollNumber) {
        Date lastUpdate = PublicScriptsProxy.geLastUpdate(context, script, trollNumber);
        if (lastUpdate == null) {
            return true;
        } else {
            int dailyQuota = script.category.getQuota();
            int minutesDelay = 72 * 60 / dailyQuota; //FIXME AThimel 29/03/2012 72h instead of 24 for the moment to avoid mistakes
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, -minutesDelay);
            Date delay = calendar.getTime();
            boolean result = lastUpdate.before(delay);
            return result;
        }
    }

    public static String getTrollNumber(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        String trollNumber = preferences.getString(PROPERTY_TROLL_ID, null);
        return trollNumber;
    }

    public static Map<String, String> fetchProperties(final Context context, String... names) throws QuotaExceededException, MissingLoginPasswordException, PublicScriptException {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        final String trollNumber = preferences.getString(PROPERTY_TROLL_ID, null);
        String trollPassword = preferences.getString(PROPERTY_TROLL_PASSWORD, null);
        if (Strings.isNullOrEmpty(trollNumber) || Strings.isNullOrEmpty(trollPassword)) {
            throw new MissingLoginPasswordException();
        }

        Set<PublicScript> scripts = Sets.newLinkedHashSet();
        Log.i(TAG, "Requesting properties: " + names);
        for (String propertyName : names) {
            PublicScript script = properties.get(propertyName);
            if (script == null) {
                Log.i(TAG, "Unknown property: " + propertyName);
            } else {
                scripts.add(script);
            }
        }

        Iterables.removeIf(scripts, new Predicate<PublicScript>() {
            @Override
            public boolean apply(PublicScript script) {
                return !needsUpdate(context, script, trollNumber);
            }
        });

        if (!scripts.isEmpty()) {

            Toast.makeText(context, "Mise à jour des informations...", Toast.LENGTH_LONG).show();

            for (PublicScript type : scripts) {
                Map<String, String> propertiesFetched = PublicScriptsProxy.fetch(context, type, trollNumber, trollPassword, false);
                SharedPreferences.Editor editor = preferences.edit();
                for (Map.Entry<String, String> prop : propertiesFetched.entrySet()) {
                    editor.putString(prop.getKey(), prop.getValue());
                }
                editor.commit();
            }
        }

        Map<String, String> result = Maps.newLinkedHashMap();
        for (String propertyName : names) {
            String value = preferences.getString(propertyName, null);
            result.put(propertyName, value);
        }
        return result;
    }

    public static String loadLogin(Context context) {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        String result = preferences.getString(PROPERTY_TROLL_ID, null);
        return result;
    }

    public static boolean saveLoginPassword(Context context, String trollNumber, String trollPassword) {
        String checkedTrollNumber = Strings.nullToEmpty(trollNumber).trim();
        String checkedTrollPassword = Strings.nullToEmpty(trollPassword).trim();

        if (Strings.isNullOrEmpty(checkedTrollNumber) || Strings.isNullOrEmpty(checkedTrollPassword)) {
            return false;
        }

        String hashedTrollPassword = MhDlaNotifierUtils.md5(checkedTrollPassword);
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PROPERTY_TROLL_ID, checkedTrollNumber);
        editor.putString(PROPERTY_TROLL_PASSWORD, hashedTrollPassword);
        editor.commit();

        return true;
    }

    public static Date getDLA(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
        String string = preferences.getString(PROPERTY_DLA, null);
        Date result = MhDlaNotifierUtils.parseDate(string);
        return result;
    }

    public static Integer getPA(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
        String string = preferences.getString(PROPERTY_PA_RESTANT, null);
        Integer result = null;
        try {
            result = Integer.parseInt(string);
        } catch (Exception eee) {
            // Nothing to do
        }
        return result;
    }
}
