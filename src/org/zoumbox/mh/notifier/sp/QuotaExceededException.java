package org.zoumbox.mh.notifier.sp;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class QuotaExceededException extends Exception {

    private static final long serialVersionUID = 1L;

    protected ScriptCategory category;
    protected int count;

    public QuotaExceededException(ScriptCategory category, int count) {
        this.category = category;
    }

    public ScriptCategory getCategory() {
        return category;
    }

    public int getCount() {
        return count;
    }
}
