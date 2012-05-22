/*
 * #%L
 * MountyHall DLA Notifier
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2012 Zoumbox.org
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
package org.zoumbox.mh_dla_notifier.sp;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author Arno <arno@zoumbox.org>
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
            Lists.newArrayList("numero", "nom", "race", "niveau", "dateInscription", "email", "blason", "nbMouches", "nbKills", "nbMorts", "numeroDeGuilde", "niveauDeRang", "pnj")),

    Mouche(
            ScriptCategory.STATIC,
            "http://sp.mountyhall.com/SP_Mouche.php?Numero=%s&Motdepasse=%s",
//            Lists.newArrayList("moucheId", "moucheNom", "moucheType", "moucheAge", "mouchePresence")
            Lists.newArrayList("nbTelaites")
    );

    public ScriptCategory category;
    protected String url;
    protected List<String> properties;

    private PublicScript(ScriptCategory category, String url, List<String> properties) {
        this.category = category;
        this.url = url;
        this.properties = properties;
    }

}
