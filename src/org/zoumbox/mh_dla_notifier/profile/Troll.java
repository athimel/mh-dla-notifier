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
