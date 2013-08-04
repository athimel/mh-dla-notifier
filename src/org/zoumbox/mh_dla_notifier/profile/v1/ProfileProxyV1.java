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
package org.zoumbox.mh_dla_notifier.profile.v1;

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
import org.zoumbox.mh_dla_notifier.MhDlaNotifierConstants;
import org.zoumbox.mh_dla_notifier.MhDlaNotifierUtils;
import org.zoumbox.mh_dla_notifier.Pair;
import org.zoumbox.mh_dla_notifier.PreferencesHolder;
import org.zoumbox.mh_dla_notifier.profile.MissingLoginPasswordException;
import org.zoumbox.mh_dla_notifier.profile.UpdateRequestType;
import org.zoumbox.mh_dla_notifier.sp.NetworkUnavailableException;
import org.zoumbox.mh_dla_notifier.sp.PublicScript;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptException;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptsProxy;
import org.zoumbox.mh_dla_notifier.sp.QuotaExceededException;
import org.zoumbox.mh_dla_notifier.sp.ScriptCategory;
import org.zoumbox.mh_dla_notifier.troll.Race;
import org.zoumbox.mh_dla_notifier.troll.Troll;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.LAST_UPDATE_RESULT;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.LAST_UPDATE_SUCCESS;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.LEVITATION;
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
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.PV_VARIATION;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.RACE;
import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.RESTART_CHECK;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class ProfileProxyV1 {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + ProfileProxyV1.class.getSimpleName();

    public static final String PREFS_NAME = "org.zoumbox.mh.dla.notifier.preferences";

    protected static final String PROPERTY_TROLL_ID = "trollId";
    protected static final String PROPERTY_TROLL_PASSWORD = "trollPassword";

    protected static final Pattern NEW_PASSWORD_PATTERN = Pattern.compile("[0-9A-Z]{8}");
    protected static final Pattern LEGACY_PASSWORD_PATTERN = Pattern.compile("[0-9a-fA-F]{32}");

    public static final Function<ScriptCategory, Integer> GET_USABLE_QUOTA = new Function<ScriptCategory, Integer>() {
        @Override
        public Integer apply(ScriptCategory input) {
            if (input == null) {
                return 1;
            }
            int dailyQuota = input.getQuota();
            int result = dailyQuota / 3; //FIXME AThimel 29/03/2012 divide by 3 for the moment to avoid mistakes
            return result;
        }
    };

    public static boolean shouldUpdate(Context context, PublicScript script, String trollNumber) {
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

    public static String getTrollNumber(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        String trollNumber = preferences.getString(PROPERTY_TROLL_ID, null);
        return trollNumber;
    }

    public static Troll fetchTroll(final Context context, UpdateRequestType updateRequest)
            throws QuotaExceededException, MissingLoginPasswordException, PublicScriptException,
            NetworkUnavailableException {

        Troll result = new Troll();

        List<PublicScriptProperties> requestedProperties = Lists.newArrayList(
                NOM, RACE, NIVAL, GUILDE, CARACT, BLASON, NB_KILLS, NB_MORTS, DATE_INSCRIPTION,
                PV, PV_MAX, FATIGUE, POS_X, POS_Y, POS_N,
                DUREE_DU_TOUR, DLA, PA_RESTANT,
                CAMOU, INVISIBLE, INTANGIBLE, IMMOBILE, A_TERRE, EN_COURSE, LEVITATION);
        Map<PublicScriptProperties, String> properties = fetchProperties(
                context, updateRequest, requestedProperties);

        result.setId(getTrollNumber(context));

        result.setNom(properties.get(NOM));
        result.setRace(Race.valueOf(properties.get(RACE)));
        result.setNival(Integer.parseInt(properties.get(NIVAL)));
        result.setDateInscription(MhDlaNotifierUtils.parseDate(properties.get(DATE_INSCRIPTION)));

        result.setPv(Integer.parseInt(properties.get(PV)));
        result.setPvVariation(Integer.parseInt(properties.get(PV_VARIATION)));
        result.setPvMaxBase(Integer.parseInt(properties.get(PV_MAX)));
        result.setFatigue(Integer.parseInt(properties.get(FATIGUE)));

        result.setPosX(Integer.parseInt(properties.get(POS_X)));
        result.setPosY(Integer.parseInt(properties.get(POS_Y)));
        result.setPosN(Integer.parseInt(properties.get(POS_N)));

        result.setCamou("1".equals(properties.get(CAMOU)));
        result.setInvisible("1".equals(properties.get(INVISIBLE)));
        result.setIntangible("1".equals(properties.get(INTANGIBLE)));
        result.setImmobile("1".equals(properties.get(IMMOBILE)));
        result.setaTerre("1".equals(properties.get(A_TERRE)));
        result.setEnCourse("1".equals(properties.get(EN_COURSE)));
        result.setLevitation("1".equals(properties.get(LEVITATION)));

        result.setDureeDuTour(Integer.parseInt(properties.get(DUREE_DU_TOUR)));
        result.setDla(MhDlaNotifierUtils.parseDate(properties.get(DLA)));
        result.setPa(Integer.parseInt(properties.get(PA_RESTANT)));

        result.setBlason(properties.get(BLASON));
        String guildeNumber = properties.get(GUILDE);
        if (!Strings.isNullOrEmpty(guildeNumber)) {
            result.setGuilde(Integer.parseInt(guildeNumber));
        }
        result.setNbKills(Integer.parseInt(properties.get(NB_KILLS)));
        result.setNbMorts(Integer.parseInt(properties.get(NB_MORTS)));

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
        result.setPvBM(0);
        result.setDlaBM(0);
        result.setPoids(0);
        int pvBm = 0, dlaBm = 0, poids = 0;
        for (String line : caractLines) {
            List<String> fields = Lists.newArrayList(Splitter.on(";").trimResults().split(line));
            String type = fields.get(0);
            if ("BMM".equals(type) || "BMP".equals(type)) {
                pvBm += Integer.parseInt(fields.get(5));
                Double dlaBMDouble = Math.floor(Double.parseDouble(fields.get(11)));
                dlaBm += dlaBMDouble.intValue();
                Double poidsDouble = Math.floor(Double.parseDouble(fields.get(12)));
                poids += poidsDouble.intValue();
            }
        }
        result.setPvBM(pvBm);
        result.setDlaBM(dlaBm);
        result.setPoids(poids);

        result.setUpdateRequestType(UpdateRequestType.valueOf(properties.get(NEEDS_UPDATE)));

        return result;
    }

    protected static Predicate<PublicScript> noNeedToUpdate(final Context context, final String trollNumber) {
        return new Predicate<PublicScript>() {
            @Override
            public boolean apply(PublicScript script) {
                return !shouldUpdate(context, script, trollNumber);
            }
        };
    }

    public static Map<PublicScriptProperties, String> fetchProperties(
            final Context context, UpdateRequestType updateRequest, List<PublicScriptProperties> requestedProperties)
            throws QuotaExceededException, MissingLoginPasswordException, PublicScriptException, NetworkUnavailableException {

        Log.i(TAG, "Fetching properties with updateRequest: " + updateRequest);
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        Pair<String, String> idAndPassword = loadIdPassword(preferences);
        final String trollNumber = idAndPassword.left();

        // Iterate over requested properties to know which SP are concerned
        Set<PublicScript> scripts = Sets.newLinkedHashSet();
        Log.i(TAG, "Requesting properties: " + requestedProperties);
        for (PublicScriptProperties property : requestedProperties) {
            PublicScript script = PublicScript.forProperty(property);
            scripts.add(script);
        }

        // Maybe no update is requested, but needed because of missing property
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
                Predicate<PublicScript> noNeedToUpdatePredicate = noNeedToUpdate(context, trollNumber);
                Iterables.removeIf(scripts, noNeedToUpdatePredicate);
            }

            for (PublicScript script : scripts) {
                Map<String, String> propertiesFetched = PublicScriptsProxy.fetch(context, script, idAndPassword);
                saveProperties(preferences, propertiesFetched);
            }
        } else {
            Predicate<PublicScript> noNeedToUpdatePredicate = noNeedToUpdate(context, trollNumber);
            Iterables.removeIf(scripts, noNeedToUpdatePredicate);

//            if (!scripts.isEmpty()) {
            if (scripts.contains(PublicScript.Profil2)) { // Ask for update only if profil2 needs an update
                backgroundUpdate = UpdateRequestType.ONLY_NECESSARY;
            }
        }

        Map<PublicScriptProperties, String> result = Maps.newLinkedHashMap();
        Log.i(TAG, "Background update needed ? " + backgroundUpdate);
        result.put(NEEDS_UPDATE, backgroundUpdate.name());

        String pvVariation = preferences.getString(PV_VARIATION.name(), "0");
        result.put(PV_VARIATION, pvVariation);

        for (PublicScriptProperties property : requestedProperties) {
            String value = preferences.getString(property.name(), null);
            result.put(property, value);
        }

        return result;
    }

    public static Date getLastUpdateSuccess(final Context context) {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        long lastUpdate = preferences.getLong(LAST_UPDATE_SUCCESS.name(), 0l);

        Date result = null;
        if (lastUpdate > 0l) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(lastUpdate);
            result = calendar.getTime();
        }
        return result;
    }

    public static Long getElapsedSinceLastUpdateSuccess(final Context context) {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        long lastUpdate = preferences.getLong(LAST_UPDATE_SUCCESS.name(), 0l);

        Long result = System.currentTimeMillis() - lastUpdate;
        return result;
    }

    public static Long getElapsedSinceLastRestartCheck(final Context context) {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        long lastCheck = preferences.getLong(RESTART_CHECK.name(), System.currentTimeMillis());

        Long result = System.currentTimeMillis() - lastCheck;
        return result;
    }

    public static void restartCheckDone(final Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(RESTART_CHECK.name(), System.currentTimeMillis());
        editor.commit();
    }

    public static String getLastUpdateResult(final Context context) {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        String result = preferences.getString(LAST_UPDATE_RESULT.name(), null);

        return result;
    }

    protected static void saveProperties(SharedPreferences preferences, Map<String, String> propertiesFetched) {
        SharedPreferences.Editor editor = preferences.edit();
        for (Map.Entry<String, String> prop : propertiesFetched.entrySet()) {
            String key = prop.getKey();
            String value = prop.getValue();

            checkForPvLoss(preferences, editor, key, value);

            editor.putString(key, value);
        }
        editor.commit();
    }

    protected static void checkForPvLoss(SharedPreferences preferences, SharedPreferences.Editor editor, String key, String value) {
        if (key.equals(PublicScriptProperties.PV.name())) {
            int actualPV = Integer.parseInt(preferences.getString(PublicScriptProperties.PV.name(), "-1"));
            int newPV = Integer.parseInt(value);
            editor.putString(PublicScriptProperties.PV_VARIATION.name(), Integer.toString(newPV - actualPV));
        }
    }

    public static String loadLogin(Context context) {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        String result = preferences.getString(PROPERTY_TROLL_ID, null);
        return result;
    }

    public static boolean saveIdPassword(Context context, String trollNumber, String trollPassword, boolean needToHashPassword) {
        if (Strings.isNullOrEmpty(trollNumber) || Strings.isNullOrEmpty(trollPassword)) {
            return false;
        }

        String finalTrollPassword = trollPassword;
        if (needToHashPassword) {
            boolean alreadyHashed = LEGACY_PASSWORD_PATTERN.matcher(trollPassword).matches();
            if (!alreadyHashed) {
                finalTrollPassword = MhDlaNotifierUtils.md5(trollPassword);
            }
        }

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PROPERTY_TROLL_ID, trollNumber);
        editor.putString(PROPERTY_TROLL_PASSWORD, finalTrollPassword);
        editor.commit();

        return true;
    }

    public static boolean areTrollIdentifiersUndefined(Context context) {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
        Pair<String, String> pair = loadIdPassword0(preferences);

        boolean result = Strings.isNullOrEmpty(pair.left());
        result |= Strings.isNullOrEmpty(pair.right());
        return result;
    }

    protected static Pair<String, String> loadIdPassword0(SharedPreferences preferences) {

        final String trollNumber = preferences.getString(PROPERTY_TROLL_ID, null);
        String trollPassword = preferences.getString(PROPERTY_TROLL_PASSWORD, null);

        Pair<String, String> result = new Pair<String, String>(trollNumber, trollPassword);
        return result;
    }


    protected static Pair<String, String> loadIdPassword(SharedPreferences preferences) throws MissingLoginPasswordException {

        Pair<String, String> pair = loadIdPassword0(preferences);

        if (Strings.isNullOrEmpty(pair.left()) || Strings.isNullOrEmpty(pair.right())) {
            throw new MissingLoginPasswordException();
        }

        return pair;
    }

    public static Troll refreshDLA(Context context) throws MissingLoginPasswordException {

        PreferencesHolder preferences = PreferencesHolder.load(context);

        boolean performUpdate = preferences.enableAutomaticUpdates;

        if (performUpdate) {

            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, 0);

            Pair<String, String> idAndPassword = loadIdPassword(sharedPreferences);

            Log.i(TAG, "Request for Profil2 fetch for refreshDLA()");
            // Force Profil2 fetch
            try {
                Map<String, String> propertiesFetched = PublicScriptsProxy.fetch(context, PublicScript.Profil2, idAndPassword);
                saveProperties(sharedPreferences, propertiesFetched);
            } catch (QuotaExceededException qee) {
                Log.w(TAG, "Quota exceeded, ignoring update", qee);
            } catch (NetworkUnavailableException qee) {
                Log.w(TAG, "Network failure, ignoring update", qee);
            } catch (PublicScriptException pse) {
                Log.w(TAG, "Script exception, ignoring update", pse);
            }
        }

        // Get updated (by the previous fetch) troll info
        Troll troll = fetchTrollWithoutUpdate(context);
        return troll;
    }

