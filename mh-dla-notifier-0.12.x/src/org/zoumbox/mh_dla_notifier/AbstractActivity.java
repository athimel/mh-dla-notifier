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
package org.zoumbox.mh_dla_notifier;

import org.zoumbox.mh_dla_notifier.profile.ProfileProxy;
import org.zoumbox.mh_dla_notifier.profile.v1.ProfileProxyV1;
import org.zoumbox.mh_dla_notifier.profile.v2.ProfileProxyV2;

import android.app.Activity;

public abstract class AbstractActivity extends Activity {

    private ProfileProxy profileProxy;

    public ProfileProxy getProfileProxy() {
        if (profileProxy == null) {
//            profileProxy = new ProfileProxyV1();
            profileProxy = new ProfileProxyV2();
        }
        return profileProxy;
    }

    protected void showToast(CharSequence message, Object... args) {
        MhDlaNotifierUtils.toast(getApplicationContext(), message, args);
    }

}
