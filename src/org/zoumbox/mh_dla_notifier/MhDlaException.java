package org.zoumbox.mh_dla_notifier;

/**
 */
public abstract class MhDlaException extends Exception {

    public MhDlaException() {
    }

    public MhDlaException(String detailMessage) {
        super(detailMessage);
    }

    public MhDlaException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public MhDlaException(Throwable cause) {
        super(cause);
    }

    public abstract String getText();

}
