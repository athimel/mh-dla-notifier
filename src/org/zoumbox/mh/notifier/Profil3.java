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
public class Profil3 {

    public static final String FORMAT = "yyyy-MM-dd HH:mm:ss";

    protected int numero;
    protected String nom;
    protected int posX;
    protected int posY;
    protected int posN;
    protected int paRestant;
    protected Date dla;
    protected int fatigue;
    protected boolean camou;
    protected boolean invisible;
    protected boolean intangible;
    protected int px;
    protected int pxPerso;
    protected int pi;
    protected int gg;

    public Profil3(String raw) {
        Iterable<String> iterable = Splitter.on(";").split(raw);
        List<String> data = Lists.newArrayList(iterable);

        DateFormat format = new SimpleDateFormat(FORMAT);

        numero = Integer.parseInt(data.get(0));
        nom = data.get(1);
        posX = Integer.parseInt(data.get(2));
        posY = Integer.parseInt(data.get(3));
        posN = Integer.parseInt(data.get(4));
        paRestant = Integer.parseInt(data.get(5));
        try {
            dla = format.parse(data.get(6));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        fatigue = Integer.parseInt(data.get(7));
        camou = "1".equals(data.get(8));
        invisible = "1".equals(data.get(9));
        intangible = "1".equals(data.get(10));
        px = Integer.parseInt(data.get(11));
        pxPerso = Integer.parseInt(data.get(12));
        pi = Integer.parseInt(data.get(13));
        gg = Integer.parseInt(data.get(14));
    }

    public int getNumero() {
        return numero;
    }

    public String getNom() {
        return nom;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public int getPosN() {
        return posN;
    }

    public int getPaRestant() {
        return paRestant;
    }

    public Date getDla() {
        return dla;
    }

    public int getFatigue() {
        return fatigue;
    }

    public boolean isCamou() {
        return camou;
    }

    public boolean isInvisible() {
        return invisible;
    }

    public boolean isIntangible() {
        return intangible;
    }

    public int getPx() {
        return px;
    }

    public int getPxPerso() {
        return pxPerso;
    }

    public int getPi() {
        return pi;
    }

    public int getGg() {
        return gg;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("numero", numero)
                .add("nom", nom)
                .add("posX", posX)
                .add("posY", posY)
                .add("posN", posN)
                .add("paRestant", paRestant)
                .add("dla", dla)
                .add("fatigue", fatigue)
                .add("camou", camou)
                .add("invisible", invisible)
                .add("intangible", intangible)
                .add("px", px)
                .add("pxPerso", pxPerso)
                .add("pi", pi)
                .add("gg", gg)
                .toString();
    }

    public static void main(String[] args) {
        Profil3 profil3 = new Profil3("104259;DevelZimZoum;57;-75;-41;6;2012-02-25 01:22:55;3;0;0;0;2;22;88;6042");
        System.out.println(profil3);
    }

}
