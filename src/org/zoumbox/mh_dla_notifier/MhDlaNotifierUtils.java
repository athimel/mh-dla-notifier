package org.zoumbox.mh_dla_notifier;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.google.common.base.Strings;
import org.apache.commons.codec.binary.Hex;

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
    public static final String DAY_DATE_FORMAT = "dd MMM yyyy";
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
