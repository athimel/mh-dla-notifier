/*
 * #%L
 * MountyHall DLA Notifier
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2012 - 2014 Zoumbox.org
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
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author Arno <arno@zoumbox.org>
 */
public enum PublicScript {

    ProfilPublic2(
            ScriptCategory.STATIC,
            "http://sp.mountyhall.com/SP_ProfilPublic2.php?Numero=%s&Motdepasse=%s",
            Lists.newArrayList(
                    "numero",
                    "nom",
                    "race",
                    "nival",
                    "dateInscription",
                    "email",
                    "blason",
                    "nbMouches",
                    "nbKills",
                    "nbMorts",
                    "guilde",
                    "niveauDeRang",
                    "pnj"
            ),
            null
    ),

    Profil2(
            ScriptCategory.DYNAMIC,
            "http://sp.mountyhall.com/SP_Profil2.php?Numero=%s&Motdepasse=%s",
            Lists.newArrayList(
                    "numero",
                    "posX",
                    "posY",
                    "posN",
                    "pvActuelsCar",
                    "pvMaxCar",
                    "pa",
                    "dla",
                    "attaqueCar",
                    "esquiveCar",
                    "degatsCar",
                    "regenerationCar",
                    "vueCar",
                    "armureBmp",
                    "mmCar",
                    "rmCar",
                    "attaquesSubies",
                    "fatigue",
                    "camou",
                    "invisible",
                    "intangible",
                    "nbParadeProgrammes",
                    "nbContreAttaquesProgrammes",
                    "dureeDuTourCar",
                    "bonusDuree",
                    "armureCar",
                    "desDArmureEnMoins",
                    "immobile",
                    "aTerre",
                    "enCourse",
                    "levitation"
            ),
            null
    ),

    Caract(
            ScriptCategory.DYNAMIC,
            "http://sp.mountyhall.com/SP_Caract.php?Numero=%s&Motdepasse=%s",
            // http://dev.zoumbox.org/issues/161 : 7th value is supposed to be pvActuels but is bogus in SP (reported by email on april 10th 2014)
            Lists.newArrayList(
                    "type", "attaque", "esquive", "degats", "regeneration", "pvMax", "pvActuelsFake", "vue", "rm", "mm", "armure", "dureeDuTour", "poids", "concentration"
            ),
            Sets.newHashSet("BMM", "BMP", "CAR")
    ),

//    Profil3(
//            ScriptCategory.DYNAMIC,
//            "http://sp.mountyhall.com/SP_Profil3.php?Numero=%s&Motdepasse=%s",
//            Lists.newArrayList("numero", NOM", POS_X", POS_Y", POS_N", PA_RESTANT", DLA", "fatigue", CAMOU", INVISIBLE", INTANGIBLE", "px", "pxPerso", "pi", "gg")),

//    BonusMalus(
//            ScriptCategory.DYNAMIC,
//            "http://sp.mountyhall.com/SP_Bonusmalus.php?Numero=%s&Motdepasse=%s",
//            Lists.newArrayList(BONUS_MALUS")),
//
//    Aptitudes2(
//            ScriptCategory.DYNAMIC,
//            "http://sp.mountyhall.com/SP_Aptitudes2.php?Numero=%s&Motdepasse=%s",
//            Lists.newArrayList(APTITUDES")),

//    Equipement(
//            ScriptCategory.STATIC,
//            "http://sp.mountyhall.com/SP_Equipement.php?Numero=%s&Motdepasse=%s",
//            Lists.newArrayList(EQUIPEMENT")),
//
//    Mouche(
//            ScriptCategory.STATIC,
//            "http://sp.mountyhall.com/SP_Mouche.php?Numero=%s&Motdepasse=%s",
//            Lists.newArrayList(MOUCHES"));

//    Vue(
//            ScriptCategory.DYNAMIC,
//            "http://sp.mountyhall.com/SP_Vue2.php?Numero=%s&Motdepasse=%s",
//            Lists.newArrayList(
//                    "TROLLS",
//                    "MONSTRES"
//            ),
//            null
//    );
    ;

    protected ScriptCategory category;
    protected String url;
    protected List<String> properties;
    protected Set<String> types;

    private PublicScript(ScriptCategory category, String url, List<String> properties, Set<String> types) {
        this.category = category;
        this.url = url;
        this.properties = properties;
        this.types = types;
    }

    @Deprecated
    public static PublicScript forProperty(String name) {
        for (PublicScript script : values()) {
            if (script.properties.contains(name)) {
                return script;
            }
        }
        throw new IllegalStateException("Unmapped property: " + name);
    }

    public ScriptCategory getCategory() {
        return category;
    }

    public Set<String> getTypes() {
        return types;
    }

    @Override
    public String toString() {
        return "PublicScript{" +
                "name=" + name() +
                ", category=" + category +
                '}';
    }
}
