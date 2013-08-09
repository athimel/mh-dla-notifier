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

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.zoumbox.mh_dla_notifier.Pair;
import org.zoumbox.mh_dla_notifier.profile.AbstractProfileProxy;
import org.zoumbox.mh_dla_notifier.profile.MissingLoginPasswordException;
import org.zoumbox.mh_dla_notifier.profile.ProfileProxy;
import org.zoumbox.mh_dla_notifier.profile.UpdateRequestType;
import org.zoumbox.mh_dla_notifier.sp.NetworkUnavailableException;
import org.zoumbox.mh_dla_notifier.sp.PublicScript;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptException;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptsProxy;
import org.zoumbox.mh_dla_notifier.sp.QuotaExceededException;
import org.zoumbox.mh_dla_notifier.troll.Troll;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class ProfileProxyV2 extends AbstractProfileProxy implements ProfileProxy {

    protected static final String PREFS_NAME_V2 = "org.zoumbox.mh.dla.notifier.preferences.v2";

    protected static final String PROPERTY_TROLL_IDS = "trollIds";

    protected static final String PROPERTY_PASSWORD = "password";
    protected static final String PROPERTY_PROFILE = "profile";

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
    public Troll fetchTroll(Context context, String trollId, UpdateRequestType updateRequestType)
            throws QuotaExceededException, MissingLoginPasswordException, PublicScriptException, NetworkUnavailableException {
        String propertyProfile = getProperty(trollId, PROPERTY_PROFILE);
        String profileString = getPreferences(context).getString(propertyProfile, "{}");

        Troll result = new Gson().fromJson(profileString, Troll.class);

        boolean needUpdate = updateRequestType.needUpdate();
        needUpdate |= Strings.isNullOrEmpty(result.getNom());
        needUpdate |= result.getDla() == null;
        // TODO AThimel 07/08/13

        if (needUpdate) {
            Pair<String, String> idAndPassword = getTrollPassword(context, trollId);
            Map<String, String> propertiesFetched = PublicScriptsProxy.fetch(context, PublicScript.Profil2, idAndPassword);
        }

        return result;
    }

    @Override
    public String getLastUpdateResult(Context context) {
        return null;
    }

    @Override
    public Date getLastUpdateSuccess(Context context, String trollId) {
        return null;
    }

    @Override
    public Troll refreshDLA(Context context, String trollId) throws MissingLoginPasswordException {
        return null;
    }

    @Override
    public Long getElapsedSinceLastRestartCheck(Context context) {
        return null;
    }

    @Override
    public Long getElapsedSinceLastUpdateSuccess(Context context) {
        return null;
    }

    @Override
    public void restartCheckDone(Context context) {
    }
}
