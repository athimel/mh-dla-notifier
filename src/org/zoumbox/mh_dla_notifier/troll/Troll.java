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

import org.zoumbox.mh_dla_notifier.profile.UpdateRequestType;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class Troll {

    protected String id;
    protected String nom;
    protected Race race;
    protected int nival;
    protected Date dateInscription;

    protected int pv;
    protected int pvMaxBase;
    protected int pvVariation;
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
    protected int dureeDuTour;
    protected Date dla;
    protected int pa;
    protected String blason;
    protected int nbKills, nbMorts;
    protected int guilde;

    protected int pvBM;
    protected int dlaBM;
    protected int poids;

    protected UpdateRequestType updateRequestType;

    // Computed
    protected int computedPvMax = -1;
    protected Date computedNextDla = null;


    public int getComputedPvMax() {
        if (computedPvMax == -1) {
            computedPvMax = Trolls.GET_MAX_PV.apply(this);
        }
        return computedPvMax;
    }

    public Date getComputedNextDla() {
        if (computedNextDla == null) {
            computedNextDla = Trolls.GET_NEXT_DLA.apply(this);
        }
        return computedNextDla;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Date getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(Date dateInscription) {
        this.dateInscription = dateInscription;
    }

    public int getPv() {
        return pv;
    }

    public void setPv(int pv) {
        this.pv = pv;
    }

    public int getPvMaxBase() {
        return pvMaxBase;
    }

    public void setPvMaxBase(int pvMaxBase) {
        this.pvMaxBase = pvMaxBase;
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

    public boolean isaTerre() {
        return aTerre;
    }

    public void setaTerre(boolean aTerre) {
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

    public int getDureeDuTour() {
        return dureeDuTour;
    }

    public void setDureeDuTour(int dureeDuTour) {
        this.dureeDuTour = dureeDuTour;
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

    public int getPvBM() {
        return pvBM;
    }

    public void setPvBM(int pvBM) {
        this.pvBM = pvBM;
    }

    public int getDlaBM() {
        return dlaBM;
    }

    public void setDlaBM(int dlaBM) {
        this.dlaBM = dlaBM;
    }

    public int getPoids() {
        return poids;
    }

    public void setPoids(int poids) {
        this.poids = poids;
    }

    public UpdateRequestType getUpdateRequestType() {
        return updateRequestType;
    }

    public void setUpdateRequestType(UpdateRequestType updateRequestType) {
        this.updateRequestType = updateRequestType;
    }

    public void setComputedPvMax(int computedPvMax) {
        this.computedPvMax = computedPvMax;
    }

    public void setComputedNextDla(Date computedNextDla) {
        this.computedNextDla = computedNextDla;
    }
}
