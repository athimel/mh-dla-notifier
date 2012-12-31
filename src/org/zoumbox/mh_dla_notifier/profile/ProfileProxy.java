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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.zoumbox.mh_dla_notifier.Constants;
import org.zoumbox.mh_dla_notifier.MhDlaNotifierUtils;
import org.zoumbox.mh_dla_notifier.Pair;
import org.zoumbox.mh_dla_notifier.Triple;
import org.zoumbox.mh_dla_notifier.sp.NetworkUnavailableException;
import org.zoumbox.mh_dla_notifier.sp.PublicScript;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptException;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptsProxy;
import org.zoumbox.mh_dla_notifier.sp.QuotaExceededException;
import org.zoumbox.mh_dla_notifier.sp.ScriptCategory;

import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.BLASON;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.CAMOU;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.CARACT;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.DLA;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.DUREE_DU_TOUR;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.FATIGUE;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.GUILDE;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.INTANGIBLE;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.INVISIBLE;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.NB_KILLS;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.NB_MORTS;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.NEEDS_UPDATE;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.NIVAL;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.NOM;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.PA_RESTANT;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.POS_N;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.POS_X;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.POS_Y;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.PV;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.PV_MAX;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.RACE;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class ProfileProxy {

    private static final String TAG = Constants.LOG_PREFIX + ProfileProxy.class.getSimpleName();

    public static final String PREFS_NAME = "org.zoumbox.mh.dla.notifier.preferences";

    public static final String PROPERTY_TROLL_ID = "trollId";
    public static final String PROPERTY_TROLL_PASSWORD = "trollPassword";

    public static boolean needsUpdate(Context context, PublicScript script, String trollNumber) {
        Date lastUpdate = PublicScriptsProxy.geLastUpdate(context, script, trollNumber);
        if (lastUpdate == null) {
            return true;
        } else {
            int dailyQuota = GET_USABLE_QUOTA.apply(script.category);
            int minutesDelay = 24 * 60 / dailyQuota;
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, -minutesDelay);
            Date delay = calendar.getTime();
            boolean result = lastUpdate.before(delay);
            return result;
        }
    }

    public static final Function<ScriptCategory, Integer> GET_USABLE_QUOTA = new Function<ScriptCategory, Integer>() {
        @Override
        public Integer apply(@Nullable ScriptCategory input) {
            if (input == null) {
                return 1;
            }
            int dailyQuota = input.getQuota();
            int result = dailyQuota / 3; //FIXME AThimel 29/03/2012 divide by 3 for the moment to avoid mistakes
            return result;
        }
    };

    public static String getTrollNumber(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        String trollNumber = preferences.getString(PROPERTY_TROLL_ID, null);
        return trollNumber;
    }

    public static Troll fetchTroll(final Context context, UpdateRequestType updateRequest) throws QuotaExceededException, MissingLoginPasswordException, PublicScriptException, NetworkUnavailableException {

        Troll result = new Troll();

        List<PublicScriptProperties> requestedProperties = Lists.newArrayList(NOM, RACE, NIVAL, PV, PV_MAX, FATIGUE, POS_X, POS_Y, POS_N,
                CAMOU, INVISIBLE, INTANGIBLE, DUREE_DU_TOUR,
                DLA, PA_RESTANT, BLASON, NB_KILLS, NB_MORTS, GUILDE,
                CARACT);
        Map<PublicScriptProperties, String> properties = ProfileProxy.fetchProperties(context, updateRequest, requestedProperties);

        result.id = getTrollNumber(context);

        result.nom = properties.get(NOM);
        result.race = Race.valueOf(properties.get(RACE));
        result.nival = Integer.parseInt(properties.get(NIVAL));

        result.pv = Integer.parseInt(properties.get(PV));
        result.pvMaxBase = Integer.parseInt(properties.get(PV_MAX));
        result.fatigue = Integer.parseInt(properties.get(FATIGUE));

        result.posX = Integer.parseInt(properties.get(POS_X));
        result.posY = Integer.parseInt(properties.get(POS_Y));
        result.posN = Integer.parseInt(properties.get(POS_N));

        result.camou = "1".equals(properties.get(CAMOU));
        result.invisible = "1".equals(properties.get(INVISIBLE));
        result.intangible = "1".equals(properties.get(INTANGIBLE));

        result.dureeDuTour = Integer.parseInt(properties.get(DUREE_DU_TOUR));
        result.dla = MhDlaNotifierUtils.parseDate(properties.get(DLA));
        result.pa = Integer.parseInt(properties.get(PA_RESTANT));

        result.blason = properties.get(BLASON);
        String guildeNumber = properties.get(GUILDE);
        if (!Strings.isNullOrEmpty(guildeNumber)) {
            result.guilde = Integer.parseInt(guildeNumber);
        }
        result.nbKills = Integer.parseInt(properties.get(NB_KILLS));
        result.nbMorts = Integer.parseInt(properties.get(NB_MORTS));

//        result.mouches = Lists.newArrayList();
//        List<String> moucheLines = Lists.newArrayList(Splitter.on("\n").omitEmptyStrings().trimResults().split(properties.get(MOUCHES)));
//        for (String line : moucheLines) {
//            List<String> fields = Lists.newArrayList(Splitter.on(";").split(line));
//            Mouche mouche = new Mouche();
//            mouche.id = fields.get(0);
//            mouche.nom = fields.get(1);
//            mouche.type = MoucheType.valueOf(fields.get(2));
//            mouche.age = Integer.parseInt(fields.get(3));
//            mouche.presente = "LA".equals(fields.get(4));
//
//            result.mouches.add(mouche);
//        }

//        result.equipements = Lists.newArrayList();
//        List<String> equipementLines = Lists.newArrayList(Splitter.on("\n").omitEmptyStrings().trimResults().split(properties.get(EQUIPEMENT)));
//        for (String line : equipementLines) {
//            List<String> fields = Lists.newArrayList(Splitter.on(";").split(line));
//            Equipement equipement = new Equipement();
//            equipement.id = fields.get(0);
//            equipement.emplacement = Integer.parseInt(fields.get(1));
//            equipement.type = EquipementType.fromType(fields.get(2));
//            equipement.identified = "1".equals(fields.get(3));
//            equipement.nom = fields.get(4);
//            equipement.magie = fields.get(5);
//            equipement.description = fields.get(6);
//            equipement.poids = Double.parseDouble(fields.get(7));
//
//            result.equipements.add(equipement);
//        }

        List<String> caractLines = Lists.newArrayList(Splitter.on("\n").omitEmptyStrings().trimResults().split(properties.get(CARACT)));
        result.pvBM = 0;
        result.dlaBM = 0;
        result.poids = 0;
        for (String line : caractLines) {
            List<String> fields = Lists.newArrayList(Splitter.on(";").trimResults().split(line));
            String type = fields.get(0);
            if ("BMM".equals(type) || "BMP".equals(type)) {
                result.pvBM += Integer.parseInt(fields.get(5));
                result.dlaBM += Integer.parseInt(fields.get(11));
                Double poids = Math.floor(Double.parseDouble(fields.get(12)));
                result.poids += poids.intValue();
            }
        }

        result.updateRequestType = UpdateRequestType.valueOf(properties.get(NEEDS_UPDATE));

        return result;
    }

    protected static Predicate<PublicScript> noNeedToUpdate(final Context context, final String trollNumber) {
        return new Predicate<PublicScript>() {
            @Override
            public boolean apply(PublicScript script) {
                return !needsUpdate(context, script, trollNumber);
            }
        };
    }

    public static Map<PublicScriptProperties, String> fetchProperties(final Context context, UpdateRequestType updateRequest, List<PublicScriptProperties> requestedProperties) throws QuotaExceededException, MissingLoginPasswordException, PublicScriptException, NetworkUnavailableException {

        Log.i(TAG, "Fetching properties with updateRequest: " + updateRequest);
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        Pair<String, String> idAndPassword = loadIdPassword(preferences);
        final String trollNumber = idAndPassword.left();

        Set<PublicScript> scripts = Sets.newLinkedHashSet();
        Log.i(TAG, "Requesting properties: " + requestedProperties);
        for (PublicScriptProperties property : requestedProperties) {
            PublicScript script = PublicScript.forProperty(property);
            scripts.add(script);
        }

        UpdateRequestType updateRequestType = updateRequest;
        if (!updateRequestType.needUpdate()) {
            for (PublicScriptProperties property : requestedProperties) {
                String value = preferences.getString(property.name(), null);
                if (value == null) {
                    updateRequestType = UpdateRequestType.FULL;
                    Log.i(TAG, "updateRequestType changed from " + updateRequest + " to " + updateRequestType);
                    break;
                }
            }
        }

        UpdateRequestType backgroundUpdate = UpdateRequestType.NONE;
        if (updateRequestType.needUpdate()) {

            if (UpdateRequestType.ONLY_NECESSARY.equals(updateRequestType)) {
                Iterables.removeIf(scripts, noNeedToUpdate(context, trollNumber));
            }

            for (PublicScript type : scripts) {
                Map<String, String> propertiesFetched = PublicScriptsProxy.fetch(context, type, idAndPassword);
                saveProperties(preferences, propertiesFetched);
            }
        } else {
            Iterables.removeIf(scripts, noNeedToUpdate(context, trollNumber));

            if (!scripts.isEmpty()) {
                backgroundUpdate = UpdateRequestType.ONLY_NECESSARY;
            }
        }

        Map<PublicScriptProperties, String> result = Maps.newLinkedHashMap();
        Log.i(TAG, "Background update needed ? " + backgroundUpdate);
        result.put(NEEDS_UPDATE, backgroundUpdate.name());

        for (PublicScriptProperties property : requestedProperties) {
            String value = preferences.getString(property.name(), null);
            result.put(property, value);
        }
        return result;
    }

    protected static void saveProperties(SharedPreferences preferences, Map<String, String> propertiesFetched) {
        SharedPreferences.Editor editor = preferences.edit();
        for (Map.Entry<String, String> prop : propertiesFetched.entrySet()) {
            editor.putString(prop.getKey(), prop.getValue());
        }
        editor.commit();
    }

    public static String loadLogin(Context context) {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        String result = preferences.getString(PROPERTY_TROLL_ID, null);
        return result;
    }

    public static boolean saveIdPassword(Context context, String trollNumber, String trollPassword) {
        String checkedTrollNumber = Strings.nullToEmpty(trollNumber).trim();
        String checkedTrollPassword = Strings.nullToEmpty(trollPassword).trim();

        if (Strings.isNullOrEmpty(checkedTrollNumber) || Strings.isNullOrEmpty(checkedTrollPassword)) {
            return false;
        }

        String hashedTrollPassword = MhDlaNotifierUtils.md5(checkedTrollPassword);
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PROPERTY_TROLL_ID, checkedTrollNumber);
        editor.putString(PROPERTY_TROLL_PASSWORD, hashedTrollPassword);
        editor.commit();

        return true;
    }

    protected static Pair<String, String> loadIdPassword(SharedPreferences preferences) throws MissingLoginPasswordException {

        if (Constants.mock) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PROPERTY_TROLL_ID, "123456");
            editor.putString(PROPERTY_TROLL_PASSWORD, "*******");
            editor.commit();
        }

        final String trollNumber = preferences.getString(PROPERTY_TROLL_ID, null);
        String trollPassword = preferences.getString(PROPERTY_TROLL_PASSWORD, null);
        if (Strings.isNullOrEmpty(trollNumber) || Strings.isNullOrEmpty(trollPassword)) {
            throw new MissingLoginPasswordException();
        }

        Pair<String, String> result = new Pair<String, String>(trollNumber, trollPassword);
        return result;
    }

    public static Triple<Date, Integer, Date> refreshDLA(Context context) throws MissingLoginPasswordException, PublicScriptException, NetworkUnavailableException, QuotaExceededException {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        Pair<String, String> idAndPassword = loadIdPassword(preferences);

        Map<String, String> propertiesFetched;
        propertiesFetched = PublicScriptsProxy.fetch(context, PublicScript.Profil2, idAndPassword);
        saveProperties(preferences, propertiesFetched);

        Troll troll = fetchTroll(context, UpdateRequestType.NONE);

        Date currentDla = troll.dla;
        Integer pa = troll.pa;
        Date nextDLA = Troll.GET_NEXT_DLA.apply(troll);

        Triple<Date, Integer, Date> result = new Triple<Date, Integer, Date>(currentDla, pa, nextDLA);
        return result;
    }

    private static Date getDLA(SharedPreferences preferences) {
        String string = preferences.getString(DLA.name(), null);
        Date result = MhDlaNotifierUtils.parseDate(string);
        return result;
    }

    public static Date getDLA(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
        Date result = getDLA(preferences);
        return result;
    }

    private static Integer getPA(SharedPreferences preferences) {
        String string = preferences.getString(PA_RESTANT.name(), null);
        Integer result = null;
        try {
            result = Integer.parseInt(string);
        } catch (Exception eee) {
            // Nothing to do
        }
        return result;
    }

}
