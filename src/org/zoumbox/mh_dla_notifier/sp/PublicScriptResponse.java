package org.zoumbox.mh_dla_notifier.sp;

import com.google.common.base.Objects;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class PublicScriptResponse {

    protected String raw;

    protected String errorMessage;

    public PublicScriptResponse(String raw) {
        this.raw = raw;

        if (raw == null) {
            errorMessage = "Erreur inconnue";
        } else if (raw.startsWith("Erreur ")) {
            errorMessage = raw;
        }
    }

    public String getRaw() {
        return raw;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean hasError() {
        return errorMessage != null;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).
                add("raw", raw).
                add("errorMessage", errorMessage).
                toString();
    }

}
