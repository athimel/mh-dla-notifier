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
package org.zoumbox.mh_dla_notifier.troll;

import java.util.Date;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class Troll {

    protected String numero;
    protected String nom;
    protected Race race;
    protected int nival;
    protected Date dateInscription;

    protected int fatigue;
    protected int posX;
    protected int posY;
    protected int posN;
    protected boolean camou;
    protected boolean invisible;
    protected boolean intangible;
    protected boolean immobile;
    protected boolean aTerre;
    protected boolean enCourse;
    protected boolean levitation;
    protected Date dla;
    protected int pa;
    protected String blason;
    protected int nbKills;
    protected int nbMorts;
    protected int guilde;
    protected int nbMouches;

    protected int rmCar;
    protected int mmCar;
    protected int attaqueCar;
    protected int esquiveCar;
    protected int degatsCar;
    protected int regenerationCar;
    protected int vueCar;
    protected int armureCar;
    protected double dureeDuTourCar;
    protected int pvMaxCar;
    protected int pvActuelsCar;
    protected double poidsCar;
    protected int concentrationCar;

    protected int rmBmm;
    protected int mmBmm;
    protected int attaqueBmm;
    protected int esquiveBmm;
    protected int degatsBmm;
    protected int regenerationBmm;
    protected int vueBmm;
    protected int armureBmm;
    protected double dureeDuTourBmm;
    protected int pvMaxBmm;
    protected int pvActuelsBmm;
    protected double poidsBmm;
    protected int concentrationBmm;

    protected int rmBmp;
    protected int mmBmp;
    protected int attaqueBmp;
    protected int esquiveBmp;
    protected int degatsBmp;
    protected int regenerationBmp;
    protected int vueBmp;
    protected int armureBmp;
    protected double dureeDuTourBmp;
    protected int pvMaxBmp;
    protected int pvActuelsBmp;
    protected double poidsBmp;
    protected int concentrationBmp;

    // Computed
    protected int pvVariation;

    public int getPv() {
        int result = getPvActuelsCar();
        return result;
    }

    @Override
    public String toString() {
        return "Troll{" +
                "numero='" + numero + '\'' +
                ", nom='" + nom + '\'' +
                ", race=" + race +
                ", nival=" + nival +
                ", dateInscription=" + dateInscription +
                ", pvVariation=" + pvVariation +
                ", fatigue=" + fatigue +
                ", posX=" + posX +
                ", posY=" + posY +
                ", posN=" + posN +
                ", camou=" + camou +
                ", invisible=" + invisible +
                ", intangible=" + intangible +
                ", immobile=" + immobile +
                ", aTerre=" + aTerre +
                ", enCourse=" + enCourse +
                ", levitation=" + levitation +
                ", dla=" + dla +
                ", pa=" + pa +
                ", blason='" + blason + '\'' +
                ", nbKills=" + nbKills +
                ", nbMorts=" + nbMorts +
                ", guilde=" + guilde +
                ", nbMouches=" + nbMouches +
                ", rmCar=" + rmCar +
                ", rmBmm=" + rmBmm +
                ", rmBmp=" + rmBmp +
                ", mmCar=" + mmCar +
                ", mmBmm=" + mmBmm +
                ", mmBmp=" + mmBmp +
                ", attaqueCar=" + attaqueCar +
                ", attaqueBmm=" + attaqueBmm +
                ", attaqueBmp=" + attaqueBmp +
                ", esquiveCar=" + esquiveCar +
                ", esquiveBmm=" + esquiveBmm +
                ", esquiveBmp=" + esquiveBmp +
                ", degatsCar=" + degatsCar +
                ", degatsBmm=" + degatsBmm +
                ", degatsBmp=" + degatsBmp +
                ", regenerationCar=" + regenerationCar +
                ", regenerationBmm=" + regenerationBmm +
                ", regenerationBmp=" + regenerationBmp +
                ", vueCar=" + vueCar +
                ", vueBmm=" + vueBmm +
                ", vueBmp=" + vueBmp +
                ", armureCar=" + armureCar +
                ", armureBmm=" + armureBmm +
                ", armureBmp=" + armureBmp +
                ", dureeDuTourCar=" + dureeDuTourCar +
                ", dureeDuTourBmm=" + dureeDuTourBmm +
                ", dureeDuTourBmp=" + dureeDuTourBmp +
                ", pvMaxCar=" + pvMaxCar +
                ", pvMaxBmm=" + pvMaxBmm +
                ", pvMaxBmp=" + pvMaxBmp +
                ", pvActuelsCar=" + pvActuelsCar +
                ", pvActuelsBmm=" + pvActuelsBmm +
                ", pvActuelsBmp=" + pvActuelsBmp +
                ", poidsCar=" + poidsCar +
                ", poidsBmm=" + poidsBmm +
                ", poidsBmp=" + poidsBmp +
                ", concentrationCar=" + concentrationCar +
                ", concentrationBmm=" + concentrationBmm +
                ", concentrationBmp=" + concentrationBmp +
                '}';
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Race getRace() {
        return race;
    }

    public void setRace(Race race) {
        this.race = race;
    }

    public int getNival() {
        return nival;
    }

    public void setNival(int nival) {
        this.nival = nival;
    }

    public int getNbMouches() {
        return nbMouches;
    }

    public void setNbMouches(int nbMouches) {
        this.nbMouches = nbMouches;
    }

    public Date getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(Date dateInscription) {
        this.dateInscription = dateInscription;
    }

    public int getPvVariation() {
        return pvVariation;
    }

    public void setPvVariation(int pvVariation) {
        this.pvVariation = pvVariation;
    }

    public int getFatigue() {
        return fatigue;
    }

    public void setFatigue(int fatigue) {
        this.fatigue = fatigue;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public int getPosN() {
        return posN;
    }

    public void setPosN(int posN) {
        this.posN = posN;
    }

    public boolean isCamou() {
        return camou;
    }

    public void setCamou(boolean camou) {
        this.camou = camou;
    }

    public boolean isInvisible() {
        return invisible;
    }

    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }

    public boolean isIntangible() {
        return intangible;
    }

    public void setIntangible(boolean intangible) {
        this.intangible = intangible;
    }

    public boolean isImmobile() {
        return immobile;
    }

    public void setImmobile(boolean immobile) {
        this.immobile = immobile;
    }

    public boolean isATerre() {
        return aTerre;
    }

    public void setATerre(boolean aTerre) {
        this.aTerre = aTerre;
    }

    public boolean isEnCourse() {
        return enCourse;
    }

    public void setEnCourse(boolean enCourse) {
        this.enCourse = enCourse;
    }

    public boolean isLevitation() {
        return levitation;
    }

    public void setLevitation(boolean levitation) {
        this.levitation = levitation;
    }

    public Date getDla() {
        return dla;
    }

    public void setDla(Date dla) {
        this.dla = dla;
    }

    public int getPa() {
        return pa;
    }

    public void setPa(int pa) {
        this.pa = pa;
    }

    public String getBlason() {
        return blason;
    }

    public void setBlason(String blason) {
        this.blason = blason;
    }

    public int getNbKills() {
        return nbKills;
    }

    public void setNbKills(int nbKills) {
        this.nbKills = nbKills;
    }

    public int getNbMorts() {
        return nbMorts;
    }

    public void setNbMorts(int nbMorts) {
        this.nbMorts = nbMorts;
    }

    public int getGuilde() {
        return guilde;
    }

    public void setGuilde(int guilde) {
        this.guilde = guilde;
    }

//    public int getPvBM() {
//        return pvBM;
//    }
//
//    public void setPvBM(int pvBM) {
//        this.pvBM = pvBM;
//    }
//
//    public int getDlaBM() {
//        return dlaBM;
//    }
//
//    public void setDlaBM(int dlaBM) {
//        this.dlaBM = dlaBM;
//    }
//
//    public int getPoids() {
//        return poids;
//    }
//
//    public void setPoids(int poids) {
//        this.poids = poids;
//    }
//
//    public UpdateRequestType getUpdateRequestType() {
//        return updateRequestType;
//    }
//
//    public void setUpdateRequestType(UpdateRequestType updateRequestType) {
//        this.updateRequestType = updateRequestType;
//    }
//
//    public void setComputedPvMax(int computedPvMax) {
//        this.computedPvMax = computedPvMax;
//    }
//
//    public void setComputedNextDla(Date computedNextDla) {
//        this.computedNextDla = computedNextDla;
//    }

    public int getRmCar() {
        return rmCar;
    }

    public void setRmCar(int rmCar) {
        this.rmCar = rmCar;
    }

    public int getMmCar() {
        return mmCar;
    }

    public void setMmCar(int mmCar) {
        this.mmCar = mmCar;
    }

    public int getAttaqueCar() {
        return attaqueCar;
    }

    public void setAttaqueCar(int attaqueCar) {
        this.attaqueCar = attaqueCar;
    }

    public int getEsquiveCar() {
        return esquiveCar;
    }

    public void setEsquiveCar(int esquiveCar) {
        this.esquiveCar = esquiveCar;
    }

    public int getDegatsCar() {
        return degatsCar;
    }

    public void setDegatsCar(int degatsCar) {
        this.degatsCar = degatsCar;
    }

    public int getRegenerationCar() {
        return regenerationCar;
    }

    public void setRegenerationCar(int regenerationCar) {
        this.regenerationCar = regenerationCar;
    }

    public int getVueCar() {
        return vueCar;
    }

    public void setVueCar(int vueCar) {
        this.vueCar = vueCar;
    }

    public int getArmureCar() {
        return armureCar;
    }

    public void setArmureCar(int armureCar) {
        this.armureCar = armureCar;
    }

    public double getDureeDuTourCar() {
        return dureeDuTourCar;
    }

    public void setDureeDuTourCar(double dureeDuTourCar) {
        this.dureeDuTourCar = dureeDuTourCar;
    }

    public int getPvMaxCar() {
        return pvMaxCar;
    }

    public void setPvMaxCar(int pvMaxCar) {
        this.pvMaxCar = pvMaxCar;
    }

    public int getPvActuelsCar() {
        return pvActuelsCar;
    }

    public void setPvActuelsCar(int pvActuelsCar) {
        this.pvActuelsCar = pvActuelsCar;
    }

    public double getPoidsCar() {
        return poidsCar;
    }

    public void setPoidsCar(double poidsCar) {
        this.poidsCar = poidsCar;
    }

    public int getConcentrationCar() {
        return concentrationCar;
    }

    public void setConcentrationCar(int concentrationCar) {
        this.concentrationCar = concentrationCar;
    }

    public int getRmBmm() {
        return rmBmm;
    }

    public void setRmBmm(int rmBmm) {
        this.rmBmm = rmBmm;
    }

    public int getMmBmm() {
        return mmBmm;
    }

    public void setMmBmm(int mmBmm) {
        this.mmBmm = mmBmm;
    }

    public int getAttaqueBmm() {
        return attaqueBmm;
    }

    public void setAttaqueBmm(int attaqueBmm) {
        this.attaqueBmm = attaqueBmm;
    }

    public int getEsquiveBmm() {
        return esquiveBmm;
    }

    public void setEsquiveBmm(int esquiveBmm) {
        this.esquiveBmm = esquiveBmm;
    }

    public int getDegatsBmm() {
        return degatsBmm;
    }

    public void setDegatsBmm(int degatsBmm) {
        this.degatsBmm = degatsBmm;
    }

    public int getRegenerationBmm() {
        return regenerationBmm;
    }

    public void setRegenerationBmm(int regenerationBmm) {
        this.regenerationBmm = regenerationBmm;
    }

    public int getVueBmm() {
        return vueBmm;
    }

    public void setVueBmm(int vueBmm) {
        this.vueBmm = vueBmm;
    }

    public int getArmureBmm() {
        return armureBmm;
    }

    public void setArmureBmm(int armureBmm) {
        this.armureBmm = armureBmm;
    }

    public double getDureeDuTourBmm() {
        return dureeDuTourBmm;
    }

    public void setDureeDuTourBmm(double dureeDuTourBmm) {
        this.dureeDuTourBmm = dureeDuTourBmm;
    }

    public int getPvMaxBmm() {
        return pvMaxBmm;
    }

    public void setPvMaxBmm(int pvMaxBmm) {
        this.pvMaxBmm = pvMaxBmm;
    }

    public int getPvActuelsBmm() {
        return pvActuelsBmm;
    }

    public void setPvActuelsBmm(int pvActuelsBmm) {
        this.pvActuelsBmm = pvActuelsBmm;
    }

    public double getPoidsBmm() {
        return poidsBmm;
    }

    public void setPoidsBmm(double poidsBmm) {
        this.poidsBmm = poidsBmm;
    }

    public int getConcentrationBmm() {
        return concentrationBmm;
    }

    public void setConcentrationBmm(int concentrationBmm) {
        this.concentrationBmm = concentrationBmm;
    }

    public int getRmBmp() {
        return rmBmp;
    }

    public void setRmBmp(int rmBmp) {
        this.rmBmp = rmBmp;
    }

    public int getMmBmp() {
        return mmBmp;
    }

    public void setMmBmp(int mmBmp) {
        this.mmBmp = mmBmp;
    }

    public int getAttaqueBmp() {
        return attaqueBmp;
    }

    public void setAttaqueBmp(int attaqueBmp) {
        this.attaqueBmp = attaqueBmp;
    }

    public int getEsquiveBmp() {
        return esquiveBmp;
    }

    public void setEsquiveBmp(int esquiveBmp) {
        this.esquiveBmp = esquiveBmp;
    }

    public int getDegatsBmp() {
        return degatsBmp;
    }

    public void setDegatsBmp(int degatsBmp) {
        this.degatsBmp = degatsBmp;
    }

    public int getRegenerationBmp() {
        return regenerationBmp;
    }

    public void setRegenerationBmp(int regenerationBmp) {
        this.regenerationBmp = regenerationBmp;
    }

    public int getVueBmp() {
        return vueBmp;
    }

    public void setVueBmp(int vueBmp) {
        this.vueBmp = vueBmp;
    }

    public int getArmureBmp() {
        return armureBmp;
    }

    public void setArmureBmp(int armureBmp) {
        this.armureBmp = armureBmp;
    }

    public double getDureeDuTourBmp() {
        return dureeDuTourBmp;
    }

    public void setDureeDuTourBmp(double dureeDuTourBmp) {
        this.dureeDuTourBmp = dureeDuTourBmp;
    }

    public int getPvMaxBmp() {
        return pvMaxBmp;
    }

    public void setPvMaxBmp(int pvMaxBmp) {
        this.pvMaxBmp = pvMaxBmp;
    }

    public int getPvActuelsBmp() {
        return pvActuelsBmp;
    }

    public void setPvActuelsBmp(int pvActuelsBmp) {
        this.pvActuelsBmp = pvActuelsBmp;
    }

    public double getPoidsBmp() {
        return poidsBmp;
    }

    public void setPoidsBmp(double poidsBmp) {
        this.poidsBmp = poidsBmp;
    }

    public int getConcentrationBmp() {
        return concentrationBmp;
    }

    public void setConcentrationBmp(int concentrationBmp) {
        this.concentrationBmp = concentrationBmp;
    }
}
