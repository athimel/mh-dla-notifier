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
package org.zoumbox.mh_dla_notifier;

import android.net.Uri;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class MhDlaNotifierConstants {

    public static final String MOCK_TROLL_ID = "123456789";

    public static final String LOG_PREFIX = "mhdn-";

    public static final Uri MH_PLAY_URI = Uri.parse("http://games.mountyhall.com/mountyhall/MH_Play/PlayStart2.php?Desk");
    public static final Uri MH_PLAY_SMARTPHONE_URI = Uri.parse("http://smartphone.mountyhall.com");

    public static final int DEFAULT_NOTIFICATION_DELAY = 10;
    public static final boolean DEFAULT_NOTIFY_WITHOUT_PA = true;
    public static final boolean DEFAULT_USE_SMARTPHONE_INTERFACE = true;
    public static final boolean DEFAULT_NOTIFY_ON_PV_LOSS = true;
    public static final boolean DEFAULT_ALLOW_AUTOMATIC_UPDATES = true;
    public static final String DEFAULT_TIME_ZONE = null; // null means SP hour (Europe/Paris)

    public static final int PV_WARM_THRESHOLD = 66;
    public static final int PV_ALARM_THRESHOLD = 33;

    public static final String NOTIFICATION_CHANNEL_ID = "MhDlaNotifier_01";

}
