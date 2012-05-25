/*
 * #%L
 * MountyHall DLA Notifier
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2012 Zoumbox.org
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
package org.zoumbox.mh_dla_notifier.sp;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class PublicScriptsProxyMock {

    public static PublicScriptResponse doMockHttpGET(String url) {
        String rawResult;
        String dla = "2012-05-22 12:25:36";
        int pa = 3;
        if (url.contains("SP_Profil2.php")) {
            rawResult = "123456;57;-75;-41;50;80;" + pa + ";" + dla + ";8;4;13;4;4;6;360;361;0;5;0;0;0;0;0;585;0;1;0";
        } else if (url.contains("SP_Profil3.php")) {
            rawResult = "123456;Mon Trõll;57;-75;-41;" + pa + ";" + dla + ";3;0;0;0;2;22;88;6042";
        } else if (url.contains("Mouche.php")) {
            rawResult = "567856;ers;Lunettes;404;LA\n" +
                    "563814;ToMars;Crobate;453;LA\n" +
                    "562632;ingToTheMoon;Nabolisants;467;LA\n" +
                    "565369;ToGether;Crobate;434;LA\n" +
                    "569033;fe;Telaite;389;LA\n" +
                    "571309;ingToTheSpace;Nabolisants;361;LA\n" +
                    "570159;ingToTheSun;Nabolisants;375;LA\n" +
                    "572547;Faster;Rivatant;350;LA\n" +
                    "574887;Strong;Xidant;318;LA\n" +
                    "575785;eRs;Lunettes;305;LA\n" +
                    "578000;;Vertie;272;LA\n" +
                    "579486;;Rivatant;241;LA\n" +
                    "581779;;Vertie;210;LA\n" +
                    "583093;;Telaite;194;LA\n" +
                    "586569;;Rivatant;149;LA\n" +
                    "588345;;Vertie;126;LA\n" +
                    "589627;;Xidant;110;LA\n" +
                    "591596;;Miel;83;LA\n" +
                    "592829;;Rivatant;67;LA\n" +
                    "594975;;Xidant;38;LA\n" +
                    "596944;;Telaite;12;LA\n" +
                    "\n" +
                    "\n" +
                    "\n";
        } else if (url.contains("SP_Equipement.php")) {
            rawResult = "9332117;0;Potion;1;Jus de Chronomètre;;TOUR : -120 min;5\n" +
                    "9332290;0;Potion;1;Voï'Pu'Rin;;Vue : -20;5\n" +
                    "9333962;0;Potion;1;Jus de Chronomètre;;TOUR : -30 min;5\n" +
                    "6237353;0;Parchemin;1;Invention Extraordinaire;;Aucune description disponible;2.5\n" +
                    "9333963;0;Composant;1;Bave d'un Fumeux;de Qualité Mauvaise [Spécial];Spécial;0.5\n" +
                    "9381922;0;Parchemin;1;Rune des Foins;;DEG : -4 D3 | Vue : -1 | PV : -4 D3;2.5\n" +
                    "9333561;0;Composant;1;Os d'un Fumeux;de Qualité Mauvaise [Corps];Spécial;0.5\n" +
                    "9119888;0;Parchemin;1;Idées Confuses;;ATT : -4 D3 | TOUR : +120 min;2.5\n" +
                    "9361786;0;Potion;1;Elixir de Longue-Vue;;Vue : +2;5\n" +
                    "9387143;0;Composant;1;Main d'une Erinyes;de Qualité Très Mauvaise [Membre];Spécial;0.5\n" +
                    "8387153;0;Potion;1;Potion de Guérison;;PV : +4 D3;5\n" +
                    "9329828;0;Potion;1;Elixir de Bonne Bouffe;;DEG : +5 D3 | REG : +5;5\n" +
                    "9256955;0;Parchemin;1;Yeu'Ki'Pic;;Vue : -6 | Effet de Zone;2.5\n" +
                    "9331351;0;Parchemin;1;Idées Confuses;;ATT : -2 D3 | TOUR : +60 min;2.5\n" +
                    "9237512;0;Parchemin;1;Plan Génial;;ATT : +2 D3 | DEG : +2 D3 | TOUR : -30 min;2.5\n" +
                    "2885780;1;Casque;1;Casque en métal;des Vampires;ATT : +1 | REG : +1 | Vue : -1 | Armure : +2 | RM : +10 %;10\n" +
                    "8633037;2;Talisman;1;Talisman d'Obsidienne;;ATT : +1 | DEG : +2 | REG : -4 | RM : +24 % | MM : +38 %;2.5\n" +
                    "3974191;4;Armure;1;Tunique d'écailles;des Vampires;ATT : +1 | ESQ : -1 | REG : +1 | Armure : +3 | RM : +20 %;30\n" +
                    "6127207;8;Bouclier;1;Rondache en bois;des Vampires;ATT : +1 | ESQ : +1 | REG : +1 | Armure : +1;15\n" +
                    "4911535;16;Arme (1 main);1;Lame en os;du Temps;DEG : +2 | TOUR : -30 min;7.5\n" +
                    "5403228;32;Bottes;1;Bottes;des Cyclopes;ATT : +1 | ESQ : +2 | DEG : +1 | Vue : -1;5\n" +
                    "8138607;64;Anneau;1;Anneau de Protection;;Protection;11\n";
        } else {
            rawResult = "123456;Mon Trõll;Kastar;19;2011-01-21 14:07:48;;http://zoumbox.org/mh/DevelZimZoumMH.png;17;122;9;1900;20;0";
        }
        PublicScriptResponse result = new PublicScriptResponse(rawResult);
        return result;
    }

}