//    private static Date getDLA(SharedPreferences preferences) {
//        String string = preferences.getString(DLA.name(), null);
//        Date result = MhDlaNotifierUtils.parseDate(string);
//        return result;
//    }

//    public static Date getDLA(Context context) {
//        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
//        Date result = getDLA(preferences);
//        return result;
//    }
//
//    private static Integer getPA(SharedPreferences preferences) {
//        String string = preferences.getString(PA_RESTANT.name(), null);
//        Integer result = null;
//        try {
//            result = Integer.parseInt(string);
//        } catch (Exception eee) {
//            // Nothing to do
//        }
//        return result;
//    }

    public static Troll fetchTrollWithoutUpdate(Context context) throws MissingLoginPasswordException {
        try {
            Troll result = fetchTroll(context, UpdateRequestType.NONE);
            return result;
        } catch (QuotaExceededException e) {
            Log.e(TAG, "Should never happen", e);
            throw new RuntimeException("Should never happen", e);
        } catch (PublicScriptException e) {
            Log.e(TAG, "Should never happen", e);
            throw new RuntimeException("Should never happen", e);
        } catch (NetworkUnavailableException e) {
            Log.e(TAG, "Should never happen", e);
            throw new RuntimeException("Should never happen", e);
        }
    }

    public static boolean isCurrentPasswordALegacyPassword(Context context) {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
        try {
            Pair<String, String> idAndPassword = loadIdPassword(preferences);
            String savedPassword = idAndPassword.right();
            Matcher matcher = LEGACY_PASSWORD_PATTERN.matcher(savedPassword);
            return matcher.matches();
        } catch (MissingLoginPasswordException mlpe) {
            Log.w(TAG, "Unable to get id+password", mlpe);
            return false;
        }
    }

}