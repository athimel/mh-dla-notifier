/*
 * #%L
 * MountyHall DLA Notifier
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2012 - 2014 Zoumbox.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package org.zoumbox.mh_dla_notifier.sp;

import com.google.common.base.MoreObjects;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class PublicScriptResponse {

    protected String raw;
    protected long duration;

    protected String errorMessage;

    public PublicScriptResponse(String raw, long duration) {
        this.raw = raw;
        this.duration = duration;

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

    public long getDuration() {
        return duration;
    }

    public boolean hasError() {
        return errorMessage != null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).
                add("duration", duration + "ms").
                add("raw", raw).
                add("errorMessage", errorMessage).
                toString();
    }

}
