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
package org.zoumbox.mh_dla_notifier.profile;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class Troll {

    public static final Function<Mouche,MoucheType> GET_MOUCHE_TYPE = new Function<Mouche, MoucheType>() {
        @Override
        public MoucheType apply(Mouche mouche) {
            return mouche.type;
        }
    };

    public String id, nom;
    public Race race;
    public int nival;
    public int pv, pvMaxBase;
    public int posX, posY, posN;
    public boolean camou, invisible, intangible;
    public int dureeDuTour;
    public Date dla;
    public int pa;
    public List<Mouche> mouches;
    public String blason;
    public int nbKills, nbMorts;

    public UpdateRequestType updateRequestType;

    // Computed
    int pvMax = -1;

    public int getPvMax() {
        if (pvMax == -1) {
            int nbTelaites = Iterables.frequency(
                    Iterables.transform(mouches, GET_MOUCHE_TYPE),
                    MoucheType.Telaite);
            pvMax = pvMaxBase + nbTelaites * 5;
        }
        return pvMax;
    }
}
