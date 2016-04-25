package org.zoumbox.mh_dla_notifier.sp;

import java.util.Date;

import org.zoumbox.mh_dla_notifier.MhDlaException;

/**
 * @author Arnaud Thimel (Code Lutin)
 */
public class HighUpdateRateException extends MhDlaException {
    protected PublicScript script;
    protected Date lastRequest;
    public HighUpdateRateException(PublicScript script, Date lastRequest) {
        this.script = script;
        this.lastRequest = lastRequest;
    }

    public HighUpdateRateException(HighUpdateRateException cause) {
        super(cause);
        this.script = cause.script;
        this.lastRequest = cause.lastRequest;
    }

    public Date getLastRequest() {
        return lastRequest;
    }

    public PublicScript getScript() {
        return script;
    }

    @Override
    public String getText() {
        return "Dernier appel au script " + script + ": " + lastRequest;
    }

}
