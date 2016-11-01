package org.zoumbox.mh_dla_notifier.sp;

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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zoumbox.mh_dla_notifier.MhDlaNotifierConstants;
import org.zoumbox.mh_dla_notifier.MhDlaNotifierUtils;
import org.zoumbox.mh_dla_notifier.troll.Race;
import org.zoumbox.mh_dla_notifier.troll.Troll;
import org.zoumbox.mh_dla_notifier.utils.LogCallback;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author Arnaud Thimel <a.thimel at gmail.com>
 */
public class PublicScripts {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + PublicScripts.class.getSimpleName();

    protected static final Function<PublicScriptResult, Map<String, String>> SCRIPT_RESULT_TO_MAP = new Function<PublicScriptResult, Map<String, String>>() {
        @Override
        public Map<String, String> apply(PublicScriptResult input) {
            PublicScript script = input.getScript();
            List<String> lines = Lists.newArrayList(Splitter.on("\n").trimResults().omitEmptyStrings().split(input.getRaw()));
            Map<String, String> result = new LinkedHashMap<String, String>();
            Set<String> types = script.getTypes();

            for (String line : lines) {
                Iterable<String> iterable = Splitter.on(";").trimResults().split(line); // Do not omit empty strings
                List<String> data = Lists.newArrayList(iterable);
                String suffix = "";

                if (types != null && !types.isEmpty()) {
                    final String typeToFind = data.get(0);
                    Optional<String> optional = Iterables.tryFind(types, Predicates.equalTo(typeToFind));
                    if (optional.isPresent()) {
                        suffix = typeToFind;
                    }
                }

                for (int i = 0; i < data.size() && i < script.properties.size(); i++) {
                    String key = script.properties.get(i);
                    if (!Strings.isNullOrEmpty(suffix)) {
                        key = key + suffix.substring(0, 1).toUpperCase() + suffix.substring(1).toLowerCase();
                    }
                    String value = data.get(i);
                    result.put(key, value);
                }
            }
//            case Vue:
//                int monstresStart = lines.indexOf("#DEBUT MONSTRES");
//                int monstresEnd = lines.indexOf("#FIN MONSTRES");
//                List<String> monstresList = lines.subList(monstresStart + 1, monstresEnd);
//                String monstres = Joiner.on("\n").join(monstresList);
//                result.put(PublicScriptProperties.MONSTRES.name(), monstres);
//
//                int trollsStart = lines.indexOf("#DEBUT TROLLS");
//                int trollsEnd = lines.indexOf("#FIN TROLLS");
//                List<String> trollsList = lines.subList(trollsStart + 1, trollsEnd);
//                String trolls = Joiner.on("\n").join(trollsList);
//                result.put(PublicScriptProperties.TROLLS.name(), trolls);
//                break;
            return result;
        }
    };

    protected static String escapeName(String rawName) {
        return rawName.replaceAll("\\\\'", "\\'");
    }

