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
        } else if (url.contains("SP_Equipement.php")) {
            rawResult = "9384101;0;Champignon;1;Fungus Rampant;Acide;7° jour du Démon du 11° cycle après Ragnarok;0.5\n" +
                    "9373814;0;Potion;1;Toxine Violente;;PV : -2 D3;5\n" +
                    "9367799;0;Casque;1;Casque en cuir;;Armure : +1 | RM : +9 %;5\n" +
                    "9375533;0;Champignon;1;Fungus Rampant;Acide;2° jour du Démon du 11° cycle après Ragnarok;0.5\n" +
                    "9347122;0;Potion;1;Elixir de Corruption;;ATT : +6 D3 | ESQ : -6 D3 | DEG : +6 D3 | REG : -6 | Vue : -6 | Armure : +6 | RM : -14 % | MM : -14 %;5\n" +
                    "9362866;0;Composant;1;Cuisse d'un Gremlins;de Qualité Bonne [Membre];Spécial;0.5\n" +
                    "9227939;0;Potion;1;Elixir de Feu;;ESQ : +6 D3 | Vue : +6;5\n" +
                    "9384096;0;Champignon;1;Fungus Rampant;Acide;7° jour du Démon du 11° cycle après Ragnarok;0.5\n" +
                    "9369658;0;Potion;1;Sinne Khole;;RM : +33 % | MM : -60 %;5\n" +
                    "9382885;0;Champignon;1;Fungus Rampant;Mielleux;7° jour du Démon du 11° cycle après Ragnarok;0.5\n" +
                    "9393138;0;Composant;1;Tripes d'un Ogre;de Qualité Moyenne [Corps];Spécial;0.5\n" +
                    "9219930;0;Champignon;1;Fungus Rampant;Acide;13° jour de la Mouche du 11° cycle après Ragnarok [Jour de la Sainte Malchance];0.5\n" +
                    "6665997;0;Champignon;1;Pied Jaune;Acide;9° jour du Démon du 8° cycle après Ragnarok;0.5\n" +
                    "9376167;0;Champignon;1;Fungus Rampant;Acide;3° jour du Démon du 11° cycle après Ragnarok;0.5\n" +
                    "9375532;0;Champignon;1;Fungus Rampant;Acide;2° jour du Démon du 11° cycle après Ragnarok;0.5\n" +
                    "9376168;0;Champignon;1;Fungus Rampant;Acide;3° jour du Démon du 11° cycle après Ragnarok;0.5\n" +
                    "9391769;0;Potion;1;Sortilège usage unique : Télékinésie;;Spécial;5\n" +
                    "9376169;0;Champignon;1;Fungus Rampant;Acide;3° jour du Démon du 11° cycle après Ragnarok;0.5\n" +
                    "9222127;0;Champignon;1;Fungus Rampant;Acide;14° jour de la Mouche du 11° cycle après Ragnarok;0.5\n" +
                    "5769036;0;Champignon;1;Pied Jaune;Salé;27° jour du Ver du 7° cycle après Ragnarok;0.5\n" +
                    "9218530;0;Champignon;1;Fungus Rampant;Acide;13° jour de la Mouche du 11° cycle après Ragnarok [Jour de la Sainte Malchance];0.5\n" +
                    "6142450;0;Champignon;1;Pied Jaune;Salé;20° jour du Scarabée du 7° cycle après Ragnarok;0.5\n" +
                    "9397711;0;Champignon;1;Fungus Rampant;Acide;15° jour du Démon du 11° cycle après Ragnarok;0.5\n" +
                    "9376166;0;Champignon;1;Fungus Rampant;Acide;3° jour du Démon du 11° cycle après Ragnarok;0.5\n" +
                    "9219925;0;Champignon;1;Fungus Rampant;Acide;13° jour de la Mouche du 11° cycle après Ragnarok [Jour de la Sainte Malchance];0.5\n" +
                    "9271248;0;Champignon;1;Suinte Cadavre;Acide;11° jour du Dindon du 11° cycle après Ragnarok;0.5\n" +
                    "4556824;0;Champignon;1;Suinte Cadavre;Acide;9° jour du Gnu du 6° cycle après Ragnarok;0.5\n" +
                    "9221328;0;Champignon;1;Fungus Rampant;Acide;14° jour de la Mouche du 11° cycle après Ragnarok;0.5\n" +
                    "9378511;0;Champignon;1;Fungus Rampant;Acide;4° jour du Démon du 11° cycle après Ragnarok;0.5\n" +
                    "3370416;0;Champignon;1;Nez Noir;Acide;9° jour du Phoenix du 6° cycle après Ragnarok;0.5\n" +
                    "3374758;0;Champignon;1;Nez Noir;Acide;10° jour du Phoenix du 6° cycle après Ragnarok;0.5\n" +
                    "9216001;0;Champignon;1;Fungus Rampant;Mielleux;11° jour de la Mouche du 11° cycle après Ragnarok;0.5\n" +
                    "3827228;0;Champignon;1;Pleurote Pleureuse;Acide;15° jour du Démon du 6° cycle après Ragnarok;0.5\n" +
                    "9276191;0;Parchemin;1;Traité de Clairvoyance;;Vue : +3 | TOUR : -90 min;2.5\n" +
                    "3831078;0;Champignon;1;Pleurote Pleureuse;Acide;16° jour du Démon du 6° cycle après Ragnarok [Z'nit du Silence];0.5\n" +
                    "6786002;0;Champignon;1;Suinte Cadavre;Acide;16° jour de la Limace du 8° cycle après Ragnarok;0.5\n" +
                    "972007;0;Champignon;1;Bolet Péteur;Acide;14° jour de la Mouche du 4° cycle après Ragnarok;0.5\n" +
                    "3053704;0;Champignon;1;Girolle Sanglante;Acide;19° jour de la Vouivre du 5° cycle après Ragnarok;0.5\n" +
                    "9347123;0;Composant;1;Patte d'un Scarabée Géant;de Qualité Bonne [Membre];Spécial;0.5\n" +
                    "3766484;0;Champignon;1;Pied Jaune;Acide;27° jour du Goblin du 6° cycle après Ragnarok;0.5\n" +
                    "3764152;0;Champignon;1;Pied Jaune;Acide;26° jour du Goblin du 6° cycle après Ragnarok [Fête du Sang];0.5\n" +
                    "5390903;1;Casque;1;Turban;du Rat;ESQ : +1 | RM : +14 %;2.5\n" +
                    "7952328;2;Talisman;1;Talisman d'Obsidienne;de l\\'Ours;ATT : +1 | DEG : +4 | REG : -4 | PV : +5 | TOUR : +30 min | RM : +40 % | MM : +20 %;2.5\n" +
                    "1612701;4;Armure;1;Tunique;du Rat;ESQ : +2 | RM : +8 % | MM : +6 %;2.5\n" +
                    "4017436;8;Bouclier;1;Rondache en métal;des Enragés;ATT : +1 | DEG : +1 | Armure : +2;30\n" +
                    "9228659;16;Arme (1 main);1;Crochet;de l\\'Ours;ATT : -2 | DEG : +5 | PV : +5 | TOUR : +30 min;12.5\n" +
                    "4332957;32;Bottes;1;Jambières en os;des Cyclopes;ATT : +1 | ESQ : -1 | DEG : +1 | Vue : -1 | Armure : +2 | RM : +7 %;10\n" +
                    "2841600;64;Anneau;1;Anneau de Protection;;Protection;7\n";
        } else if (url.contains("SP_Bonusmalus.php")) {
            rawResult = "Vue Troublée;Sortilège;Vue : -6;0\n" +
                    "Vue Troublée;Sortilège;Vue : -6;0\n" +
                    "Glue;Sortilège;;0\n" +
                    "Désorientation;Téléportation;ATT : -12 | ESQ : -13;2\n";
        } else {
            rawResult = "123456;Mon Trõll;Kastar;19;2011-01-21 14:07:48;;http://zoumbox.org/mh/DevelZimZoumMH.png;17;122;9;1900;20;0";
        }
        PublicScriptResponse result = new PublicScriptResponse(rawResult);
        return result;
    }

}
