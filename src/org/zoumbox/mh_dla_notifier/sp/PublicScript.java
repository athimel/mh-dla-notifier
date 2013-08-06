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
package org.zoumbox.mh_dla_notifier.sp;

import java.util.List;

import com.google.common.collect.Lists;

import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.A_TERRE;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.BLASON;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.CAMOU;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.CARACT;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.DATE_INSCRIPTION;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.DLA;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.DUREE_DU_TOUR;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.EN_COURSE;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.FATIGUE;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.GUILDE;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.IMMOBILE;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.INTANGIBLE;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.INVISIBLE;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.LEVITATION;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.MONSTRES;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.NB_KILLS;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.NB_MORTS;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.NIVAL;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.NOM;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.PA_RESTANT;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.POS_N;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.POS_X;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.POS_Y;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.PV;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.PV_MAX;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.RACE;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.TROLLS;

/**
 * @author Arno <arno@zoumbox.org>
 */
public enum PublicScript {

    Profil2(
            ScriptCategory.DYNAMIC,
            "http://sp.mountyhall.com/SP_Profil2.php?Numero=%s&Motdepasse=%s",
            Lists.newArrayList(
                    "numero",
                    POS_X.name(),
                    POS_Y.name(),
                    POS_N.name(),
                    PV.name(),
                    PV_MAX.name(),
                    PA_RESTANT.name(),
                    DLA.name(),
                    "attaque",
                    "esquive",
                    "degats",
                    "regeneration",
                    "vue",
                    "armure",
                    "mm",
                    "rm",
                    "attaquesSubies",
                    FATIGUE.name(),
                    CAMOU.name(),
                    INVISIBLE.name(),
                    INTANGIBLE.name(),
                    "nbParadeProgrammes",
                    "nbContreAttaquesProgrammes",
                    DUREE_DU_TOUR.name(),
                    "bonusDuree",
                    "armureNaturelle",
                    "desDArmureEnMoins",
                    IMMOBILE.name(),
                    A_TERRE.name(),
                    EN_COURSE.name(),
                    LEVITATION.name()
            )
    ),

//    Profil3(
//            ScriptCategory.DYNAMIC,
//            "http://sp.mountyhall.com/SP_Profil3.php?Numero=%s&Motdepasse=%s",
//            Lists.newArrayList("numero", NOM.name(), POS_X.name(), POS_Y.name(), POS_N.name(), PA_RESTANT.name(), DLA.name(), "fatigue", CAMOU.name(), INVISIBLE.name(), INTANGIBLE.name(), "px", "pxPerso", "pi", "gg")),

//    BonusMalus(
//            ScriptCategory.DYNAMIC,
//            "http://sp.mountyhall.com/SP_Bonusmalus.php?Numero=%s&Motdepasse=%s",
//            Lists.newArrayList(BONUS_MALUS.name())),
//
//    Aptitudes2(
//            ScriptCategory.DYNAMIC,
//            "http://sp.mountyhall.com/SP_Aptitudes2.php?Numero=%s&Motdepasse=%s",
//            Lists.newArrayList(APTITUDES.name())),

    ProfilPublic2(
            ScriptCategory.STATIC,
            "http://sp.mountyhall.com/SP_ProfilPublic2.php?Numero=%s&Motdepasse=%s",
            Lists.newArrayList(
                    "numero",
                    NOM.name(),
                    RACE.name(),
                    NIVAL.name(),
                    DATE_INSCRIPTION.name(),
                    "email",
                    BLASON.name(),
                    "nbMouches",
                    NB_KILLS.name(),
                    NB_MORTS.name(),
                    GUILDE.name(),
                    "niveauDeRang",
                    "pnj"
            )
    ),

//    Equipement(
//            ScriptCategory.STATIC,
//            "http://sp.mountyhall.com/SP_Equipement.php?Numero=%s&Motdepasse=%s",
//            Lists.newArrayList(EQUIPEMENT.name())),
//
//    Mouche(
//            ScriptCategory.STATIC,
//            "http://sp.mountyhall.com/SP_Mouche.php?Numero=%s&Motdepasse=%s",
//            Lists.newArrayList(MOUCHES.name()));

    Caract(
            ScriptCategory.DYNAMIC,
            "http://sp.mountyhall.com/SP_Caract.php?Numero=%s&Motdepasse=%s",
            Lists.newArrayList(
                    CARACT.name()
            )
    ),

    Vue(
            ScriptCategory.DYNAMIC,
            "http://sp.mountyhall.com/SP_Vue2.php?Numero=%s&Motdepasse=%s",
            Lists.newArrayList(
                    TROLLS.name(),
                    MONSTRES.name()
            )
    );


    protected ScriptCategory category;
    protected String url;
    protected List<String> properties;

    private PublicScript(ScriptCategory category, String url, List<String> properties) {
        this.category = category;
        this.url = url;
        this.properties = properties;
    }

    public static PublicScript forProperty(PublicScriptProperties property) {
        for (PublicScript script : values()) {
            if (script.properties.contains(property.name())) {
                return script;
            }
        }
        throw new IllegalStateException("Unmapped property: " + property);
    }

    public ScriptCategory getCategory() {
        return category;
    }
}