    public static void pushToTroll(Troll troll, Map<String, String> propertiesFetched, LogCallback log) {
        for (Map.Entry<String, String> entry : propertiesFetched.entrySet()) {
            try {
                String name = entry.getKey();
//                log.d(TAG, "Try to set property: " + name);

                String stringValue = entry.getValue();
                if ("numero".equals(name)) { troll.setNumero(stringValue);
                } else if ("nom".equals(name)) { troll.setNom(escapeName(stringValue));
                } else if ("race".equals(name)) { troll.setRace(Race.valueOf(stringValue));
                } else if ("nival".equals(name)) { troll.setNival(Integer.parseInt(stringValue));
                } else if ("dateInscription".equals(name)) { troll.setDateInscription(MhDlaNotifierUtils.parseSpDate(stringValue));
//                } else if ("email".equals(name)) { troll.setEm(stringValue);
                } else if ("blason".equals(name)) { troll.setBlason(stringValue);
                } else if ("nbMouches".equals(name)) { troll.setNbMouches(Integer.parseInt(stringValue));
                } else if ("nbKills".equals(name)) { troll.setNbKills(Integer.parseInt(stringValue));
                } else if ("nbMorts".equals(name)) { troll.setNbMorts(Integer.parseInt(stringValue));
                } else if ("guilde".equals(name)) { troll.setGuilde(Integer.parseInt(stringValue));
//                } else if ("niveauDeRang".equals(name)) { troll.setBlason(stringValue);
//                } else if ("pnj".equals(name)) { troll.setBlason(stringValue);
                } else if ("posX".equals(name)) { troll.setPosX(Integer.parseInt(stringValue));
                } else if ("posY".equals(name)) { troll.setPosY(Integer.parseInt(stringValue));
                } else if ("posN".equals(name)) { troll.setPosN(Integer.parseInt(stringValue));
                } else if ("pa".equals(name)) { troll.setPa(Integer.parseInt(stringValue));
                } else if ("dla".equals(name)) { troll.setDla(MhDlaNotifierUtils.parseSpDate(stringValue));

//                } else if ("attaquesSubies".equals(name)) { troll.setAtt(stringValue);
                } else if ("fatigue".equals(name)) { troll.setFatigue(Integer.parseInt(stringValue));
                } else if ("camou".equals(name)) { troll.setCamou("1".equals(stringValue));
                } else if ("invisible".equals(name)) { troll.setInvisible("1".equals(stringValue));
                } else if ("intangible".equals(name)) { troll.setIntangible("1".equals(stringValue));
//                } else if ("nbParadeProgrammes".equals(name)) { troll.setNb(stringValue);
//                } else if ("nbContreAttaquesProgrammes".equals(name)) { troll.setNb(stringValue);
//                } else if ("bonusDuree".equals(name)) { troll.setBon(stringValue);
//                } else if ("desDArmureEnMoins".equals(name)) { troll.setDe(stringValue);
                } else if ("immobile".equals(name)) { troll.setImmobile("1".equals(stringValue));
                } else if ("aTerre".equals(name)) { troll.setATerre("1".equals(stringValue));
                } else if ("enCourse".equals(name)) { troll.setEnCourse("1".equals(stringValue));
                } else if ("levitation".equals(name)) { troll.setLevitation("1".equals(stringValue));

//                } else if ("typeCar".equals(name)) { troll.setBlason(stringValue);
                } else if ("attaqueCar".equals(name)) { troll.setAttaqueCar(Integer.parseInt(stringValue));
                } else if ("esquiveCar".equals(name)) { troll.setEsquiveCar(Integer.parseInt(stringValue));
                } else if ("degatsCar".equals(name)) { troll.setDegatsCar(Integer.parseInt(stringValue));
                } else if ("regenerationCar".equals(name)) { troll.setRegenerationCar(Integer.parseInt(stringValue));
                } else if ("pvMaxCar".equals(name)) { troll.setPvMaxCar(Integer.parseInt(stringValue));
                } else if ("pvActuelsCar".equals(name)) { troll.setPvActuelsCar(Integer.parseInt(stringValue));
//                } else if ("pvActuelsFakeCar".equals(name)) { troll.setBlason(stringValue);
                } else if ("vueCar".equals(name)) { troll.setVueCar(Integer.parseInt(stringValue));
                } else if ("rmCar".equals(name)) { troll.setRmCar(Integer.parseInt(stringValue));
                } else if ("mmCar".equals(name)) { troll.setMmCar(Integer.parseInt(stringValue));
                } else if ("armureCar".equals(name)) { troll.setArmureCar(Integer.parseInt(stringValue));
                } else if ("dureeDuTourCar".equals(name)) { troll.setDureeDuTourCar(Double.parseDouble(stringValue));
                } else if ("poidsCar".equals(name)) { troll.setPoidsCar(Double.parseDouble(stringValue));
                } else if ("concentrationCar".equals(name)) { troll.setConcentrationCar(Integer.parseInt(stringValue));

//                } else if ("typeBmm".equals(name)) { troll.setTy(stringValue);
                } else if ("attaqueBmm".equals(name)) { troll.setAttaqueBmm(Integer.parseInt(stringValue));
                } else if ("esquiveBmm".equals(name)) { troll.setEsquiveBmm(Integer.parseInt(stringValue));
                } else if ("degatsBmm".equals(name)) { troll.setDegatsBmm(Integer.parseInt(stringValue));
                } else if ("regenerationBmm".equals(name)) { troll.setRegenerationBmm(Integer.parseInt(stringValue));
                } else if ("pvMaxBmm".equals(name)) { troll.setPvMaxBmm(Integer.parseInt(stringValue));
//                } else if ("pvActuelsFakeBmm".equals(name)) { troll.setPvActuelsBmm(stringValue);
                } else if ("vueBmm".equals(name)) { troll.setVueBmm(Integer.parseInt(stringValue));
                } else if ("rmBmm".equals(name)) { troll.setRmBmm(Integer.parseInt(stringValue));
                } else if ("mmBmm".equals(name)) { troll.setMmBmm(Integer.parseInt(stringValue));
                } else if ("armureBmm".equals(name)) { troll.setArmureBmm(Integer.parseInt(stringValue));
                } else if ("dureeDuTourBmm".equals(name)) { troll.setDureeDuTourBmm(Double.parseDouble(stringValue));
                } else if ("poidsBmm".equals(name)) { troll.setPoidsBmm(Double.parseDouble(stringValue));
                } else if ("concentrationBmm".equals(name)) { troll.setConcentrationBmm(Integer.parseInt(stringValue));

//                } else if ("typeBmp".equals(name)) { troll.setT(stringValue);
                } else if ("attaqueBmp".equals(name)) { troll.setAttaqueBmp(Integer.parseInt(stringValue));
                } else if ("esquiveBmp".equals(name)) { troll.setEsquiveBmp(Integer.parseInt(stringValue));
                } else if ("degatsBmp".equals(name)) { troll.setDegatsBmp(Integer.parseInt(stringValue));
                } else if ("regenerationBmp".equals(name)) { troll.setRegenerationBmp(Integer.parseInt(stringValue));
                } else if ("pvMaxBmp".equals(name)) { troll.setPvMaxBmp(Integer.parseInt(stringValue));
//                } else if ("pvActuelsFakeBmp".equals(name)) { troll.setBlason(stringValue);
                } else if ("vueBmp".equals(name)) { troll.setVueBmp(Integer.parseInt(stringValue));
                } else if ("rmBmp".equals(name)) { troll.setRmBmp(Integer.parseInt(stringValue));
                } else if ("mmBmp".equals(name)) { troll.setMmBmp(Integer.parseInt(stringValue));
                } else if ("armureBmp".equals(name)) { troll.setArmureBmp(Integer.parseInt(stringValue));
                } else if ("dureeDuTourBmp".equals(name)) { troll.setDureeDuTourBmp(Double.parseDouble(stringValue));
                } else if ("poidsBmp".equals(name)) { troll.setPoidsBmp(Double.parseDouble(stringValue));
                } else if ("concentrationBmp".equals(name)) { troll.setConcentrationBmp(Integer.parseInt(stringValue));

                } else {
                    log.w(TAG, "Ignored property: " + name);
                }
            } catch (IllegalStateException ise) {
                log.e(TAG, "An exception occured", ise);
            }
        }

    }

    public static void pushToTroll(Troll troll, PublicScriptResult publicScriptResult, LogCallback log) {
        String scriptName = publicScriptResult.getScript().name();
        log.i(TAG, String.format("%s result [raw=%s]", scriptName, publicScriptResult.getRaw()));
        Map<String, String> map = PublicScripts.SCRIPT_RESULT_TO_MAP.apply(publicScriptResult);
        log.i(TAG, String.format("%s result [map=%s]", scriptName, map));
        PublicScripts.pushToTroll(troll, map, log);
        log.i(TAG, String.format("%s result [troll=%s]", scriptName, troll));
    }

}
