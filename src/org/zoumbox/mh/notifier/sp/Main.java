package org.zoumbox.mh.notifier.sp;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class Main {

    public static void main(String[] args) throws QuotaExceededException {

        Profil2 profil2 = MHPublicScriptsProxy.fetch(Profil2.class, "aaa", "bbb", false);
        System.out.println(profil2);

        Profil3 profil3 = MHPublicScriptsProxy.fetch(Profil3.class, "aaa", "bbb", false);
        System.out.println(profil3);

        ProfilPublic2 profilPublic2 = MHPublicScriptsProxy.fetch(ProfilPublic2.class, "aaa", "bbb", true);
        System.out.println(profilPublic2);
    }

}
