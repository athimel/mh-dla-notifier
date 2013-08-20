package org.zoumbox.mh_dla_notifier.sp;

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

import java.util.Map;

import org.zoumbox.mh_dla_notifier.MhDlaNotifierConstants;
import org.zoumbox.mh_dla_notifier.troll.Troll;
import org.zoumbox.mh_dla_notifier.utils.SystemLogCallback;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class MhTester {


    public static void main(String[] args) {
        SystemLogCallback logCallback = new SystemLogCallback();
        {
            Troll troll = new Troll();

            {
                PublicScriptResult publicScriptResult = new PublicScriptResult(PublicScript.ProfilPublic2, PublicScriptsProxyMock.DEVEL_PROFIL_PUBLIC2);
                PublicScripts.pushToTroll(troll, publicScriptResult, logCallback);
                System.out.println(troll);
            }

            {
                PublicScriptResult publicScriptResult = new PublicScriptResult(PublicScript.Profil2, PublicScriptsProxyMock.DEVEL_PROFIL2);
                PublicScripts.pushToTroll(troll, publicScriptResult, logCallback);
                System.out.println(troll);
            }

            {
                PublicScriptResult publicScriptResult = new PublicScriptResult(PublicScript.Caract, PublicScriptsProxyMock.DEVEL_CARACT);
                PublicScripts.pushToTroll(troll, publicScriptResult, logCallback);
                System.out.println(troll);
            }
        }

        for (Map.Entry<PublicScript, String> entry : PublicScriptsProxyMock.getMockScripts().entries()) {
            Troll troll = new Troll();
            PublicScriptResult result = new PublicScriptResult(entry.getKey(), entry.getValue());
            PublicScripts.pushToTroll(troll, result, logCallback);
            System.out.println(troll);
        }
    }
}
