package org.zoumbox.mh_dla_notifier;

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

/**
 * Utilisé pour différencier les alarmes
 */
public enum AlarmType {
    CURRENT_DLA(111111),
    NEXT_DLA(333333),

    AFTER_CURRENT_DLA(222222),
    AFTER_NEXT_DLA(444444),

    NEXT_DLA_ACTIVATION(300000),
    DLA_EVEN_AFTER_ACTIVATION(555555),
    DLA_EVEN_EVEN_AFTER_ACTIVATION(666666),

    IN_THE_FUTURE_ACTIVATION(999999);

    protected int identifier;

    AlarmType(int identifier) {
        this.identifier = identifier;
    }

    public int getIdentifier() {
        return identifier;
    }

}
