package org.zoumbox.mh_dla_notifier;

/**
 * Utilisé pour différencier les alarmes
 */
public enum AlarmType {
    CURRENT_DLA(111111),
    AFTER_CURRENT_DLA(222222),
    NEXT_DLA(333333),
    AFTER_NEXT_DLA(444444),
    DLA_EVEN_AFTER(555555);

    protected int identifier;

    AlarmType(int identifier) {
        this.identifier = identifier;
    }

    public int getIdentifier() {
        return identifier;
    }

}
