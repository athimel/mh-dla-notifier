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
package org.zoumbox.mh_dla_notifier.profile;

import com.google.common.base.Function;

import java.util.Date;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class Troll {

//    public static final Function<Mouche,MoucheType> GET_MOUCHE_TYPE = new Function<Mouche, MoucheType>() {
//        @Override
//        public MoucheType apply(Mouche mouche) {
//            return mouche.type;
//        }
//    };

    public String id, nom;
    public Race race;
    public int nival;
    public int pv, pvMaxBase;
    public int fatigue;
    public int posX, posY, posN;
    public boolean camou, invisible, intangible;
    public int dureeDuTour;
    public Date dla;
    public int pa;
    public String blason;
    public int nbKills, nbMorts;
    public int guilde;

    public int pvBM;
    public int dlaBM;
    public int poids;

    public UpdateRequestType updateRequestType;

    // Computed
    int pvMax = -1;

    public int getPvMax() {
        if (pvMax == -1) {
            pvMax = pvMaxBase + pvBM;
        }
        return pvMax;
    }

    // Gain en minutes par PV sacrifié = 120 / ( Fatigue * (1 + Arrondi.Inférieur(Fatigue / 10) ) ) minutes. (arrondi inférieur)
    public static final Function<Integer, Integer> GET_DLA_GAIN_BY_PV = new Function<Integer, Integer>() {
        @Override
        public Integer apply(Integer fatigue) {
            int result = 30;
            if (fatigue > 4) {
                result = 120 / (fatigue * (1 + fatigue / 10));
            }
            return result;
        }
    };

    // Au début de chaque DLA (Date Limite d'Action), ce compteur est divisé par 1,25 (arrondi à l'inférieur). Le gain en minutes par PV sacrifié est calculé en fonction de ce compteur, si celui-ci est supérieur à 4, sinon le gain est toujours de 30 minutes par PV sacrifié.
    public static final Function<Integer, Integer> GET_NEXT_FATIGUE = new Function<Integer, Integer>() {
        @Override
        public Integer apply(Integer fatigue) {
            double result = fatigue.doubleValue() / 1.25;
            Double resultRounded = Math.floor(result);
            int resultRoundedToInt = resultRounded.intValue();
            return resultRoundedToInt;
        }
    };

    // Lorsque vous êtes blessé, chaque point de vie en moins vous donnera un malus de DLA de (250 / PV max) minutes.
    public static final Function<Troll, Integer> GET_PV_DLA_MALUS = new Function<Troll, Integer>() {
        @Override
        public Integer apply(Troll troll) {
            int pvMax = troll.getPvMax();
            int result = 0;
            if (pvMax > 0) {
                result = (250 * (pvMax - troll.pv) / pvMax);
            }
            return result;
        }
    };

    public static final Function<Troll, Integer> GET_NEXT_DLA_DURATION = new Function<Troll, Integer>() {
        @Override
        public Integer apply(Troll troll) {
            // Duree de base du tour (585) + poids (125) + bonus magique (-130) + malus blessure (~120)
            int dlaPVMalus = GET_PV_DLA_MALUS.apply(troll);
            int computed = troll.dureeDuTour + troll.poids + troll.dlaBM + dlaPVMalus;
            int result = Math.max(computed, troll.dureeDuTour);
            return result;
        }
    };

}
