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
import java.util.Set;

import org.zoumbox.mh_dla_notifier.profile.AbstractProfileProxy;
import org.zoumbox.mh_dla_notifier.profile.MissingLoginPasswordException;
import org.zoumbox.mh_dla_notifier.profile.ProfileProxy;
import org.zoumbox.mh_dla_notifier.profile.UpdateRequestType;
import org.zoumbox.mh_dla_notifier.sp.NetworkUnavailableException;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptException;
import org.zoumbox.mh_dla_notifier.sp.QuotaExceededException;
import org.zoumbox.mh_dla_notifier.troll.Troll;

import android.content.Context;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class ProfileProxyV2 extends AbstractProfileProxy implements ProfileProxy {

    @Override
    public Set<String> getTrollIds(Context context) {
        return null;
    }

    @Override
    public boolean saveIdPassword(Context context, String trollId, String trollPassword) {
        return false;
    }

    @Override
    public boolean areTrollIdentifiersUndefined(Context context) {
        return false;
    }

    @Override
    public Troll fetchTroll(Context context, String trollId, UpdateRequestType updateRequestType) throws QuotaExceededException, MissingLoginPasswordException, PublicScriptException, NetworkUnavailableException {
        return null;
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
