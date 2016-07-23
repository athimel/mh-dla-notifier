package org.zoumbox.mh_dla_notifier.profile;

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

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.zoumbox.mh_dla_notifier.Pair;
import org.zoumbox.mh_dla_notifier.sp.NetworkUnavailableException;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptException;
import org.zoumbox.mh_dla_notifier.sp.QuotaExceededException;
import org.zoumbox.mh_dla_notifier.sp.ScriptCategory;
import org.zoumbox.mh_dla_notifier.troll.Troll;

import com.google.common.base.Function;

import android.content.Context;

/**
 * @author Arnaud Thimel <a.thimel at gmail.com>
 */
public interface ProfileProxy {

    public static final Function<ScriptCategory, Integer> GET_USABLE_QUOTA = new Function<ScriptCategory, Integer>() {
        @Override
        public Integer apply(ScriptCategory input) {
            if (input == null) {
                return 1;
            }

            int result;
            if (ScriptCategory.STATIC.equals(input)) {
                // Static scripts are updated only once a day
                result = 1;
            } else {
                int dailyQuota = input.getQuota();
                result = dailyQuota / 3; // Divide by 3 for the moment to avoid mistakes
            }

            return result;
        }
    };

    Set<String> getTrollIds(Context context);

    void saveIdPassword(Context context, String trollId, String trollPassword);

    boolean isPasswordDefined(Context context, String trollId);

    Pair<Troll, Boolean> fetchTrollWithoutUpdate(Context context, String trollId) throws MissingLoginPasswordException;

    Pair<Troll, Boolean> fetchTroll(Context context, String trollId, UpdateRequestType updateRequestType)
            throws QuotaExceededException, MissingLoginPasswordException, PublicScriptException,
            NetworkUnavailableException;

    String getLastUpdateResult(Context context, String trollId);

    Date getLastUpdateSuccess(Context context, String trollId);

    Troll refreshDLA(Context context, String trollId) throws MissingLoginPasswordException;

    Long getElapsedSinceLastRestartCheck(Context context);

    Long getElapsedSinceLastUpdateSuccess(Context context, String trollId);

    void restartCheckDone(Context context);

}
