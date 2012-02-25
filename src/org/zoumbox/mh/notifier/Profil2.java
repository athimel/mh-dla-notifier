package org.zoumbox.mh.notifier;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class Profil2 {

    public static final String FORMAT = "yyyy-MM-dd HH:mm:ss";

    protected int numero;
    protected int posX;
    protected int posY;
    protected int posN;
    protected int pv;
    protected int pvMax;
    protected int paRestant;
    protected Date dla;
    protected int attaque;
    protected int esquive;
    protected int degats;
    protected int regeneration;
    protected int vue;
    protected int armure;
    protected int mm;
    protected int rm;
    protected int attaquesSubies;
    protected int fatigue;
    protected boolean camou;
    protected boolean invisible;
    protected boolean intangible;
    protected int nbParadeProgrammes;
    protected int nbContreAttaquesProgrammes;
    protected int dureeDuTour;
    protected int bonusDuree;
    protected int armureNaturelle;
    protected int desDArmureEnMoins;

    public Profil2(String raw) {
        Iterable<String> iterable = Splitter.on(";").split(raw);
        List<String> data = Lists.newArrayList(iterable);

        DateFormat format = new SimpleDateFormat(FORMAT);

        numero = Integer.parseInt(data.get(0));
        posX = Integer.parseInt(data.get(1));
        posY = Integer.parseInt(data.get(2));
        posN = Integer.parseInt(data.get(3));
        pv = Integer.parseInt(data.get(4));
        pvMax = Integer.parseInt(data.get(5));
        paRestant = Integer.parseInt(data.get(6));
        try {
            dla = format.parse(data.get(7));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        attaque = Integer.parseInt(data.get(8));
        esquive = Integer.parseInt(data.get(9));
        degats = Integer.parseInt(data.get(10));
        regeneration = Integer.parseInt(data.get(11));
        vue = Integer.parseInt(data.get(12));
        armure = Integer.parseInt(data.get(13));
        mm = Integer.parseInt(data.get(14));
        rm = Integer.parseInt(data.get(15));
        attaquesSubies = Integer.parseInt(data.get(16));
        fatigue = Integer.parseInt(data.get(17));
        camou = "1".equals(data.get(18));
        invisible = "1".equals(data.get(19));
        intangible = "1".equals(data.get(20));
        nbParadeProgrammes = Integer.parseInt(data.get(21));
        nbContreAttaquesProgrammes = Integer.parseInt(data.get(22));
        dureeDuTour = Integer.parseInt(data.get(23));
        bonusDuree = Integer.parseInt(data.get(24));
        armureNaturelle = Integer.parseInt(data.get(25));
        desDArmureEnMoins = Integer.parseInt(data.get(26));
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("numero", numero)
                .add("posX", posX)
                .add("posY", posY)
                .add("posN", posN)
                .add("paRestant", paRestant)
                .add("dla", dla)
                .add("fatigue", fatigue)
                .add("camou", camou)
                .add("invisible", invisible)
                .add("intangible", intangible)
//                .add("px", px)
//                .add("pxPerso", pxPerso)
//                .add("pi", pi)
//                .add("gg", gg)
                .toString();
    }

    public static void main(String[] args) {
        Profil2 profil2 = new Profil2("104259;57;-75;-41;85;80;6;2012-02-25 01:22:55;8;4;13;4;4;6;359;356;0;3;0;0;0;0;0;585;0;1;0");
        System.out.println(profil2);
    }

}
