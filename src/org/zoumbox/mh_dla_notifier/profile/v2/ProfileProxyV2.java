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

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.zoumbox.mh_dla_notifier.MhDlaNotifierConstants;
import org.zoumbox.mh_dla_notifier.Pair;
import org.zoumbox.mh_dla_notifier.PreferencesHolder;
import org.zoumbox.mh_dla_notifier.profile.AbstractProfileProxy;
import org.zoumbox.mh_dla_notifier.profile.MissingLoginPasswordException;
import org.zoumbox.mh_dla_notifier.profile.ProfileProxy;
import org.zoumbox.mh_dla_notifier.profile.UpdateRequestType;
import org.zoumbox.mh_dla_notifier.sp.NetworkUnavailableException;
import org.zoumbox.mh_dla_notifier.sp.PublicScript;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptException;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptResult;
import org.zoumbox.mh_dla_notifier.sp.PublicScripts;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptsProxy;
import org.zoumbox.mh_dla_notifier.sp.QuotaExceededException;
import org.zoumbox.mh_dla_notifier.troll.Troll;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class ProfileProxyV2 extends AbstractProfileProxy implements ProfileProxy {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + ProfileProxyV2.class.getSimpleName();

    protected static final String PREFS_NAME_V2 = "org.zoumbox.mh.dla.notifier.preferences.v2";

    protected static final String PROPERTY_TROLL_IDS = "trollIds";

    protected static final String PROPERTY_PASSWORD = "password";
    protected static final String PROPERTY_PROFILE = "profile";
    protected static final String PROPERTY_LAST_UPDATE_ATTEMPT = "lastUpdateAttempt";
    protected static final String PROPERTY_LAST_UPDATE_RESULT = "lastUpdateResult";
    protected static final String PROPERTY_LAST_UPDATE_SUCCESS = "lastUpdateSuccess";

    protected SharedPreferences getPreferences(final Context context) {
        SharedPreferences result = context.getSharedPreferences(PREFS_NAME_V2, 0);
        return result;
    }

    protected String getProperty(String trollId, String propertyName) {
        String result = String.format("troll-%s.%s", trollId, propertyName);
        return result;
    }

    @Override
    public Set<String> getTrollIds(Context context) {
        String trollIdsString = getPreferences(context).getString(PROPERTY_TROLL_IDS, "");
        Iterable<String> trollIds = Splitter.on(",").split(trollIdsString);
        ImmutableSet<String> result = ImmutableSet.copyOf(trollIds);
        return result;
    }

    @Override
    public void saveIdPassword(Context context, String trollId, String trollPassword) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        String propertyName = getProperty(trollId, PROPERTY_PASSWORD);
        editor.putString(propertyName, trollPassword);
        editor.commit();
    }

    protected Pair<String, String> getTrollPassword(Context context, String trollId) {
        String propertyPassword = getProperty(trollId, PROPERTY_PASSWORD);
        String password = getPreferences(context).getString(propertyPassword, null);
        Pair<String, String> result = Pair.of(trollId, password);
        return result;
    }

    @Override
    public boolean areTrollIdentifiersUndefined(Context context) {
        Set<String> trollIds = getTrollIds(context);
        for (String trollId : trollIds) {
            if (getTrollPassword(context, trollId).right() == null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Pair<Troll, Boolean> fetchTroll(Context context, String trollId, UpdateRequestType updateRequestType)
            throws QuotaExceededException, MissingLoginPasswordException, PublicScriptException, NetworkUnavailableException {
        Troll troll = readTroll(context, trollId);

        boolean needUpdate = updateRequestType.needUpdate();
        needUpdate |= Strings.isNullOrEmpty(troll.getNom());
        needUpdate |= troll.getDla() == null;
        // TODO AThimel 07/08/13

        if (needUpdate) {
            Pair<String, String> idAndPassword = getTrollPassword(context, trollId);

            PublicScriptResult pp2Result = fetchScript(context, PublicScript.ProfilPublic2, idAndPassword);
            PublicScripts.pushToTroll(troll, pp2Result);

            PublicScriptResult p2Result = fetchScript(context, PublicScript.Profil2, idAndPassword);
            PublicScripts.pushToTroll(troll, p2Result);

            PublicScriptResult p2Caract = fetchScript(context, PublicScript.Caract, idAndPassword);
            PublicScripts.pushToTroll(troll, p2Caract);

            saveTroll(context, trollId, troll);
        }

        return Pair.of(troll, false); // TODO AThimel 19/08/13 implement needsUpdate
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

    protected PublicScriptResult fetchScript(Context context, PublicScript script, Pair<String, String> idAndPassword)
            throws QuotaExceededException, PublicScriptException, NetworkUnavailableException {
        String trollNumber = idAndPassword.left();
        String trollPassword = idAndPassword.right();
        PublicScriptResult result;
        long now = System.currentTimeMillis();
        try {
            result = PublicScriptsProxy.fetchScript(context, script, trollNumber, trollPassword);
            saveUpdateResult(context, trollNumber, script, now, null);
        } catch (PublicScriptException pse) {
            saveUpdateResult(context, trollNumber, script, now, pse);
            throw new PublicScriptException(pse);
        } catch (QuotaExceededException qee) {
            saveUpdateResult(context, trollNumber, script, now, qee);
            throw new QuotaExceededException(qee);
        } catch (NetworkUnavailableException nue) {
            saveUpdateResult(context, trollNumber, script, now, nue);
            throw new NetworkUnavailableException(nue);
        }

        return result;
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


            Log.i(TAG, "Request for Profil2 fetch for refreshDLA()");
            // Force Profil2 fetch
            try {
                Troll troll = readTroll(context, trollId);

                PublicScriptResult p2Result = fetchScript(context, PublicScript.Profil2, idAndPassword);
                PublicScripts.pushToTroll(troll, p2Result);

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
