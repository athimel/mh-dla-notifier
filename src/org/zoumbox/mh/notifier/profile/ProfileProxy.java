package org.zoumbox.mh.notifier.profile;

import android.content.SharedPreferences;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.zoumbox.mh.notifier.sp.MHPublicScriptsProxy;
import org.zoumbox.mh.notifier.sp.PublicScript;
import org.zoumbox.mh.notifier.sp.QuotaExceededException;

import java.util.Map;
import java.util.Set;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class ProfileProxy {

    protected static Map<String, PublicScript> properties = Maps.newHashMap();
    static {
        properties.put("nom", PublicScript.ProfilPublic2);
        properties.put("race", PublicScript.ProfilPublic2);
        properties.put("niveau", PublicScript.ProfilPublic2);
        properties.put("dateInscription", PublicScript.ProfilPublic2);
        properties.put("email", PublicScript.ProfilPublic2);
        properties.put("blason", PublicScript.ProfilPublic2);
        properties.put("nbMouches", PublicScript.ProfilPublic2);
        properties.put("nbKill", PublicScript.ProfilPublic2);
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

    public Map<String, String> fetchProperties(SharedPreferences preferences, String... names) throws QuotaExceededException, MissingLoginPasswordException {
        Set<PublicScript> scripts = Sets.newLinkedHashSet();
        for (String propertyName : names) {
            if (!preferences.contains(propertyName)) {
                PublicScript script = properties.get(propertyName);
                if (script == null) {
                    System.out.println("Unknown property: " + propertyName);
                } else {
                    scripts.add(script);
                }
            }
        }

        if (!scripts.isEmpty()) {
            String trollNumber = preferences.getString("trollId", null);
            String trollPassword = preferences.getString("trollPassword", null);
            if (Strings.isNullOrEmpty(trollNumber) || Strings.isNullOrEmpty(trollPassword)) {
                throw new MissingLoginPasswordException();
            }

            for (PublicScript type : scripts) {
                Map<String, String> propertiesFetched = MHPublicScriptsProxy.fetch(type, trollNumber, trollPassword, false);
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
}
