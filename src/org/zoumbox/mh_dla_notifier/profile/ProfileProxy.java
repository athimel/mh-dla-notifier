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

import java.util.Date;
import java.util.Set;

import org.zoumbox.mh_dla_notifier.sp.NetworkUnavailableException;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptException;
import org.zoumbox.mh_dla_notifier.sp.QuotaExceededException;
import org.zoumbox.mh_dla_notifier.sp.ScriptCategory;
import org.zoumbox.mh_dla_notifier.troll.Troll;

import com.google.common.base.Function;

import android.content.Context;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public interface ProfileProxy {

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

    Set<String> getTrollIds(Context context);

    boolean saveIdPassword(Context context, String trollId, String trollPassword);

    boolean areTrollIdentifiersUndefined(Context context);

    Troll fetchTrollWithoutUpdate(Context context, String trollId) throws MissingLoginPasswordException;

    Troll fetchTroll(Context context, String trollId, UpdateRequestType updateRequestType)
            throws QuotaExceededException, MissingLoginPasswordException, PublicScriptException,
            NetworkUnavailableException;

    String getLastUpdateResult(final Context context);

    Date getLastUpdateSuccess(Context context, String trollId);

    Troll refreshDLA(Context context, String trollId) throws MissingLoginPasswordException;

    Long getElapsedSinceLastRestartCheck(final Context context);

    Long getElapsedSinceLastUpdateSuccess(final Context context);

    void restartCheckDone(final Context context);
}
