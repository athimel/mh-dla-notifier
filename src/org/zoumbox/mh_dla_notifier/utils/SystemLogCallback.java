package org.zoumbox.mh_dla_notifier.utils;

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
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class SystemLogCallback implements LogCallback {

    @Override
    public int v(String tag, String msg) {
        String format = String.format("[VERBOSE] %s %s", tag, msg);
        System.out.println(format);
        return 0;
    }

    @Override
    public int v(String tag, String msg, Throwable tr) {
        String format = String.format("[VERBOSE] %s %s %s", tag, msg, tr);
        System.out.println(format);
        return 0;
    }

    @Override
    public int d(String tag, String msg) {
        String format = String.format("[DEBUG] %s %s", tag, msg);
        System.out.println(format);
        return 0;
    }

    @Override
    public int d(String tag, String msg, Throwable tr) {
        String format = String.format("[DEBUG] %s %s %s", tag, msg, tr);
        System.out.println(format);
        return 0;
    }

    @Override
    public int i(String tag, String msg) {
        String format = String.format("[INFO] %s %s", tag, msg);
        System.out.println(format);
        return 0;
    }

    @Override
    public int i(String tag, String msg, Throwable tr) {
        String format = String.format("[INFO] %s %s %s", tag, msg, tr);
        System.out.println(format);
        return 0;
    }

    @Override
    public int w(String tag, String msg) {
        String format = String.format("[WARN] %s %s", tag, msg);
        System.out.println(format);
        return 0;
    }

    @Override
    public int w(String tag, String msg, Throwable tr) {
        String format = String.format("[WARN] %s %s %s", tag, msg, tr);
        System.out.println(format);
        return 0;
    }

    @Override
    public int w(String tag, Throwable tr) {
        String format = String.format("[WARN] %s %s", tag, tr);
        System.out.println(format);
        return 0;
    }

    @Override
    public int e(String tag, String msg) {
        String format = String.format("[ERROR] %s %s", tag, msg);
        System.out.println(format);
        return 0;
    }

    @Override
    public int e(String tag, String msg, Throwable tr) {
        String format = String.format("[ERROR] %s %s %s", tag, msg, tr);
        System.out.println(format);
        return 0;
    }
}
