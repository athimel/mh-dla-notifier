package org.zoumbox.mh.notifier.profile;

import android.content.SharedPreferences;
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

    SharedPreferences preferences;

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

    public void fetchProperties(String ... names) throws QuotaExceededException {
        Set<PublicScript> result = Sets.newLinkedHashSet();
        for (String propertyName : names) {
            if (!preferences.contains(propertyName)) {
                result.add(properties.get(propertyName));
            }
        }

        for (PublicScript type : result) {
            MHPublicScriptsProxy.fetch(type, "", "", false);
        }
    }
}
