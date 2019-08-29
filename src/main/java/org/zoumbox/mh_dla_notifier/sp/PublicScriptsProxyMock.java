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
package org.zoumbox.mh_dla_notifier.sp;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class PublicScriptsProxyMock {

    private enum MockTroll {
        DevelZimzoum, Snorf, omnipotente, plusilyadefoumoinilyaderi, Uch, zebu, Kaht, TmT, OutrAskai, Nanp
    }

    public static PublicScriptResponse doMockHttpGET(String url) {
        long start = System.currentTimeMillis();
        MockTroll mockTroll = MockTroll.Nanp;

        String rawResult = null;
        for (PublicScript ps : PublicScript.values()) {
            if (url.contains(ps.name() + ".php")) {
                rawResult = scripts.get(ps).get(mockTroll);
                if (rawResult == null) {
                    rawResult = scripts.get(ps).get(MockTroll.DevelZimzoum);
                }
                if (rawResult != null) {
                    break;
                }
            }
        }
        if (rawResult == null) {
            throw new UnsupportedOperationException("URL non prévue : " + url + " troll: " + mockTroll);
        }
        long end = System.currentTimeMillis();
        PublicScriptResponse result = new PublicScriptResponse(rawResult, end - start);
        return result;
    }

    public static final String DEVEL_PROFIL2 =
//            "104259;95;-77;-61;86;90;0;2013-02-11 15:55:25;12;4;21;4;4;6;1288;986;0;4;0;0;0;0;0;585;-75;1;0;1;0;1;0";
//            "104259;7;72;-59;56;90;2;2013-04-10 04:14:25;13;4;21;5;4;10;1439;1095;0;12;0;0;0;0;0;585;-75;2;0;0;0;1;0";
//            "104259;8;-29;-5;73;90;5;2013-08-05 11:17:25;13;4;23;5;4;14;1545;1305;0;0;0;0;0;0;0;585;-60;2;0;0;0;0;0";
//            "104259;20;-52;-31;105;90;6;2013-08-19 21:47:25;13;4;24;5;4;14;1555;1314;0;0;0;0;0;0;0;585;-60;2;0;0;0;0;0";
//            "123456789;21;-53;-31;64;90;6;2014-04-12 11:25:42;13;4;24;5;4;14;1555;1315;0;3;0;0;0;0;0;585;-60;2;0;1;0;0;0";
//            "104259;28;-59;-69;105;67;0;2013-11-15 16:04:55;7;8;24;5;4;14;1705;1467;0;14;0;0;0;0;0;573;-60;3;0;0;0;0;0";
//            "104259;-23;-98;-71;111;100;1;2014-04-10 16:56:25;14;11;26;5;4;17;2075;1670;0;1;0;0;0;0;0;573;-80;3;0;0;0;0;0";
            "123456789;46;-88;-17;185;160;3;2016-10-21 13:00:55;18;18;32;6;4;23;4415;2729;0;8;0;0;0;0;0;573;-90;3;0;0;0;0;0";
    public static final String DEVEL_PROFIL_PUBLIC2 =
//            "123456;Mon Trõll;Kastar;19;2011-01-21 14:07:48;;http://zoumbox.org/mh/syndikhd/104259-happy.png;17;122;9;1900;20;0";
//            "104259;DevelZimZoum;Kastar;30;2011-01-21 14:07:48;;http://zoumbox.org/mh/syndikhd/104259-happy.png;30;317;10;1900;20;0\n";
//            "123456789;Mon Trõll;Kastar;33;2011-01-21 14:07:48;;http://zoumbox.org/mh/syndikhd/104259-happy.png;34;341;10;1900;20;0";
            "123456789;Mon Trõll;Kastar;50;2011-01-21 14:07:48;;http://zoumbox.org/mh/syndikhd/104259-happy_300.png;62;647;15;1900;20;0";
    public static final String DEVEL_CARACT =
            // Type; Attaque; Esquive; Dégats; Régénération; PVMax; PVActuels; Portée deVue; RM; MM; Armure; Duree du Tour; Poids; Concentration
//            "BMM;-6;-8; 9; 6; 15;  0; 3;1232; 647; 4;-120;  0;0\n" +
//            "BMP; 3; 1; 6;-4;  0;  0;-1;   0;   0;14;   0;108;0\n" +
//            "CAR;13; 8;24; 5;100;105; 4;1467;1705; 3; 573;  0;0";
//            "BMM; 6;5; 9; 3;15;  0; 2;1103; 590; 4;-120;    0;0\n" +
//            "BMP; 3;1; 6;-4; 0;  0;-1;   0;   0;14;   0;105.5;0\n" +
//            "CAR;13;4;24; 5;90; 67; 4;1314;1555; 2; 585;    0;0\n";
//            "BMM;6;3;5; 4; 5; 0; 1;262;193;3;-130;  0; 0\n" +
//            "BMP;1;2;4;-4; 0; 0;-1;  0;  0;6;   0;125;13\n" +
//            "CAR;9;4;16;4;80;85; 4;486;510;1; 585;  0; 0\n";
//            "BMM;14;3; 9; 4; 5; 0;-3;275;201;3;-130;  0;0\n" +
//            "BMP; 1;2; 4;-4; 0; 0;-1;  0;  0;6;   0;149;0\n" +
//            "CAR; 9;4;16; 4;80;75; 4;511;530;1; 585;  0;0\n";
//                    "BMM;6;3; 5; 4; 5; 0; 1;279;201;3;-130;    0;0\n" +
//                    "BMP;1;2; 4;-4; 0; 0;-1;  0;  0;6;   0;144.5;9\n" +
//                    "CAR;9;4;16; 4;80;85; 4;518;531;1; 585;    0;0\n";
//            "BMM; 8;4; 6; 4;10; 0; 2;1022; 546; 4;-150;    0;0\n" +
//                    "BMP; 1;2; 4;-4; 0; 0;-1;   0;   0;10;   0;193.5;0\n" +
//                    "CAR;13;4;21; 5;90;56; 4;1095;1439; 2; 585;    0;0\n";
//            "BMM; 9;  7;-2; 6; 15;  0; 1;2070; 788; 6;-160;    0;0\n" +
//            "BMP;-3;-10; 2;-4;  0;  0;-4;   0;   0;17;   0;111.5;0\n" +
//            "CAR;14; 11;26; 5;100;115; 4;1670;2075; 3; 573;    0;0";
            "BMM;13;7;9;12;25;0;4;4120;1677;7;-180;0;0\n"+
            "BMP;6;-3;1;-4;0;0;-1;0;0;23;0;152.5;0\n"+
            "CAR;18;18;32;6;160;185;4;2729;4415;3;573;0;0";
    public static final String DEVEL_MOUCHES =
            "599939;;Crobate;632;LA\n" +
                    "567856;ers;Lunettes;1065;LA\n" +
                    "563814;ToMars;Crobate;1114;LA\n" +
                    "633679;;Miel;153;LA\n" +
                    "612312;;Vertie;458;LA\n" +
                    "636799;;Miel;105;LA\n" +
                    "562632;ingToTheMoon;Nabolisants;1128;LA\n" +
                    "602416;;Héros;595;LA\n" +
                    "616779;;Nabolisants;400;LA\n" +
                    "637453;;Héros;95;LA\n" +
                    "565369;ToGether;Crobate;1095;LA\n" +
                    "569033;fe;Telaite;1051;LA\n" +
                    "571309;ingToTheSpace;Nabolisants;1023;LA\n" +
                    "570159;ingToTheSun;Nabolisants;1037;LA\n" +
                    "572547;Faster;Rivatant;1011;LA\n" +
                    "574887;Strong;Xidant;980;LA\n" +
                    "575785;eRs;Lunettes;967;LA\n" +
                    "578000;;Vertie;933;LA\n" +
                    "579486;;Rivatant;902;LA\n" +
                    "581779;;Vertie;872;LA\n" +
                    "583093;;Rivatant;855;LA\n" +
                    "586569;;Rivatant;811;LA\n" +
                    "588345;;Vertie;787;LA\n" +
                    "589627;;Xidant;772;LA\n" +
                    "641119;;Rivatant;38;LA\n" +
                    "591596;;Miel;744;LA\n" +
                    "592829;;Rivatant;729;LA\n" +
                    "594975;;Xidant;699;LA\n" +
                    "596944;;Nabolisants;674;LA\n" +
                    "602417;;Rivatant;595;LA\n" +
                    "604774;;Xidant;559;LA\n" +
                    "607662;;Lunettes;522;LA\n" +
                    "614433;;Telaite;429;LA\n" +
                    "619546;;Miel;363;LA\n" +
                    "621732;;Vertie;334;LA\n" +
                    "624911;;Miel;286;LA\n" +
                    "628982;;Héros;220;LA\n" +
                    "629009;;Nabolisants;219;LA\n" +
                    "642141;[Lampe Géniale 10032945];Rivatant;24;LA\n" +
                    "642142;[Lampe Géniale 10032945];Vertie;24;LA\n" +
                    "642143;[Lampe Géniale 10032945];Rivatant;24;LA\n" +
                    "642144;[Lampe Géniale 10032945];Miel;24;LA\n" +
                    "642145;[Lampe Géniale 10032945];Nabolisants;24;LA\n" +
                    "642146;[Lampe Géniale 10032945];Lunettes;24;LA\n" +
                    "642984;;Telaite;8;LA\n" +
                    "\n" +
                    "\n" +
                    "\n";
    public static final String DEVEL_BONUS_MALUS =
            "Vue Troublée;Sortilège;Vue : -6;0\n" +
                    "Vue Troublée;Sortilège;Vue : -6;0\n" +
                    "Glue;Sortilège;;0\n" +
                    "Désorientation;Téléportation;ATT : -12 | ESQ : -13;2\n";
    public static final String DEVEL_APTITUDES2 =
            "C;18;90;0;2;\n" +
                    "C;18;90;0;1;\n" +
                    "C;3;93;0;1;\n" +
                    "C;16;90;0;5;\n" +
                    "C;16;90;0;4;\n" +
                    "C;16;90;0;3;\n" +
                    "C;16;90;0;2;\n" +
                    "C;16;90;0;1;\n" +
                    "C;12;90;0;1;\n" +
                    "C;21;90;0;1;\n" +
                    "C;8;85;0;5;\n" +
                    "C;8;90;0;4;\n" +
                    "C;8;90;0;3;\n" +
                    "C;8;90;0;2;\n" +
                    "C;8;87;0;1;\n" +
                    "C;14;82;0;1;\n" +
                    "C;44;90;0;1;\n" +
                    "C;11;90;0;1;\n" +
                    "C;7;84;0;1;\n" +
                    "C;5;57;0;1;\n" +
                    "C;9;54;0;1;\n" +
                    "S;3;80;0;1\n" +
                    "S;10;80;0;1\n" +
                    "S;27;80;0;1\n" +
                    "S;28;37;0;1";
    public static final String DEVEL_EQUIP =
            "9332117;0;Potion;1;Jus de Chronomètre;;TOUR : -120 min;5\n" +
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
    public static final String DEVEL_VUE =
            "#DEBUT TROLLS\n" +
                    "12517;83;-106;-78\n" +
                    "49604;83;-106;-78\n" +
                    "50362;83;-106;-78\n" +
                    "89138;83;-106;-78\n" +
                    "88222;83;-106;-78\n" +
                    "86132;83;-106;-78\n" +
                    "90568;83;-106;-78\n" +
                    "95636;83;-106;-78\n" +
                    "100160;83;-106;-78\n" +
                    "104259;83;-106;-78\n" +
                    "#FIN TROLLS\n" +
                    "#DEBUT MONSTRES\n" +
                    "1829509;Gowap Apprivoisé [Ancien] Pàpaäht;86;-108;-78\n" +
                    "3333207;Golem de cuir [Légendaire];86;-108;-78\n" +
                    "1782296;Gowap Apprivoisé [Ancien] Pétàüre;82;-106;-77\n" +
                    "3131275;Gowap Apprivoisé [Ancien];83;-106;-79\n" +
                    "3260316;Gowap Apprivoisé [Ancien];83;-106;-79\n" +
                    "4236780;Nâ-Hàniym-Hééé [Vénérable];83;-106;-79\n" +
                    "4266040;Squelette [Ancien];83;-106;-78\n" +
                    "4266434;Petit Squelette [Ancien];83;-106;-78\n" +
                    "4264646;Croquemitaine [Ancien];83;-106;-78\n" +
                    "4304082;Croquemitaine [Naissant];83;-106;-78\n" +
                    "4278807;Abishaii Rouge [Novice];83;-106;-78\n" +
                    "1881670;Gowap Apprivoisé [Ancien];83;-106;-77\n" +
                    "2023605;Gowap Apprivoisé [Ancien] Sühssùk;83;-106;-77\n" +
                    "2571623;Gowap Apprivoisé [Ancien] Nöhnõsse;83;-106;-77\n" +
                    "4272893;Abishaii Rouge [Novice];83;-106;-77\n" +
                    "#FIN MONSTRES\n" +
                    "#DEBUT ORIGINE\n" +
                    "4;83;-106;-78\n" +
                    "#FIN ORIGINE\n";

    public static final String OMNI_PROFIL_PUBLIC2 = "104098;omnipotente;Kastar;17;2011-01-01 11:40:46;loicoudard@yahoo.fr;http://blason.mountyhall.com/Blason_PJ/104098;16;72;8;281;80;0\n";
    public static final String OMNI_EQUIP =
            "9384101;0;Champignon;1;Fungus Rampant;Acide;7° jour du Démon du 11° cycle après Ragnarok;0.5\n" +
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
    public static final String OMNI_CARACT =
            "BMM; 5;3; 9; 0;15; 0;-1;257; 292;3;-20;    0;0\n" +
            "BMP;-1;1; 5;-4; 0; 0; 0;  0;   0;4;  0;192.5;0\n" +
            "CAR; 5;6;12; 3;70;85; 5;373;1129;3;639;    0;0\n";

    public static final String SNORF_PROFIL2 = "86133;-31;-71;-55;161;190;0;2012-06-06 05:30:46;16;15;19;9;12;23;10311;2211;2;0;0;0;0;0;0;585;0;5;1";
    public static final String SNORF_PROFIL_PUBLIC2 = "86133;Snorf le jeune;Skrim;44;2007-02-06 23:09:17;;http://blason.mountyhall.com/Blason_PJ/86133;44;338;7;1900;160;0";
    public static final String SNORF_BONUS_MALUS =
            "Charme;Capacité Spéciale;ATT : -2 | ESQ : -5 | Vue : -1;1\n" +
                    "Charme;Capacité Spéciale;ATT : -2 | ESQ : -3 | Vue : -1;1\n" +
                    "Elixir de Feu;Potion;ESQ : +11 | Vue : +5;5\n" +
                    "Maladie;Capacité Spéciale;DEG : -8 | REG : -4;2\n";
    public static final String SNORF_CARACT =
            // Type; Attaque; Esquive; Dégats; Régénération; PVMax; PVActuels; Portée deVue; RM; MM; Armure; Duree du Tour; Poids; Concentration
            "BMM; 5;13;11; 6;  5;  0;11;5544; 4122;10;-160;  0;0\n" +
            "BMP;-1;-5;-5;-8;  0;  0;-2;   0;    0;23;   0;114;0\n" +
            "CAR;16;15;19; 9;190;161;12;2211;10311; 5; 585;  0;0\n";

    public static final String PYDFMYDR_PROFIL_PUBLIC2 =
            "104259;plusilyadefoumoinilyaderi;Darkling;5;2010-04-26 17:22:39;plusilyadefoumoinilyaderi@hotmail.fr;http://blason.mountyhall.com/Blason_PJ/102611;9;75;11;1;00;0";

    public static final String UCH_PROFIL2 = "88222;81;-100;-76;100;100;0;2012-06-29 18:23:12;3;11;3;7;28;11;4336;1139;0;0;0;0;0;0;0;618;-50;1;0;0;0;0";

//    public static final String ZEBU_PROFIL2 =
//            "105395;45;-87;-16;145;140;6;2016-10-25 05:40:40;3;10;30;4;3;9;5397;2148;0;2;0;0;0;0;0;573;-45;3;0;0;0;0;0";
//    public static final String ZEBU_PROFIL_PUBLIC2 =
//            "105395;zebu\\'troll;Kastar;38;2011-09-02 11:14:36;;http://zoumbox.org/mh/syndikhd/105395_100.png;26;443;13;1900;40;0";
//    public static final String ZEBU_CARACT =
//            "BMM;9;1;5;5;5;0;3;1353;1511;5;-90;0;0\n" +
//            "BMP;-1;5;1;-2;0;0;0;-107;-269;9;0;63;84\n" +
//            "CAR;3;10;30;4;140;145;3;2148;5397;3;573;0;0";

    // 01/11/2016 22h43
    public static final String ZEBU_PROFIL2 =
            "105395;15;-104;-55;145;140;0;2016-11-02 04:44:40;3;13;30;5;3;9;5419;2155;0;7;0;0;0;0;0;573;-45;3;0;0;0;0;0";
    public static final String ZEBU_PROFIL_PUBLIC2 =
            "105395;zebu\\'troll;Kastar;38;2011-09-02 11:14:36;;http://zoumbox.org/mh/syndikhd/105395_100.png;26;443;13;1900;40;0";
    public static final String ZEBU_CARACT =
            "BMM; 2;-4; 5; 5;  5;  0;3;1357;1517;5;-90;   0;0\n" +
            "BMP;-1; 5; 1;-2;  0;  0;0;-107;-270;9;  0;75.5;0\n" +
            "CAR; 3;13;30; 5;140;145;3;2155;5419;3;573;   0;0";

    public static final String KAHT_PROFIL2 = "86132;20;-56;-56;191;240;0;2016-11-02 06:32:17;6;19;42;9;3;23;14194;4167;0;0;0;0;0;0;0;573;-75;5;0;0;0;0;0";
    public static final String KAHT_APTITUDES2 = "C;7;75;0;1;\n" +
            "C;26;77;0;1;\n" +
            "C;21;90;0;1;\n" +
            "C;12;90;0;1;\n" +
            "C;14;81;0;1;\n" +
            "C;24;90;0;1;\n" +
            "C;16;90;0;4;\n" +
            "C;16;90;0;3;\n" +
            "C;16;90;0;2;\n" +
            "C;16;90;0;1;\n" +
            "C;3;90;0;1;\n" +
            "C;5;84;0;1;\n" +
            "C;18;90;0;1;\n" +
            "C;44;90;0;1;\n" +
            "C;48;90;0;1;\n" +
            "S;3;82;0;1\n" +
            "S;8;80;0;1\n" +
            "S;10;80;0;1\n" +
            "S;12;80;0;1\n" +
            "S;13;54;0;1\n" +
            "S;15;80;0;1\n" +
            "S;16;80;0;1\n" +
            "S;17;80;0;1\n" +
            "S;24;80;0;1\n" +
            "S;27;80;0;1";
    public static final String KAHT_PROFIL_PUBLIC2 = "86132;Kahtaströll;Kastar;59;2007-02-06 23:08:29;cedric.hallereau@free.fr;http://zoumbox.org/mh/syndikhd/86132-fofo_300.png;63;889;12;1900;150;0";
    public static final String KAHT_CARACT = "BMM;11;-3;7;7;5;0;5;11084;6387;15;-150;0;0\n" +
            "BMP;-2;-4;1;3;0;0;-2;0;0;23;0;261;0\n" +
            "CAR;6;19;42;9;240;191;3;4167;14194;5;573;0;0";

    public static final String TMT_PROFIL2 = "100160;20;-102;-43;270;260;0;2016-11-01 22:36:42;17;24;26;9;3;22;8208;3114;0;0;0;0;0;0;0;585;-90;3;0;0;0;0;0";
    public static final String TMT_PROFIL_PUBLIC2 = "100160;TailleMannequinTroll;Durakuir;55;2009-07-09 10:10:29;;http://zoumbox.org/mh/syndikhd/100160.png;62;443;12;1900;180;0";
    public static final String TMT_CARACT =
        "BMM;-25;-9;11;6;10;0;8;6695;3283;11;-180;0;0\n" +
        "BMP;0;-4;4;-4;0;0;-1;0;0;22;0;170.5;0\n" +
        "CAR;17;24;26;9;260;270;3;3114;8208;3;585;0;0";


    public static final String OUTRASKAI_PROFIL_PUBLIC2 = "109623;Outr'Askaï;Darkling;19;2015-10-06 11:25:39;;http://zoumbox.org/mh/syndikhd/109623_300.png;19;1;0;1900;20;0";

    public static final String NANP_PROFIL_PUBLIC2 = "123456789;Nanp;Darkling;12;2019-05-01 11:29:41;;//blason.mountyhall.com/Blason_PJ/111134;;13;0;0;1;0;0;0";
    public static final String NANP_PROFIL2 = "123456789;34;9;-37;90;70;3;2019-08-29 17:30:00;3;3;3;5;6;0;302;228;0;0;0;0;0;0;0;618;-435;3;0;0;0;0;0;90;12;808;1;10;0;;618";
    public static final String NANP_CARACT =
            "BMM;-23;-20;5;0;0;0;6;0;0;7;-90;0;0\n" +
            "BMP;-2;2;-7;-8;20;0;-2;186;181;13;-120;108;0\n" +
            "CAR;3;3;3;5;70;30;6;228;302;3;618;0;0";

    public static final Map<PublicScript, Map<MockTroll, String>> scripts = new HashMap<PublicScript, Map<MockTroll, String>>();

    static {
        for (PublicScript ps : PublicScript.values()) {
            Map<MockTroll, String> trollStringMap = new HashMap<MockTroll, String>();
            scripts.put(ps, trollStringMap);
        }

        // Devel
//        scripts.get(PublicScript.Mouche).put(MockTroll.DevelZimzoum, DEVEL_MOUCHES);
        scripts.get(PublicScript.Profil2).put(MockTroll.DevelZimzoum, DEVEL_PROFIL2);
        scripts.get(PublicScript.ProfilPublic2).put(MockTroll.DevelZimzoum, DEVEL_PROFIL_PUBLIC2);
//        scripts.get(PublicScript.Equipement).put(MockTroll.DevelZimzoum, DEVEL_EQUIP);
//        scripts.get(PublicScript.Aptitudes2).put(MockTroll.DevelZimzoum, DEVEL_APTITUDES2);
//        scripts.get(PublicScript.BonusMalus).put(MockTroll.DevelZimzoum, DEVEL_BONUS_MALUS);
        scripts.get(PublicScript.Caract).put(MockTroll.DevelZimzoum, DEVEL_CARACT);

        // omnipotente
        scripts.get(PublicScript.ProfilPublic2).put(MockTroll.omnipotente, OMNI_PROFIL_PUBLIC2);
//        scripts.get(PublicScript.Equipement).put(MockTroll.omnipotente, OMNI_EQUIP);
        scripts.get(PublicScript.Caract).put(MockTroll.omnipotente, OMNI_CARACT);

        // Snorf
        scripts.get(PublicScript.Profil2).put(MockTroll.Snorf, SNORF_PROFIL2);
        scripts.get(PublicScript.ProfilPublic2).put(MockTroll.Snorf, SNORF_PROFIL_PUBLIC2);
//        scripts.get(PublicScript.BonusMalus).put(MockTroll.Snorf, SNORF_BONUS_MALUS);
        scripts.get(PublicScript.Caract).put(MockTroll.Snorf, SNORF_CARACT);

        // plusilyadefoumoinilyaderi
        scripts.get(PublicScript.ProfilPublic2).put(MockTroll.plusilyadefoumoinilyaderi, PYDFMYDR_PROFIL_PUBLIC2);

        // Uch
        scripts.get(PublicScript.Profil2).put(MockTroll.Uch, UCH_PROFIL2);

        // zebu
        scripts.get(PublicScript.Profil2).put(MockTroll.zebu, ZEBU_PROFIL2);
        scripts.get(PublicScript.ProfilPublic2).put(MockTroll.zebu, ZEBU_PROFIL_PUBLIC2);
        scripts.get(PublicScript.Caract).put(MockTroll.zebu, ZEBU_CARACT);

        // Kaht
        scripts.get(PublicScript.Profil2).put(MockTroll.Kaht, KAHT_PROFIL2);
        scripts.get(PublicScript.ProfilPublic2).put(MockTroll.Kaht, KAHT_PROFIL_PUBLIC2);
        scripts.get(PublicScript.Caract).put(MockTroll.Kaht, KAHT_CARACT);

        // TmT
        scripts.get(PublicScript.Profil2).put(MockTroll.TmT, TMT_PROFIL2);
        scripts.get(PublicScript.ProfilPublic2).put(MockTroll.TmT, TMT_PROFIL_PUBLIC2);
        scripts.get(PublicScript.Caract).put(MockTroll.TmT, TMT_CARACT);

        // Outr'Askaï
        scripts.get(PublicScript.ProfilPublic2).put(MockTroll.OutrAskai, OUTRASKAI_PROFIL_PUBLIC2);

        // Nanp
        scripts.get(PublicScript.ProfilPublic2).put(MockTroll.Nanp, NANP_PROFIL_PUBLIC2);
        scripts.get(PublicScript.Profil2).put(MockTroll.Nanp, NANP_PROFIL2);
        scripts.get(PublicScript.Caract).put(MockTroll.Nanp, NANP_CARACT);

    }


}
