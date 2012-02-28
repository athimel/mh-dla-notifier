package org.zoumbox.mh.notifier.sp;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
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
