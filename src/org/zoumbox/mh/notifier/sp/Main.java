package org.zoumbox.mh.notifier.sp;

import java.util.Map;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class Main {

    public static void main(String[] args) throws QuotaExceededException {

        Map<String, String > result = MHPublicScriptsProxy.fetch(PublicScript.Profil2, "aaa", "bbb", false);
        System.out.println(result);

        result = MHPublicScriptsProxy.fetch(PublicScript.Profil3, "aaa", "bbb", false);
        System.out.println(result);

        result = MHPublicScriptsProxy.fetch(PublicScript.ProfilPublic2, "aaa", "bbb", true);
        System.out.println(result);
    }

}
