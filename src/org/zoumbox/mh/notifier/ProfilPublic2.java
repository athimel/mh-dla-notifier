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
public class ProfilPublic2 {

    public static final String FORMAT = "yyyy-MM-dd HH:mm:ss";

    protected int numero;
    protected String nom;
    protected Race race;
    protected int niveau;
    protected Date dateInscription;
    protected String email;
    protected String blason;
    protected String intangible;
    protected int nbMouches;
    protected int nbKills;
    protected int nbMorts;
    protected int numeroDeGuilde;
    protected int niveauDeRang;
    protected boolean pnj;

    public ProfilPublic2(String raw) {
        Iterable<String> iterable = Splitter.on(";").split(raw);
        List<String> data = Lists.newArrayList(iterable);

        DateFormat format = new SimpleDateFormat(FORMAT);

        numero = Integer.parseInt(data.get(0));
        nom = data.get(1);
        race = Race.valueOf(data.get(2));
        niveau = Integer.parseInt(data.get(3));
        try {
            dateInscription = format.parse(data.get(4));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        email = data.get(5);
        blason = data.get(6);
//        intangible = data.get(7);
        nbMouches = Integer.parseInt(data.get(7));
        nbKills = Integer.parseInt(data.get(8));
        nbMorts = Integer.parseInt(data.get(9));
        numeroDeGuilde = Integer.parseInt(data.get(10));
        niveauDeRang = Integer.parseInt(data.get(11));
        pnj = "1".equals(data.get(12));
    }

    public int getNumero() {
        return numero;
    }

    public String getNom() {
        return nom;
    }

    public Race getRace() {
        return race;
    }

    public int getNiveau() {
        return niveau;
    }

    public Date getDateInscription() {
        return dateInscription;
    }

    public String getEmail() {
        return email;
    }

    public String getBlason() {
        return blason;
    }

    public String getIntangible() {
        return intangible;
    }

    public int getNbMouches() {
        return nbMouches;
    }

    public int getNbKills() {
        return nbKills;
    }

    public int getNbMorts() {
        return nbMorts;
    }

    public int getNumeroDeGuilde() {
        return numeroDeGuilde;
    }

    public int getNiveauDeRang() {
        return niveauDeRang;
    }

    public boolean isPnj() {
        return pnj;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("numero", numero)
                .add("nom", nom)
                .add("race", race)
                .add("niveau", niveau)
                .add("dateInscription", dateInscription)
                .add("email", email)
                .add("blason", blason)
                .add("nbMouches", nbMouches)
                .add("nbKills", nbKills)
                .add("nbMorts", nbMorts)
                .add("numeroDeGuilde", numeroDeGuilde)
                .add("niveauDeRang", niveauDeRang)
                .add("pnj", pnj)
                .toString();
    }

    public static void main(String[] args) {
        ProfilPublic2 profilPublic2 = new ProfilPublic2("104259;DevelZimZoum;Kastar;18;2011-01-21 14:07:48;;http://zoumbox.org/mh/DevelZimZoumMH.png;17;117;9;1900;20;0");
        System.out.println(profilPublic2);
    }

}
