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
import android.widget.Toast;
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

import static org.zoumbox.mh_dla_notifier.sp.PublicScriptProperties.*;
/**
 * @author Arno <arno@zoumbox.org>
 */
public class ProfileProxy {

    private static final String TAG = Constants.LOG_PREFIX + ProfileProxy.class.getSimpleName();

    public static final String PREFS_NAME = "org.zoumbox.mh.dla.notifier.preferences";

    public static final String PROPERTY_TROLL_ID = "trollId";
    public static final String PROPERTY_TROLL_PASSWORD = "trollPassword";

    @Deprecated
    protected static final Map<PublicScriptProperties, String> oldKeys = Maps.newHashMap();
    static {
        oldKeys.put(NOM, "nom");
        oldKeys.put(RACE, "race");
        oldKeys.put(NIVAL, "niveau");
        oldKeys.put(BLASON, "blason");
        oldKeys.put(NB_KILLS, "nbKills");
        oldKeys.put(NB_MORTS, "nbMorts");
        oldKeys.put(POS_X, "posX");
        oldKeys.put(POS_Y, "posY");
        oldKeys.put(POS_N, "posN");
        oldKeys.put(PV, "pv");
        oldKeys.put(PV_MAX, "pvMax");
        oldKeys.put(PA_RESTANT, "paRestant");
        oldKeys.put(DLA, "dla");
        oldKeys.put(DUREE_DU_TOUR, "dureeDuTour");
    }


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

    public static Troll fetchTroll(final Context context, boolean requestForUpdate) throws QuotaExceededException, MissingLoginPasswordException, PublicScriptException, NetworkUnavailableException {

        Troll result = new Troll();

        Map<PublicScriptProperties, String> properties = ProfileProxy.fetchProperties(context, requestForUpdate,
                NOM, RACE, NIVAL, PV, PV_MAX, POS_X, POS_Y, POS_N,
                CAMOU, INVISIBLE, INTANGIBLE, DUREE_DU_TOUR,
                DLA, PA_RESTANT, BLASON, NB_KILLS, NB_MORTS,
                MOUCHES);

        result.id = getTrollNumber(context);

        result.nom = properties.get(NOM);
        result.race = Race.valueOf(properties.get(RACE));
        result.nival = Integer.parseInt(properties.get(NIVAL));

        result.pv = Integer.parseInt(properties.get(PV));
        result.pvMaxBase = Integer.parseInt(properties.get(PV_MAX));

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
        result.nbKills = Integer.parseInt(properties.get(NB_KILLS));
        result.nbMorts = Integer.parseInt(properties.get(NB_MORTS));

        result.mouches = Lists.newArrayList();
        List<String> lines = Lists.newArrayList(Splitter.on("\n").omitEmptyStrings().trimResults().split(properties.get(MOUCHES)));
        for (String line : lines) {
            List<String> fields = Lists.newArrayList(Splitter.on(";").split(line));
            Mouche mouche = new Mouche();
            mouche.id = fields.get(0);
            mouche.nom = fields.get(1);
            mouche.type = MoucheType.valueOf(fields.get(2));
            mouche.age = Integer.parseInt(fields.get(3));
            mouche.presente = "LA".equals(fields.get(4));

            result.mouches.add(mouche);
        }

        result.needsUpdate = Boolean.TRUE.toString().endsWith(properties.get(NEEDS_UPDATE));

        return result;
    }

    public static Map<PublicScriptProperties, String> fetchProperties(final Context context, boolean requestForUpdate, PublicScriptProperties ... names) throws QuotaExceededException, MissingLoginPasswordException, PublicScriptException, NetworkUnavailableException {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        Pair<String, String> idAndPassword = loadIdPassword(preferences);
        final String trollNumber = idAndPassword.left();

        Set<PublicScript> scripts = Sets.newLinkedHashSet();
        Log.i(TAG, "Requesting properties: " + names);
        for (PublicScriptProperties property : names) {
            PublicScript script = PublicScript.forProperty(property);
            scripts.add(script);
        }

        boolean forceUpdate = requestForUpdate;
        if (!forceUpdate) {
            for (PublicScriptProperties property : names) {
                String value = preferences.getString(property.name(), null);
                if (value == null) {
                    forceUpdate = true;
                    break;
                }
            }
        }

        boolean needsBackgroundUpdate;
        if (forceUpdate) {
            needsBackgroundUpdate = false;
            for (PublicScript type : scripts) { // FIXME AThimel 25/05/2012 Even if I force update, not all scripts should be updated
                Map<String, String> propertiesFetched = PublicScriptsProxy.fetch(context, type, idAndPassword);
                saveProperties(preferences, propertiesFetched);
            }
        } else {
            Iterables.removeIf(scripts, new Predicate<PublicScript>() {
                @Override
                public boolean apply(PublicScript script) {
                    return !needsUpdate(context, script, trollNumber);
                }
            });

            needsBackgroundUpdate =  !scripts.isEmpty();
        }

        Map<PublicScriptProperties, String> result = Maps.newLinkedHashMap();
        result.put(NEEDS_UPDATE, ""+needsBackgroundUpdate);

        for (PublicScriptProperties property : names) {
            String oldValue = preferences.getString(oldKeys.get(property), null);
            String value = preferences.getString(property.name(), oldValue);
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

    public static Pair<Date, Integer> refreshDLA(Context context) throws MissingLoginPasswordException, PublicScriptException {

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        Pair<String, String> idAndPassword = loadIdPassword(preferences);

        Map<String, String> propertiesFetched;
        try {
            propertiesFetched = PublicScriptsProxy.fetch(context, PublicScript.Profil2, idAndPassword);
            saveProperties(preferences, propertiesFetched);
        } catch (QuotaExceededException qee) {
            MhDlaNotifierUtils.toast(context, "Quota atteint, pas de mise à jour");
        } catch (NetworkUnavailableException nue) {
            MhDlaNotifierUtils.toast(context, "Pas de réseau, pas de mise à jour");
        }

        Date dla = getDLA(preferences);
        Integer pa = getPA(preferences);
        Pair<Date, Integer> result = new Pair<Date, Integer>(dla, pa);
        return result;
    }

    protected static Date getDLA(SharedPreferences preferences) {
        String string = preferences.getString(DLA.name(), null);
        Date result = MhDlaNotifierUtils.parseDate(string);
        return result;
    }

    public static Date getDLA(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
        Date result = getDLA(preferences);
        return result;
    }

    public static Integer getPA(SharedPreferences preferences) {
        String string = preferences.getString(PA_RESTANT.name(), null);
        Integer result = null;
        try {
            result = Integer.parseInt(string);
        } catch (Exception eee) {
            // Nothing to do
        }
        return result;
    }

    public static Integer getPA(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
        Integer result = getPA(preferences);
        return result;
    }
}
