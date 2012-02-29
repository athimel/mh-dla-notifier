package org.zoumbox.mh.notifier.profile;

import android.app.Activity;
import android.content.SharedPreferences;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.zoumbox.mh.notifier.sp.MhPublicScriptsProxy;
import org.zoumbox.mh.notifier.sp.PublicScript;
import org.zoumbox.mh.notifier.sp.QuotaExceededException;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class ProfileProxy {

    public static final String PREFS_NAME = "org.zoumbox.mh.dla.notifier.preferences";

    public static final String PROPERTY_TROLL_ID = "trollId";
    public static final String PROPERTY_TROLL_PASSWORD = "trollPassword";

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
        properties.put("paRestant", PublicScript.Profil2);
        properties.put("dla", PublicScript.Profil2);
        properties.put("fatigue", PublicScript.Profil2);
        properties.put("dureeDuTour", PublicScript.Profil2);
        properties.put("bonusDuree", PublicScript.Profil2);

        properties.put("px", PublicScript.Profil3);
        properties.put("pxPerso", PublicScript.Profil3);
        properties.put("pi", PublicScript.Profil3);
        properties.put("gg", PublicScript.Profil3);
    }

    public static boolean needsUpdate(Activity activity, PublicScript script, String trollNumber) {
        Date lastUpdate = MhPublicScriptsProxy.geLastUpdate(activity, script, trollNumber);
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

    public static Map<String, String> fetchProperties(final Activity activity, String... names) throws QuotaExceededException, MissingLoginPasswordException {

        SharedPreferences preferences = activity.getSharedPreferences(PREFS_NAME, 0);

        final String trollNumber = preferences.getString(PROPERTY_TROLL_ID, null);
        String trollPassword = preferences.getString(PROPERTY_TROLL_PASSWORD, null);
        if (Strings.isNullOrEmpty(trollNumber) || Strings.isNullOrEmpty(trollPassword)) {
            throw new MissingLoginPasswordException();
        }

        Set<PublicScript> scripts = Sets.newLinkedHashSet();
        for (String propertyName : names) {
            PublicScript script = properties.get(propertyName);
            if (script == null) {
                System.out.println("Unknown property: " + propertyName);
            } else {
                System.out.println("Missing property: " + propertyName);
                scripts.add(script);
            }
        }

        Iterables.removeIf(scripts, new Predicate<PublicScript>() {
            @Override
            public boolean apply(PublicScript script) {
                return !needsUpdate(activity, script, trollNumber);
            }
        });

        if (!scripts.isEmpty()) {
            for (PublicScript type : scripts) {
                Map<String, String> propertiesFetched = MhPublicScriptsProxy.fetch(activity, type, trollNumber, trollPassword, false);
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

    public static String loadLogin(Activity activity) {

        SharedPreferences preferences = activity.getSharedPreferences(PREFS_NAME, 0);

        String result = preferences.getString(PROPERTY_TROLL_ID, null);

        return result;
    }

    public static boolean saveLoginPassword(Activity activity, String trollNumber, String trollPassword) {
        if (Strings.isNullOrEmpty(trollNumber) || Strings.isNullOrEmpty(trollPassword)) {
            return false;
        }

        SharedPreferences preferences = activity.getSharedPreferences(PREFS_NAME, 0);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PROPERTY_TROLL_ID, trollNumber);
        editor.putString(PROPERTY_TROLL_PASSWORD, trollPassword); // TODO 28/02/2012 AThimel MD5 password
        editor.commit();

        return true;
    }

}
