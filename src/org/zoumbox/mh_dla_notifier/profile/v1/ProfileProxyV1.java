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
package org.zoumbox.mh_dla_notifier.profile.v1;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zoumbox.mh_dla_notifier.MhDlaNotifierConstants;
import org.zoumbox.mh_dla_notifier.MhDlaNotifierUtils;
import org.zoumbox.mh_dla_notifier.Pair;
import org.zoumbox.mh_dla_notifier.PreferencesHolder;
import org.zoumbox.mh_dla_notifier.profile.AbstractProfileProxy;
import org.zoumbox.mh_dla_notifier.profile.MissingLoginPasswordException;
import org.zoumbox.mh_dla_notifier.profile.ProfileProxy;
import org.zoumbox.mh_dla_notifier.profile.UpdateRequestType;
import org.zoumbox.mh_dla_notifier.sp.HighUpdateRateException;
import org.zoumbox.mh_dla_notifier.sp.NetworkUnavailableException;
import org.zoumbox.mh_dla_notifier.sp.PublicScript;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptException;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptsProxy;
import org.zoumbox.mh_dla_notifier.sp.QuotaExceededException;
import org.zoumbox.mh_dla_notifier.troll.Race;
import org.zoumbox.mh_dla_notifier.troll.Troll;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class ProfileProxyV1 extends AbstractProfileProxy implements ProfileProxy {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + ProfileProxyV1.class.getSimpleName();

    public static boolean shouldUpdate(Context context, PublicScript script, String trollNumber) {
        Date lastUpdate = PublicScriptsProxy.geLastUpdate(context, script, trollNumber);
        if (lastUpdate == null) {
            return true;
        } else {
            int dailyQuota = GET_USABLE_QUOTA.apply(script.getCategory());
            int minutesDelay = 24 * 60 / dailyQuota;
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, -minutesDelay);
            Date delay = calendar.getTime();
            boolean result = lastUpdate.before(delay);
            return result;
        }
    }

    protected String getTrollNumber(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        String trollNumber = preferences.getString(PROPERTY_TROLL_ID, null);
        return trollNumber;
    }

    public static final String PREFS_NAME = "org.zoumbox.mh.dla.notifier.preferences";

    protected static final String PROPERTY_TROLL_ID = "trollId";
    protected static final String PROPERTY_TROLL_PASSWORD = "trollPassword";

    public Set<String> getTrollIds(Context context) {

        String trollId = getTrollNumber(context);
        Set<String> result = new LinkedHashSet<String>();
        if (!Strings.isNullOrEmpty(trollId)) {
            result.add(trollId); // TODO AThimel 06/08/13 manage several trolls
        }
        return result;
    }

    public void saveIdPassword(Context context, String trollId, String trollPassword) {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PROPERTY_TROLL_ID, trollId);
        editor.putString(PROPERTY_TROLL_PASSWORD, trollPassword);
        editor.commit();


        SharedPreferences preferences2 = context.getSharedPreferences("org.zoumbox.mh.dla.notifier.preferences.v2", 0);
        SharedPreferences.Editor edit = preferences2.edit();
        edit.clear();
        edit.commit();
    }

    public boolean isPasswordDefined(Context context, String trollId) {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
        Pair<String, String> pair = loadIdPassword0(preferences);

        boolean result = Strings.isNullOrEmpty(pair.left());
        result |= Strings.isNullOrEmpty(pair.right());
        return result;
    }

    protected static Pair<String, String> loadIdPassword0(SharedPreferences preferences) {

        final String trollNumber = preferences.getString(PROPERTY_TROLL_ID, null);
        String trollPassword = preferences.getString(PROPERTY_TROLL_PASSWORD, null);

        Pair<String, String> result = Pair.of(trollNumber, trollPassword);
        return result;
    }


    protected Pair<String, String> loadIdPassword(SharedPreferences preferences) throws MissingLoginPasswordException {

        Pair<String, String> pair = loadIdPassword0(preferences);

        if (Strings.isNullOrEmpty(pair.left()) || Strings.isNullOrEmpty(pair.right())) {
            throw new MissingLoginPasswordException();
        }

        return pair;
    }

    public Pair<String, String> loadIdPassword(Context context) {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
        Pair<String, String> pair = loadIdPassword0(preferences);

        return pair;
    }

    public Pair<Troll, Boolean> fetchTroll(final Context context, String trollId, UpdateRequestType updateRequest)
            throws QuotaExceededException, MissingLoginPasswordException, PublicScriptException,
            NetworkUnavailableException {

        // TODO AThimel 06/08/13 use trollId

        Troll result = new Troll();

        List<String> requestedProperties = Arrays.asList(
                "NOM", "RACE", "NIVAL", "GUILDE", "CARACT", "BLASON", "NB_KILLS", "NB_MORTS", "DATE_INSCRIPTION",
                "PV", "PV_MAX", "FATIGUE", "POS_X", "POS_Y", "POS_N",
                "DUREE_DU_TOUR", "DLA", "PA_RESTANT",
                "CAMOU", "INVISIBLE", "INTANGIBLE", "IMMOBILE", "A_TERRE", "EN_COURSE", "LEVITATION");
        Map<String, String> properties = fetchProperties(
                context, updateRequest, requestedProperties);

        result.setNumero(getTrollNumber(context));

        result.setNom(properties.get("NOM"));
        result.setRace(Race.valueOf(properties.get("RACE")));
        result.setNival(Integer.parseInt(properties.get("NIVAL")));
        result.setDateInscription(MhDlaNotifierUtils.parseSpDate(properties.get("DATE_INSCRIPTION")));

        result.setPvActuelsCar(Integer.parseInt(properties.get("PV")));
        result.setPvVariation(Integer.parseInt(properties.get("PV_VARIATION")));
        result.setPvMaxCar(Integer.parseInt(properties.get("PV_MAX")));
        result.setFatigue(Integer.parseInt(properties.get("FATIGUE")));

        result.setPosX(Integer.parseInt(properties.get("POS_X")));
        result.setPosY(Integer.parseInt(properties.get("POS_Y")));
        result.setPosN(Integer.parseInt(properties.get("POS_N")));

        result.setCamou("1".equals(properties.get("CAMOU")));
        result.setInvisible("1".equals(properties.get("INVISIBLE")));
        result.setIntangible("1".equals(properties.get("INTANGIBLE")));
        result.setImmobile("1".equals(properties.get("IMMOBILE")));
        result.setATerre("1".equals(properties.get("A_TERRE")));
        result.setEnCourse("1".equals(properties.get("EN_COURSE")));
        result.setLevitation("1".equals(properties.get("LEVITATION")));

        result.setDureeDuTourCar(Integer.parseInt(properties.get("DUREE_DU_TOUR")));
        result.setDla(MhDlaNotifierUtils.parseSpDate(properties.get("DLA")));
        result.setPa(Integer.parseInt(properties.get("PA_RESTANT")));

        result.setBlason(properties.get("BLASON"));
        String guildeNumber = properties.get("GUILDE");
        if (!Strings.isNullOrEmpty(guildeNumber)) {
            result.setGuilde(Integer.parseInt(guildeNumber));
        }
        result.setNbKills(Integer.parseInt(properties.get("NB_KILLS")));
        result.setNbMorts(Integer.parseInt(properties.get("NB_MORTS")));

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

//        List<String> caractLines = Lists.newArrayList(Splitter.on("\n").omitEmptyStrings().trimResults().split(properties.get("CARACT")));
//        result.setPvBM(0);
//        result.setDlaBM(0);
//        result.setPoids(0);
//        int pvBm = 0, dlaBm = 0, poids = 0;
//        for (String line : caractLines) {
//            List<String> fields = Lists.newArrayList(Splitter.on(";").trimResults().split(line));
//            String type = fields.get(0);
//            if ("BMM".equals(type) || "BMP".equals(type)) {
//                pvBm += Integer.parseInt(fields.get(5));
//                Double dlaBMDouble = Math.floor(Double.parseDouble(fields.get(11)));
//                dlaBm += dlaBMDouble.intValue();
//                Double poidsDouble = Math.floor(Double.parseDouble(fields.get(12)));
//                poids += poidsDouble.intValue();
//            }
//        }
//        result.setPvBM(pvBm);
//        result.setDlaBM(dlaBm);
//        result.setPoids(poids);

        boolean needsUpdate = UpdateRequestType.valueOf(properties.get("NEEDS_UPDATE")).needUpdate();
        Pair<Troll, Boolean> resultPair = Pair.of(result, needsUpdate);

        return resultPair;
    }

    protected static Predicate<PublicScript> noNeedToUpdate(final Context context, final String trollNumber) {
        return new Predicate<PublicScript>() {
            @Override
            public boolean apply(PublicScript script) {
                return !shouldUpdate(context, script, trollNumber);
            }
        };
    }

    public Map<String, String> fetchProperties(
            final Context context, UpdateRequestType updateRequest, List<String> requestedProperties)
            throws QuotaExceededException, MissingLoginPasswordException, PublicScriptException, NetworkUnavailableException {

        Log.i(TAG, "Fetching properties with updateRequest: " + updateRequest);
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        Pair<String, String> idAndPassword = loadIdPassword(preferences);
        final String trollNumber = idAndPassword.left();

        // Iterate over requested properties to know which SP are concerned
        Set<PublicScript> scripts = new LinkedHashSet<PublicScript>();
        Log.i(TAG, "Requesting properties: " + requestedProperties);
//        for (String property : requestedProperties) {
//            PublicScript script = PublicScript.forProperty(property);
//            scripts.add(script);
//        }
//        scripts.add(PublicScript.ProfilPublic2);
//        scripts.add(PublicScript.Profil2);
//        scripts.add(PublicScript.Caract);

        // Maybe no update is requested, but needed because of missing property
        UpdateRequestType updateRequestType = updateRequest;
        if (!updateRequestType.needUpdate()) {
            for (String property : requestedProperties) {
                String value = preferences.getString(property, null);
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
                try {
                    Map<String, String> propertiesFetched = PublicScriptsProxy.fetchProperties(context, script, idAndPassword);
                    saveProperties(preferences, propertiesFetched);
                } catch (HighUpdateRateException hure) {
                    // Nothing to do
                }
            }
        } else {
            Predicate<PublicScript> noNeedToUpdatePredicate = noNeedToUpdate(context, trollNumber);
            Iterables.removeIf(scripts, noNeedToUpdatePredicate);

//            if (!scripts.isEmpty()) {
            if (scripts.contains(PublicScript.Profil2)) { // Ask for update only if profil2 needs an update
                backgroundUpdate = UpdateRequestType.ONLY_NECESSARY;
            }
        }

        Map<String, String> result = new LinkedHashMap<String, String>();
        Log.i(TAG, "Background update needed ? " + backgroundUpdate);
        result.put("NEEDS_UPDATE", backgroundUpdate.name());

        String pvVariation = preferences.getString("PV_VARIATION", "0");
        result.put("PV_VARIATION", pvVariation);

        for (String property : requestedProperties) {
            String value = preferences.getString(property, null);
            result.put(property, value);
        }

        return result;
    }

    public Date getLastUpdateSuccess(final Context context, String trollId) {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        long lastUpdate = preferences.getLong("LAST_UPDATE_SUCCESS", 0l);

        Date result = null;
        if (lastUpdate > 0l) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(lastUpdate);
            result = calendar.getTime();
        }
        return result;
    }

    public Long getElapsedSinceLastUpdateSuccess(final Context context, String trollId) {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        long lastUpdate = preferences.getLong("LAST_UPDATE_SUCCESS", 0l);

        Long result = System.currentTimeMillis() - lastUpdate;
        return result;
    }

    public String getLastUpdateResult(final Context context, String trollId) {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        String result = preferences.getString("LAST_UPDATE_RESULT", null);

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
        if (key.equals("PV")) {
            int actualPV = Integer.parseInt(preferences.getString("PV", "-1"));
            int newPV = Integer.parseInt(value);
            editor.putString("PV_VARIATION", Integer.toString(newPV - actualPV));
        }
    }

    public Troll refreshDLA(Context context, String trollId) throws MissingLoginPasswordException {

        PreferencesHolder preferences = PreferencesHolder.load(context);

        boolean performUpdate = preferences.enableAutomaticUpdates;

        if (performUpdate) {

            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, 0);

            Pair<String, String> idAndPassword = loadIdPassword(sharedPreferences);

            Log.i(TAG, "Request for Profil2 fetch for refreshDLA()");
            // Force Profil2 fetch
            try {
                Map<String, String> propertiesFetched = PublicScriptsProxy.fetchProperties(context, PublicScript.Profil2, idAndPassword);
                saveProperties(sharedPreferences, propertiesFetched);
            } catch (QuotaExceededException qee) {
                Log.w(TAG, "Quota exceeded, ignoring update", qee);
            } catch (NetworkUnavailableException qee) {
                Log.w(TAG, "Network failure, ignoring update", qee);
            } catch (PublicScriptException pse) {
                Log.w(TAG, "Script exception, ignoring update", pse);
            } catch (HighUpdateRateException hure) {
                Log.w(TAG, "Too much updates, ignoring update", hure);
            }
        }

        // Get updated (by the previous fetch) troll info
        Troll troll = fetchTrollWithoutUpdate(context, trollId).left();
        return troll;
    }

    @Override
    protected SharedPreferences getPreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
        return preferences;
    }

//    private static Date getDLA(SharedPreferences preferences) {
//        String string = preferences.getString(DLA.name(), null);
//        Date result = MhDlaNotifierUtils.parseSpDate(string);
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

}
