package org.zoumbox.mh_dla_notifier.sp;

/**
 * @author Arno <arno@zoumbox.org>
 */
public enum ScriptCategory {
    DYNAMIC(24),
    STATIC(10),
    MESSAGES(12),
    CALLS(4);

    protected int quota;

    ScriptCategory(int quota) {
        this.quota = quota;
    }

    public int getQuota() {
        return quota;
    }

}
