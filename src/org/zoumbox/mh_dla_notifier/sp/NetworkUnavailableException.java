package org.zoumbox.mh_dla_notifier.sp;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class NetworkUnavailableException extends Exception {

    private static final long serialVersionUID = 1L;

    public NetworkUnavailableException(Exception source) {
        super(source);
    }

}
