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

import org.zoumbox.mh_dla_notifier.MhDlaException;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class PublicScriptException extends MhDlaException {

    private static final long serialVersionUID = 1L;

    protected PublicScriptResponse spResult;

    public PublicScriptException(PublicScriptResponse spResult) {
        this.spResult = spResult;
    }

    public PublicScriptException(Throwable cause) {
        super(cause);
    }

    public PublicScriptException(PublicScriptException cause) {
        super(cause);
        this.spResult = cause.spResult;
    }

    @Override
    public String getMessage() {
        String result;
        if (spResult != null) {
            result = spResult.getErrorMessage();
        } else {
            result = getCause().getMessage();
        }
        return result;
    }

    @Override
    public String getText() {
        return getMessage();
    }
}
