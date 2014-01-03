package org.zoumbox.mh_dla_notifier.profile.v2;

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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

import org.zoumbox.mh_dla_notifier.MhDlaNotifierConstants;
import org.zoumbox.mh_dla_notifier.Pair;
import org.zoumbox.mh_dla_notifier.PreferencesHolder;
import org.zoumbox.mh_dla_notifier.profile.AbstractProfileProxy;
import org.zoumbox.mh_dla_notifier.profile.MissingLoginPasswordException;
import org.zoumbox.mh_dla_notifier.profile.ProfileProxy;
import org.zoumbox.mh_dla_notifier.profile.UpdateRequestType;
import org.zoumbox.mh_dla_notifier.profile.v1.ProfileProxyV1;
import org.zoumbox.mh_dla_notifier.sp.NetworkUnavailableException;
import org.zoumbox.mh_dla_notifier.sp.PublicScript;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptException;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptResult;
import org.zoumbox.mh_dla_notifier.sp.PublicScripts;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptsProxy;
import org.zoumbox.mh_dla_notifier.sp.QuotaExceededException;
import org.zoumbox.mh_dla_notifier.troll.Troll;
import org.zoumbox.mh_dla_notifier.utils.AndroidLogCallback;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class ProfileProxyV2 extends AbstractProfileProxy implements ProfileProxy {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + ProfileProxyV2.class.getSimpleName();

    protected static final String PREFS_NAME_V2 = "org.zoumbox.mh.dla.notifier.preferences.v2";

    // General properties
    protected static final String PROPERTY_TROLL_IDS = "trollIds";

    // Per troll properties
    protected static final String PROPERTY_PASSWORD = "password";
    protected static final String PROPERTY_PROFILE = "profile";
    protected static final String PROPERTY_LAST_UPDATE_ATTEMPT = "lastUpdateAttempt";
    protected static final String PROPERTY_LAST_UPDATE_RESULT = "lastUpdateResult";
    protected static final String PROPERTY_LAST_UPDATE_SUCCESS = "lastUpdateSuccess";

    protected SharedPreferences getPreferences(Context context) {
        SharedPreferences result = context.getSharedPreferences(PREFS_NAME_V2, 0);
        return result;
    }

    protected String getProperty(String trollId, String propertyName) {
        String result = String.format("troll-%s.%s", trollId, propertyName);
        return result;
    }

    @Override
    public Set<String> getTrollIds(Context context) {
        Set<String> result = Sets.newLinkedHashSet(getTrollIds0(context));
        if (result == null || result.isEmpty()) {
            boolean migrationResult = tryMigration(context);
            if (migrationResult) {
                result = Sets.newLinkedHashSet(getTrollIds0(context));
            }
        }
        return result;
    }

    protected boolean tryMigration(Context context) {
        Log.i(TAG, "Try migration from ProfileProxyV1");
        ProfileProxyV1 profileProxyV1 = new ProfileProxyV1();
        Set<String> trollIds = profileProxyV1.getTrollIds(context);
        boolean successful = false;
        if (trollIds != null && !trollIds.isEmpty()) {
            for (String trollId : trollIds) {
                Log.i(TAG, "Try migration from ProfileProxyV1 for troll: " + trollId);
                Pair<String, String> stringPair = profileProxyV1.loadIdPassword(context);
                if (stringPair != null && !Strings.isNullOrEmpty(stringPair.left()) && !Strings.isNullOrEmpty(stringPair.right())) {
                    try {
                        Pair<Troll, Boolean> trollAndUpdatePair = profileProxyV1.fetchTrollWithoutUpdate(context, trollId);
                        if (trollAndUpdatePair != null && trollAndUpdatePair.left() != null) {
                            Troll troll = trollAndUpdatePair.left();
                            Log.i(TAG, "Troll read from ProfileProxyV1: " + troll);
                            saveTroll(context, trollId, troll);
                            saveIdPassword(context, trollId, stringPair.right());
                            successful = true;
                        }
                    } catch (Exception eee) {
                        Log.w(TAG, "Unable to migrate troll: " + trollId, eee);

                        Log.i(TAG, "Try to save id and password", eee);
                        try {
                            saveIdPassword(context, trollId, stringPair.right());
                            successful = true;
                        } catch (Exception eee2) {
                            Log.w(TAG, "Unable to save id and password for troll: " + trollId, eee2);
                        }
                    }
                }
            }
        }
        return successful;
    }

    protected List<String> getTrollIds0(Context context) {
        String trollIdsString = getPreferences(context).getString(PROPERTY_TROLL_IDS, "");
        Iterable<String> trollIds = Splitter.on(",").omitEmptyStrings().split(trollIdsString);
        List<String> result = Lists.newArrayList(trollIds);
        Log.i(TAG, "Managed trolls: " + result);
        return result;
    }

    @Override
    public void saveIdPassword(Context context, String trollId, String trollPassword) {
        List<String> trollIds = getTrollIds0(context);
        if (!trollIds.contains(trollId)) {
            Log.i(TAG, String.format("Adding troll %s to managed trolls: %s", trollId, trollIds));
            trollIds.add(0, trollId);
        }
        String password = trollPassword;
        try {
            password = password.replaceAll("\n", "").trim();
        } catch (Exception eee) {
            Log.w(TAG, "Unable to escape password", eee);
        }

        SharedPreferences.Editor editor = getPreferences(context).edit();
        String propertyName = getProperty(trollId, PROPERTY_PASSWORD);
        editor.putString(propertyName, password);
        editor.putString(PROPERTY_TROLL_IDS, Joiner.on(",").join(trollIds));
        editor.commit();
    }

    protected Pair<String, String> getTrollPassword(Context context, String trollId) {
        String propertyPassword = getProperty(trollId, PROPERTY_PASSWORD);
        String password = getPreferences(context).getString(propertyPassword, null);
        Pair<String, String> result = Pair.of(trollId, password);
        return result;
    }

    @Override
    public boolean isPasswordDefined(Context context, String trollId) {
        String password = getTrollPassword(context, trollId).right();
        boolean result = !Strings.isNullOrEmpty(password);
        return result;
    }

    @Override
    public Pair<Troll, Boolean> fetchTroll(Context context, String trollId, UpdateRequestType updateRequestType)
            throws QuotaExceededException, MissingLoginPasswordException, PublicScriptException, NetworkUnavailableException {
        Troll troll = readTroll(context, trollId);

        Set<PublicScript> scriptsToUpdate = Sets.newLinkedHashSet();

        // Pas de nom -> Besoin des informations statiques
        if (Strings.isNullOrEmpty(troll.getNom())) {
            scriptsToUpdate.add(PublicScript.ProfilPublic2);
        }
        // Pas de DLA -> Besoin des informations dynamiques (DLA, PV, PA, ...)
        if (troll.getDla() == null) {
            scriptsToUpdate.add(PublicScript.Profil2);
        }
        // Pas de poids d'équipement -> Besoin des caracts
        if (troll.getPoidsBmp() == 0d) {
            scriptsToUpdate.add(PublicScript.Caract);
        }
        if (UpdateRequestType.FULL.equals(updateRequestType)) {
            // XXX AThimel 12/11/13 Do not include static profile to avoid too much requests
            scriptsToUpdate.add(PublicScript.Profil2);
            scriptsToUpdate.add(PublicScript.Caract);
        }
        Set<PublicScript> scriptsThatRequiresAnUpdate = getScriptsThatRequiresAnUpdate(context, trollId);
        if (updateRequestType.needUpdate()) {
            scriptsToUpdate.addAll(scriptsThatRequiresAnUpdate);
        }

        if (!scriptsToUpdate.isEmpty()) {
            Pair<String, String> idAndPassword = getTrollPassword(context, trollId);

            fetchScripts(context, idAndPassword, troll, scriptsToUpdate);
            scriptsThatRequiresAnUpdate.removeAll(scriptsToUpdate);

            saveTroll(context, trollId, troll);
        }

        boolean needsABackgroundUpdate = !scriptsThatRequiresAnUpdate.isEmpty();
        Pair<Troll, Boolean> result = Pair.of(troll, needsABackgroundUpdate);
        return result;
    }

    protected static final Set<PublicScript> WATCHED_SCRIPTS = ImmutableSortedSet.of(PublicScript.ProfilPublic2, PublicScript.Profil2, PublicScript.Caract);

    protected Set<PublicScript> getScriptsThatRequiresAnUpdate(Context context, String trollId) {
        final Map<PublicScript, Date> lastUpdates = PublicScriptsProxy.geLastUpdates(context, trollId, WATCHED_SCRIPTS);
        Set<PublicScript> result = Sets.newHashSet(Iterables.filter(WATCHED_SCRIPTS, new Predicate<PublicScript>() {
            @Override
            public boolean apply(PublicScript input) {
                Date lastUpdate = lastUpdates.get(input);
                if (lastUpdate == null) {
                    return true;
                }

                // TODO AThimel 28/08/13 Improve update policy

                int dailyQuota = GET_USABLE_QUOTA.apply(input.getCategory());
                int minutesDelay = 24 * 60 / dailyQuota;
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MINUTE, -minutesDelay);
                Date delay = calendar.getTime();
                boolean result = lastUpdate.before(delay);
                return result;
            }
        }));
        return result;
    }

    protected Troll readTroll(Context context, String trollId) {
        String propertyProfile = getProperty(trollId, PROPERTY_PROFILE);
        String profileString = getPreferences(context).getString(propertyProfile, "{}");

        Troll result = new Gson().fromJson(profileString, Troll.class);
        return result;
    }

    protected void saveTroll(Context context, String trollId, Troll troll) {
        String propertyProfile = getProperty(trollId, PROPERTY_PROFILE);
        SharedPreferences.Editor editor = getPreferences(context).edit();
        String profileString = new Gson().toJson(troll);
        editor.putString(propertyProfile, profileString);
        editor.commit();
    }

    protected Optional<PublicScriptResult> fetchScript(Context context, PublicScript script, Pair<String, String> idAndPassword)
            throws QuotaExceededException, PublicScriptException, NetworkUnavailableException {
        String trollId = idAndPassword.left();
        String trollPassword = idAndPassword.right();
        Optional<PublicScriptResult> result;
        long now = System.currentTimeMillis();
        try {
            result = PublicScriptsProxy.fetchScript(context, script, trollId, trollPassword);
            saveUpdateResult(context, trollId, script, now, null);
        } catch (PublicScriptException pse) {
            saveUpdateResult(context, trollId, script, now, pse);
            throw new PublicScriptException(pse);
        } catch (QuotaExceededException qee) {
            saveUpdateResult(context, trollId, script, now, qee);
            throw new QuotaExceededException(qee);
        } catch (NetworkUnavailableException nue) {
            saveUpdateResult(context, trollId, script, now, nue);
            throw new NetworkUnavailableException(nue);
        }

        return result;
    }

    protected void fetchScripts(Context context, Pair<String, String> idAndPassword, Troll troll, Set<PublicScript> scripts)
            throws QuotaExceededException, PublicScriptException, NetworkUnavailableException {

        Preconditions.checkNotNull(troll);

        int pvBeforeUpdate = troll.getPvActuelsCar();

        AndroidLogCallback logCallback = new AndroidLogCallback();
        for (PublicScript publicScript : scripts) {
            Optional<PublicScriptResult> publicScriptResult = fetchScript(context, publicScript, idAndPassword);
            PublicScripts.pushToTroll(troll, publicScriptResult, logCallback);
        }

        int pvAfterUpdate = troll.getPvActuelsCar();

        int variation = pvAfterUpdate - pvBeforeUpdate;
        troll.setPvVariation(variation);
    }

    protected void saveUpdateResult(Context context, String trollId, PublicScript script, long date, Exception exception) {
        if (PublicScript.Profil2.equals(script)) {

            boolean success = false;
            String result;
            if (exception == null) {
                result = "SUCCESS";
                success = true;
            } else {
                result = exception.getClass().getName();
                if (exception instanceof NetworkUnavailableException || (exception instanceof PublicScriptException && exception.getCause() != null)) {
                    result = "NETWORK ERROR: " + result;
                }
            }

            SharedPreferences.Editor editor = getPreferences(context).edit();
            editor.putLong(getProperty(trollId, PROPERTY_LAST_UPDATE_ATTEMPT), date);
            editor.putString(getProperty(trollId, PROPERTY_LAST_UPDATE_RESULT), result);
            if (success) {
                editor.putLong(getProperty(trollId, PROPERTY_LAST_UPDATE_SUCCESS), date);
            }

            editor.commit();
        }
    }

    @Override
    public String getLastUpdateResult(Context context, String trollId) {

        String result = getPreferences(context).getString(getProperty(trollId, PROPERTY_LAST_UPDATE_RESULT), null);

        return result;
    }

    @Override
    public Date getLastUpdateSuccess(Context context, String trollId) {

        long lastUpdate = getPreferences(context).getLong(getProperty(trollId, PROPERTY_LAST_UPDATE_SUCCESS), 0l);

        Date result = null;
        if (lastUpdate > 0l) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(lastUpdate);
            result = calendar.getTime();
        }
        return result;
    }

    @Override
    public Troll refreshDLA(Context context, String trollId) throws MissingLoginPasswordException {

        PreferencesHolder preferences = PreferencesHolder.load(context);

        boolean performUpdate = preferences.enableAutomaticUpdates;

        if (performUpdate) {

            Pair<String, String> idAndPassword = getTrollPassword(context, trollId);


            Log.i(TAG, "Request for Profil2 fetch for #refreshDLA()");
            // Force Profil2 fetch
            try {
                Troll troll = readTroll(context, trollId);

                fetchScripts(context, idAndPassword, troll, ImmutableSet.of(PublicScript.Profil2));

                saveTroll(context, trollId, troll);
            } catch (QuotaExceededException qee) {
                Log.w(TAG, "Quota exceeded, ignoring update", qee);
            } catch (NetworkUnavailableException qee) {
                Log.w(TAG, "Network failure, ignoring update", qee);
            } catch (PublicScriptException pse) {
                Log.w(TAG, "Script exception, ignoring update", pse);
            }
        }

        // Get updated (by the previous fetch) troll info
        Troll troll = fetchTrollWithoutUpdate(context, trollId).left();
        return troll;
    }

    @Override
    public Long getElapsedSinceLastUpdateSuccess(Context context, String trollId) {
        long lastUpdate = getPreferences(context).getLong(getProperty(trollId, PROPERTY_LAST_UPDATE_SUCCESS), 0l);

        Long result = System.currentTimeMillis() - lastUpdate;
        return result;
    }

}
