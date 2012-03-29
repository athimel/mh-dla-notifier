package org.zoumbox.mh_dla_notifier;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class Pair<L, R> {

    protected L left;
    protected R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L left() {
        return left;
    }

    public R right() {
        return right;
    }

}
