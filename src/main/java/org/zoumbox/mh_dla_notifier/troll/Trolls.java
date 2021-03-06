package org.zoumbox.mh_dla_notifier.troll;

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

import java.util.Calendar;
import java.util.Date;

import org.zoumbox.mh_dla_notifier.MhDlaNotifierUtils;

import com.google.common.base.Function;

import android.content.Context;

/**
 * @author Arnaud Thimel <a.thimel at gmail.com>
 */
public class Trolls {

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
            double nextFatigue = fatigue.doubleValue() / 1.25;
            Double resultRounded = Math.floor(nextFatigue);
            int result = resultRounded.intValue();
            return result;
        }
    };

    public static final Function<Troll, Integer> GET_MAX_PV = new Function<Troll, Integer>() {
        @Override
        public Integer apply(Troll troll) {
            int result = troll.pvMaxCar + troll.pvMaxBmm + troll.pvMaxBmp;
            return result;
        }
    };

    public static final Function<Troll, Double> GET_POIDS = new Function<Troll, Double>() {
        @Override
        public Double apply(Troll troll) {
            double result = troll.poidsCar + troll.poidsBmm + troll.poidsBmp;
            return result;
        }
    };

    public static final Function<Troll, Double> GET_DUREE_DU_TOUR = new Function<Troll, Double>() {
        @Override
        public Double apply(Troll troll) {
            double result = troll.dureeDuTourCar + troll.dureeDuTourBmm + troll.dureeDuTourBmp;
            return result;
        }
    };

    // Lorsque vous êtes blessé, chaque point de vie en moins vous donnera un malus de DLA de (250 / PV max) minutes.
    public static final Function<Troll, Integer> GET_PV_DLA_MALUS = new Function<Troll, Integer>() {
        @Override
        public Integer apply(Troll troll) {
            int pvMax = GET_MAX_PV.apply(troll);
            int result = 0;
            if (pvMax > 0) {
                // La règle est : 250 min./ nb PV total x PV manquants
                // source (http://mountypedia.mountyhall.com/Mountyhall/PV)
                double value = 250d / pvMax;
                value = Math.floor(value * 100d) / 100d; // On arrondi à 2 nombres après la virgule
                int pvManquants = pvMax - troll.pvActuelsCar;
                value = value * pvManquants;
                result = Double.valueOf(value).intValue();
            }
            return result;
        }
    };

//    public static void main(String[] args) {
//
//        {
//            Troll troll = new Troll();
//            troll.setPvMaxCar(115);
//            troll.setPvActuelsCar(92);
//
//            Integer integer = GET_PV_DLA_MALUS.apply(troll);
//            System.out.println(integer); // 49 espéré
//        }
//
//        {
//            Troll troll = new Troll();
//            troll.setPvMaxCar(100);
//            troll.setPvActuelsCar(50);
//
//            Integer integer = GET_PV_DLA_MALUS.apply(troll);
//            System.out.println(integer); // 125 espéré
//        }
//
//        {
//            Troll troll = new Troll();
//            troll.setPvMaxCar(150);
//            troll.setPvActuelsCar(100);
//
//            Integer integer = GET_PV_DLA_MALUS.apply(troll);
//            System.out.println(integer); // 83 espéré
//        }
//
//    }

    public static final Function<Troll, Integer> GET_NEXT_DLA_DURATION = new Function<Troll, Integer>() {
        @Override
        public Integer apply(Troll troll) {
            // Duree de base du tour (585) + poids (125) + bonus magique (-130) + malus blessure (~120)
            int dlaPVMalus = GET_PV_DLA_MALUS.apply(troll);
            Double dureeDuTour = GET_DUREE_DU_TOUR.apply(troll);
            Double poids = GET_POIDS.apply(troll);
            int computed = dureeDuTour.intValue() + poids.intValue() + dlaPVMalus;
            int result = Math.max(computed, Double.valueOf(troll.dureeDuTourCar).intValue());
            return result;
        }
    };

    public static final Function<Troll, Date> GET_NEXT_DLA = new Function<Troll, Date>() {
        @Override
        public Date apply(Troll troll) {

            int nextDlaDuration = GET_NEXT_DLA_DURATION.apply(troll);

            Date result = null;
            Calendar nextDla = Calendar.getInstance();
            if (troll.dla != null) {
                nextDla.setTime(troll.dla);
                nextDla.add(Calendar.MINUTE, nextDlaDuration);
                result = nextDla.getTime();
            }
            return result;
        }
    };

    public static Function<Troll, String> getWidgetDlaTextFunction(final Context context) {
        Function<Troll, String> result = new Function<Troll, String>() {
            @Override
            public String apply(Troll troll) {

                String result;
                Date dla = troll.getDla();
                if (MhDlaNotifierUtils.IS_IN_THE_FUTURE.apply(dla)) {
                    result = MhDlaNotifierUtils.formatHourNoSecondsForDisplay(context, dla);
                    result += "/" + troll.getPa() + "PA";
                } else {
                    Date nextDla = GET_NEXT_DLA.apply(troll);
                    result = String.format("(%s)", MhDlaNotifierUtils.formatHourNoSecondsForDisplay(context, nextDla));
                }
                return result;
            }
        };
        return result;
    }


//    public static final Function<Mouche,MoucheType> GET_MOUCHE_TYPE = new Function<Mouche, MoucheType>() {
//        @Override
//        public MoucheType apply(Mouche mouche) {
//            return mouche.type;
//        }
//    };

}
