package org.zoumbox.mh_dla_notifier.sp;

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

import java.util.Date;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class MhSpRequest {

    protected static final long M_24_HOURS = 24L * 60L * 60L * 1000L;

    protected PublicScript script;
    protected long duration;
    protected Date date;
    protected String status;

    public MhSpRequest(Date date, long duration, PublicScript script, String status) {
        this.date = date;
        this.duration = duration;
        this.script = script;
        this.status = status;
    }

    public PublicScript getScript() {
        return script;
    }

    public void setScript(PublicScript script) {
        this.script = script;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean lessThan24Hours() {
        boolean result = System.currentTimeMillis() - date.getTime() < M_24_HOURS;
        return result;
    }

    @Override
    public String toString() {
        String result = String.format("MhSpRequest{ date=%s%s, script=%8s %s, status=%s, duration=%dms }",
                date, lessThan24Hours() ? " (*)":"", script.getCategory().name(), script.name(), status, duration);
        return result;
    }
}
