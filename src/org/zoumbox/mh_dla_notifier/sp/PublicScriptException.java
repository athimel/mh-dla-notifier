package org.zoumbox.mh_dla_notifier.sp;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class PublicScriptException extends Throwable {

    private static final long serialVersionUID = 1L;

    protected PublicScriptResponse spResult;

    public PublicScriptException(PublicScriptResponse spResult) {
        this.spResult = spResult;
    }

    @Override
    public String getMessage() {
        return spResult.getErrorMessage();
    }

}
