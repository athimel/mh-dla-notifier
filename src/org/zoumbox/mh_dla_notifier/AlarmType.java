package org.zoumbox.mh_dla_notifier;

/**
 * Utilisé pour différencier les alarmes
 */
public enum AlarmType {
    CURRENT_DLA(86956675),
    AFTER_CURRENT_DLA(57665968),
    NEXT_DLA(81913480),
    AFTER_NEXT_DLA(8431918);

    protected int identifier;

    AlarmType(int identifier) {
        this.identifier = identifier;
    }

    public int getIdentifier() {
        return identifier;
    }

}
