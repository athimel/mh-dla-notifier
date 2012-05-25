package org.zoumbox.mh_dla_notifier.profile;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public enum UpdateRequestType {
    NONE,
    FULL,
    ONLY_NECESSARY;

    public boolean needUpdate() {
        boolean result = FULL.equals(this) || ONLY_NECESSARY.equals(this);
        return result;
    }
}
