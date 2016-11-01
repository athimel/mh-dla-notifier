package org.zoumbox.mh_dla_notifier.sp;

import junit.framework.Assert;

import org.junit.Test;
import org.zoumbox.mh_dla_notifier.MhDlaNotifierUtils;
import org.zoumbox.mh_dla_notifier.troll.Race;
import org.zoumbox.mh_dla_notifier.troll.Troll;
import org.zoumbox.mh_dla_notifier.utils.SystemLogCallback;

public class PublicScriptsTest {

    @Test
    public void testPushToTroll() {

        PublicScriptResult psrProfilPublic2 = new PublicScriptResult(PublicScript.ProfilPublic2, PublicScriptsProxyMock.ZEBU_PROFIL_PUBLIC2);
        PublicScriptResult psrProfil2 = new PublicScriptResult(PublicScript.Profil2, PublicScriptsProxyMock.ZEBU_PROFIL2);
        PublicScriptResult psrCaract = new PublicScriptResult(PublicScript.Caract, PublicScriptsProxyMock.ZEBU_CARACT);
        Troll troll = new Troll();
        PublicScripts.pushToTroll(troll, psrProfilPublic2, new SystemLogCallback());
        PublicScripts.pushToTroll(troll, psrProfil2, new SystemLogCallback());
        PublicScripts.pushToTroll(troll, psrCaract, new SystemLogCallback());
        Assert.assertEquals("105395", troll.getNumero());
        Assert.assertEquals("zebu\\'troll", troll.getNom());
        Assert.assertEquals(Race.Kastar, troll.getRace());
        Assert.assertEquals(38, troll.getNival());
        Assert.assertEquals(MhDlaNotifierUtils.parseSpDate("2011-09-02 11:14:36"), troll.getDateInscription());
        Assert.assertEquals(3, troll.getVueCar());
        Assert.assertEquals(3, troll.getArmureCar());
        Assert.assertEquals(573.0, troll.getDureeDuTourCar());
        Assert.assertEquals(140, troll.getPvMaxCar());
        Assert.assertEquals(145, troll.getPvActuelsCar());
    }

}
