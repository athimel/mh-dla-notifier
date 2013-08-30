package org.zoumbox.mh_dla_notifier.profile;

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

import org.zoumbox.mh_dla_notifier.MhDlaNotifierConstants;
import org.zoumbox.mh_dla_notifier.Pair;
import org.zoumbox.mh_dla_notifier.sp.NetworkUnavailableException;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptException;
import org.zoumbox.mh_dla_notifier.sp.QuotaExceededException;
import org.zoumbox.mh_dla_notifier.troll.Troll;

import com.google.common.base.Preconditions;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public abstract class AbstractProfileProxy implements ProfileProxy {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + AbstractProfileProxy.class.getSimpleName();
    protected static final String PROPERTY_RESTART_CHECK = "RESTART_CHECK";

    protected abstract SharedPreferences getPreferences(final Context context);

    public Pair<Troll, Boolean> fetchTrollWithoutUpdate(Context context, String trollId) throws MissingLoginPasswordException {
        Preconditions.checkArgument(trollId != null);
        try {
            Pair<Troll, Boolean> result = fetchTroll(context, trollId, UpdateRequestType.NONE);
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

    public Long getElapsedSinceLastRestartCheck(final Context context) {
        long lastCheck = getPreferences(context).getLong(PROPERTY_RESTART_CHECK, System.currentTimeMillis());
        Long result = System.currentTimeMillis() - lastCheck;
        return result;
    }

    public void restartCheckDone(final Context context) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putLong(PROPERTY_RESTART_CHECK, System.currentTimeMillis());
        editor.commit();
    }

}
