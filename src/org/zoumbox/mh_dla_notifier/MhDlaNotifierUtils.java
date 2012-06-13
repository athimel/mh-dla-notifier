/*
 * #%L
 * MountyHall DLA Notifier
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2012 Zoumbox.org
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
package org.zoumbox.mh_dla_notifier;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import org.apache.commons.codec.binary.Hex;

import javax.annotation.Nullable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class MhDlaNotifierUtils {

    private static final String TAG = Constants.LOG_PREFIX + MhDlaNotifierUtils.class.getSimpleName();

    public static final String INTPUT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String HOUR_DATE_FORMAT = "HH:mm:ss";
    public static final String DAY_DATE_FORMAT = "dd MMM";
    public static final String DISPLAY_DATE_FORMAT = DAY_DATE_FORMAT + " - " + HOUR_DATE_FORMAT;

    /**
     * Encodes a raw byte[] to an hexadecimal String
     *
     * @param raw the byte[] to encode
     * @return the hexadecimal encoded text
     */
    public static String toHexadecimal(byte[] raw) {
        String result = null;
        if (raw != null) {
            result = new String(Hex.encodeHex(raw));
        }
        return result;
    }

    public static String md5(String text) {
        if (!Strings.isNullOrEmpty(text)) {
            try {
                // Create MD5 Hash
                MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
                digest.update(text.getBytes());
                byte messageDigest[] = digest.digest();

                String result = toHexadecimal(messageDigest);
                return result;

            } catch (NoSuchAlgorithmException nsae) {
                Log.e(TAG, "Algo MD5 non trouvé", nsae);
            }
        }
        return "";
    }

    public static Date parseDate(String input) {
        Date result = null;
        if (input != null) {
            DateFormat inputDF = new SimpleDateFormat(INTPUT_DATE_FORMAT);
            try {
                result = inputDF.parse(input);
            } catch (ParseException pe) {
                Log.e(TAG, "Date mal formatée", pe);
            }
        }
        return result;
    }

    public static String formatDate(String input) {
        Date date = parseDate(input);
        String result = formatDate(date);
        return result;
    }

    public static String formatDate(Date input) {
        String result = "n/c";
        if (input != null) {
            DateFormat outputDF = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.FRENCH);
            result = outputDF.format(input);
        }
        return result;
    }

    public static String formatDay(Date input) {
        String result = "n/c";
        if (input != null) {
            DateFormat outputDF = new SimpleDateFormat(DAY_DATE_FORMAT, Locale.FRENCH);
            result = outputDF.format(input);
        }
        return result;
    }

    public static String formatHour(Date input) {
        String result = "n/c";
        if (input != null) {
            DateFormat outputDF = new SimpleDateFormat(HOUR_DATE_FORMAT, Locale.FRENCH);
            result = outputDF.format(input);
        }
        return result;
    }

    public static final Function<Double, String> PRETTY_PRINT_DURATION = new Function<Double, String>() {
        @Override
        public String apply(Double duration) {
            int durationInt = ((Double)Math.floor(duration)).intValue();
            int durationHours = durationInt / 60;
            int durationMinutes = durationInt % 60;
            Double durationSeconds = 60d * (duration - Math.floor(duration));
            String result;
            String hours = String.format("%d heures", durationHours);
            String minutes = String.format("%d minute", durationMinutes);
            if (durationMinutes > 1) {
                minutes += "s";
            }
            if (durationSeconds > 0) {
                String seconds = String.format("%.0f seconde", durationSeconds);
                if (durationSeconds > 1) {
                    seconds += "s";
                }
                result = String.format("%s, %s et %s", hours, minutes, seconds);
            } else {
                result = String.format("%s et %s", hours, minutes);
            }

            return result;
        }
    };

    public static Date substractMinutes(Date date, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, -minutes);
        Date result = calendar.getTime();
        return result;
    }

    public static void toast(Context context, CharSequence message, Object... args) {
        if (message != null) {
            String messageString = message.toString();
            if (args != null) {
                messageString = String.format(messageString, args);
            }
            Toast.makeText(context, messageString, Toast.LENGTH_LONG).show();
        }
    }

}
