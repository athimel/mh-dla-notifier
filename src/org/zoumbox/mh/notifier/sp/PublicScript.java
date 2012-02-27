package org.zoumbox.mh.notifier.sp;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public enum PublicScript {

    Profil2(
            ScriptCategory.DYNAMIC,
            "http://sp.mountyhall.com/SP_Profil2.php?Numero=%s&Motdepasse=%s",
            Lists.newArrayList("numero", "posX", "posY", "posN", "pv", "pvMax", "paRestant", "dla", "attaque", "esquive", "degats", "regeneration", "vue", "armure", "mm", "rm", "attaquesSubies", "fatigue", "camou", "invisible", "intangible", "nbParadeProgrammes", "nbContreAttaquesProgrammes", "dureeDuTour", "bonusDuree", "armureNaturelle", "desDArmureEnMoins")),

    Profil3(
            ScriptCategory.DYNAMIC,
            "http://sp.mountyhall.com/SP_Profil3.php?Numero=%s&Motdepasse=%s",
            Lists.newArrayList("numero", "nom", "posX", "posY", "posN", "paRestant", "dla", "fatigue", "camou", "invisible", "intangible", "px", "pxPerso", "pi", "gg")),

    ProfilPublic2(
            ScriptCategory.STATIC,
            "http://sp.mountyhall.com/SP_ProfilPublic2.php?Numero=%s&Motdepasse=%s",
            Lists.newArrayList("numero", "nom", "race", "niveau", "dateInscription", "email", "blason", "nbMouches", "nbKills", "nbMorts", "numeroDeGuilde", "niveauDeRang", "pnj"));

    protected ScriptCategory category;
    protected String url;
    protected List<String> properties;

    private PublicScript(ScriptCategory category, String url, List<String> properties) {
        this.category = category;
        this.url = url;
        this.properties = properties;
    }

}
