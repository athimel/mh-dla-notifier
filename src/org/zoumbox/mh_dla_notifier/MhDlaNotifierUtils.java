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
package org.zoumbox.mh_dla_notifier;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Hex;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class MhDlaNotifierUtils {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + MhDlaNotifierUtils.class.getSimpleName();

    public static final int BLASON_MAX_SIZE = 1024;
    public static final int BLASON_WIDGET_MAX_SIZE = 300;

    public static final String INTPUT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String HOUR_DATE_FORMAT = "HH:mm:ss";
    public static final String HOUR_NO_SEC_DATE_FORMAT = "HH:mm";
    public static final String DAY_DATE_FORMAT = "dd MMM";
    public static final String DISPLAY_DATE_FORMAT = DAY_DATE_FORMAT + " - " + HOUR_DATE_FORMAT;

    public static final String BLASON_FILES_PREFIX = "v0.13.3_";

    protected static final String N_C = "n/c";

    public static final Predicate<Date> IS_IN_THE_FUTURE = new Predicate<Date>() {
        @Override
        public boolean apply(Date date) {
            if (date == null) {
                return false;
            }
            boolean result = date.after(new Date());
            return result;
        }
    };
    protected static final int CROP_PADDING = 10;

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

    public static TimeZone getSpTimeZone() {
        TimeZone result = TimeZone.getTimeZone("Europe/Paris");
        return result;
    }

    public static TimeZone getDisplayTimeZone(Context context) {
        PreferencesHolder preferences = PreferencesHolder.load(context);
        TimeZone result = getDisplayTimeZone(preferences.timeZoneId);
        return result;
    }

    public static TimeZone getDisplayTimeZone(String timeZoneId) {
        TimeZone result;
        if (Strings.isNullOrEmpty(timeZoneId) || "default".equalsIgnoreCase(timeZoneId)) {
            // Heure du serveur MH
            result = getSpTimeZone();
        } else if ("system".equalsIgnoreCase(timeZoneId)) {
            // Heure du téléphone
            result = TimeZone.getDefault();
        } else {
            // Heure custom
            try {
                result = TimeZone.getTimeZone(timeZoneId);
            } catch (Exception eee) {
                result = getSpTimeZone();
            }
        }
        return result;
    }

    public static Date parseSpDate(String input) {
        Date result = null;
        if (input != null) {
            DateFormat inputDF = new SimpleDateFormat(INTPUT_DATE_FORMAT);
            inputDF.setTimeZone(getSpTimeZone());
            try {
                result = inputDF.parse(input);
            } catch (ParseException pe) {
                Log.e(TAG, "Date mal formatée ", pe);
            }
        }
        return result;
    }

//    public static String formatDate(String input) {
//        Date date = parseSpDate(input);
//        String result = formatDate(date);
//        return result;
//    }

    public static String formatDLAForDisplay(Context context, Date input) {
        String result = N_C;
        if (input != null) {
            CharSequence format = context.getText(R.string.dla_format);
            DateFormat outputDF = new SimpleDateFormat(format.toString(), Locale.FRENCH);
            outputDF.setTimeZone(getDisplayTimeZone(context));
            result = outputDF.format(input);
        }
        return result;
    }

    public static String formatDLAForDisplayShort(Context context, Date input) {
        String result = N_C;
        if (input != null) {
            CharSequence format = context.getText(R.string.dla_format_short);
            DateFormat outputDF = new SimpleDateFormat(format.toString(), Locale.FRENCH);
            outputDF.setTimeZone(getDisplayTimeZone(context));
            result = outputDF.format(input);
        }
        return result;
    }

//    public static String formatDate(Date input) {
//        String result = N_C;
//        if (input != null) {
//            DateFormat outputDF = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.FRENCH);
//            result = outputDF.format(input);
//        }
//        return result;
//    }

    public static String formatDay(Date input) {
        String result = N_C;
        if (input != null) {
            DateFormat outputDF = new SimpleDateFormat(DAY_DATE_FORMAT, Locale.FRENCH);
            result = outputDF.format(input);
        }
        return result;
    }

    public static String formatHour(Date input) {
        String result = N_C;
        if (input != null) {
            DateFormat outputDF = new SimpleDateFormat(HOUR_DATE_FORMAT, Locale.FRENCH);
            result = outputDF.format(input);
        }
        return result;
    }

    public static String formatHourNoSecondsForDisplay(Context context, Date input) {
        String result = N_C;
        if (input != null) {
            DateFormat outputDF = new SimpleDateFormat(HOUR_NO_SEC_DATE_FORMAT, Locale.FRENCH);
            outputDF.setTimeZone(getDisplayTimeZone(context));
            result = outputDF.format(input);
        }
        return result;
    }

//    public static final Function<Double, String> PRETTY_PRINT_DURATION_DOUBLE = new Function<Double, String>() {
//        @Override
//        public String apply(Double duration) {
//            int durationInt = ((Double)Math.floor(duration)).intValue();
//            int durationHours = durationInt / 60;
//            int durationMinutes = durationInt % 60;
//            Double durationSeconds = 60d * (duration - Math.floor(duration));
//            String result;
//            String hours = String.format("%d heures", durationHours);
//            String minutes = String.format("%d minute", durationMinutes);
//            if (durationMinutes > 1) {
//                minutes += "s";
//            }
//            if (durationSeconds > 0) {
//                String seconds = String.format("%.0f seconde", durationSeconds);
//                if (durationSeconds > 1) {
//                    seconds += "s";
//                }
//                result = String.format("%s, %s et %s", hours, minutes, seconds);
//            } else {
//                result = String.format("%s et %s", hours, minutes);
//            }
//
//            return result;
//        }
//    };

//    public static final Function<Integer, String> PRETTY_PRINT_DURATION = new Function<Integer, String>() {
//        @Override
//        public String apply(Integer duration) {
//            int durationHours = duration / 60;
//            int durationMinutes = duration % 60;
//            String hours = String.format("%d heures", durationHours);
//            String minutes = String.format("%d minute", durationMinutes);
//            if (durationMinutes > 1) {
//                minutes += "s";
//            }
//            String result = String.format("%s et %s", hours, minutes);
//
//            return result;
//        }
//    };

    public static String prettyPrintDuration(Context context, Integer duration) {
        int durationHours = duration / 60;
        int durationMinutes = duration % 60;
        CharSequence format = context.getText(R.string.dla_duration_format);
        String result = String.format(format.toString(), durationHours, durationMinutes);
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

    public static Bitmap loadBlason(String blasonUrl, File filesDir) {
        Bitmap result = loadBlason0(blasonUrl, filesDir, BLASON_MAX_SIZE);
        return result;
    }

    public static Bitmap loadBlasonForWidget(String blasonUrl, File filesDir) {
        Bitmap result = loadBlason0(blasonUrl, filesDir, BLASON_WIDGET_MAX_SIZE);
        return result;
    }

    protected static Bitmap loadBlason0(String blasonUrl, File filesDir, int blasonMaxSize) {
        Bitmap result = null;
        if (!Strings.isNullOrEmpty(blasonUrl)) {
            String fileName = BLASON_FILES_PREFIX + MhDlaNotifierUtils.md5(blasonUrl) + "_" + blasonMaxSize;
            File localFile = new File(filesDir, fileName);
            Log.i(TAG, "localFile: " + localFile);
            if (!localFile.exists()) {

                result = loadAndCropBlason(blasonUrl, filesDir);

                if (result != null && (result.getWidth() > blasonMaxSize || result.getHeight() > blasonMaxSize)) {

                    double blasonMaxSizeDouble = blasonMaxSize;

                    double factor = Math.min(blasonMaxSizeDouble / result.getWidth(), blasonMaxSizeDouble / result.getHeight());
                    int newWidth = (int) (result.getWidth() * factor);
                    int newHeight = (int) (result.getHeight() * factor);
                    Log.i(TAG, String.format("Will resize from %dx%d to %dx%d",result.getWidth(), result.getHeight(), newWidth, newHeight));

                    result = Bitmap.createScaledBitmap(result, newWidth, newHeight, true);

                    Log.i(TAG, "Save resized result to " + localFile);
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(localFile);

                        result.compress(Bitmap.CompressFormat.PNG, 90, fos);

                    } catch (Exception eee) {
                        Log.e(TAG, "Exception", eee);
                    } finally {
                        try {
                            Closeables.close(fos, false);
                        } catch (IOException e) {
                            Log.e(TAG, "Un exception occurred", e);
                        }
                    }
                }

            } else {

                Log.i(TAG, "Existing, loading from cache");
                BufferedInputStream bis = null;
                try {
                    bis = new BufferedInputStream(new FileInputStream(localFile));

                    result = BitmapFactory.decodeStream(bis);

                    bis.close();
                } catch (Exception eee) {
                    Log.e(TAG, "Exception", eee);
                } finally {
                    try {
                        Closeables.close(bis, false);
                    } catch (IOException e) {
                        Log.e(TAG, "Un exception occurred", e);
                    }
                }
            }
        }
        return result;
    }

    protected static Bitmap loadAndCropBlason(String blasonUrl, File filesDir) {

        Bitmap result = null;
        if (!Strings.isNullOrEmpty(blasonUrl)) {
            String fileName = BLASON_FILES_PREFIX + MhDlaNotifierUtils.md5(blasonUrl) + "_cropped";
            File localFile = new File(filesDir, fileName);
            Log.i(TAG, "localFile: " + localFile);
            if (!localFile.exists()) {

                Bitmap rawBlason = loadRawBlason(blasonUrl, filesDir);

                if (rawBlason != null) {
                    int minX = Integer.MAX_VALUE;
                    int minY = Integer.MAX_VALUE;
                    int maxX = Integer.MIN_VALUE;
                    int maxY = Integer.MIN_VALUE;
                    for (int x=0; x<rawBlason.getWidth(); x++) {
                        for (int y=0; y<rawBlason.getHeight(); y++) {
                            int pixel = rawBlason.getPixel(x, y);
                            int alpha = (pixel >> 24) & 0xff;
                            boolean transparent = alpha == 0;
                            if (!transparent) {
                                minX = Math.min(minX, x);
                                minY = Math.min(minY, y);
                                maxX = Math.max(maxX, x);
                                maxY = Math.max(maxY, y);
                            }
                        }
                    }

                    int cropMinX = Math.max(0, minX - CROP_PADDING);
                    int cropMinY = Math.max(0, minY - CROP_PADDING);
                    int cropMaxX = Math.min(rawBlason.getWidth() - 1, maxX + CROP_PADDING);
                    int cropMaxY = Math.min(rawBlason.getHeight() - 1, maxY + CROP_PADDING);

                    Log.i(TAG,
                            "Size info: " + rawBlason.getWidth() + "x" + rawBlason.getHeight() + " ; " +
                            "Alpha info: min=" + minX + "x" + minY + " ; max=" + maxX + "x" + maxY + " ; " +
                            "Crop info: min=" + cropMinX + "x" + cropMinY + " ; max=" + cropMaxX + "x" + cropMaxY);

                    if (cropMinX > 0 || cropMinY > 0 || cropMaxX < rawBlason.getWidth() || cropMaxY < rawBlason.getHeight()) {

                        result = Bitmap.createBitmap(rawBlason, cropMinX, cropMinY, cropMaxX - cropMinX, cropMaxY - cropMinY);

                        Log.i(TAG, "Save cropped result to " + localFile);
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(localFile);

                            result.compress(Bitmap.CompressFormat.PNG, 90, fos);

                        } catch (Exception eee) {
                            Log.e(TAG, "Exception", eee);
                        } finally {
                            try {
                                Closeables.close(fos, false);
                            } catch (IOException e) {
                                Log.e(TAG, "Un exception occurred", e);
                            }
                        }
                    }
                }
            } else {

                Log.i(TAG, "Existing, loading from cache");
                BufferedInputStream bis = null;
                try {
                    bis = new BufferedInputStream(new FileInputStream(localFile));

                    result = BitmapFactory.decodeStream(bis);

                    bis.close();
                } catch (Exception eee) {
                    Log.e(TAG, "Exception", eee);
                } finally {
                    try {
                        Closeables.close(bis, false);
                    } catch (IOException e) {
                        Log.e(TAG, "Un exception occurred", e);
                    }
                }
            }
        }
        return result;
    }

    protected static Bitmap loadRawBlason(String blasonUrl, File filesDir) {
        Bitmap result = null;
        if (!Strings.isNullOrEmpty(blasonUrl)) {
            String fileName = BLASON_FILES_PREFIX + MhDlaNotifierUtils.md5(blasonUrl);
            File localFile = new File(filesDir, fileName);
            Log.i(TAG, "localFile: " + localFile);
            if (!localFile.exists()) {

                Log.i(TAG, "Not existing, fetching from " + blasonUrl);
                BufferedInputStream bis = null;
                try {
                    URL url = new URL(blasonUrl);
                    URLConnection conn = url.openConnection();
                    conn.connect();
                    bis = new BufferedInputStream(conn.getInputStream());
                    result = BitmapFactory.decodeStream(bis);

                } catch (Exception eee) {
                    Log.e(TAG, "Exception", eee);
                } finally {
                    try {
                        Closeables.close(bis, false);
                    } catch (IOException e) {
                        Log.e(TAG, "Un exception occurred", e);
                    }
                }

                if (result != null) {
                    Log.i(TAG, "Save fetched result to " + localFile);
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(localFile);

                        result.compress(Bitmap.CompressFormat.PNG, 90, fos);

                    } catch (Exception eee) {
                        Log.e(TAG, "Exception", eee);
                        return null;
                    } finally {
                        try {
                            Closeables.close(fos, false);
                        } catch (IOException e) {
                            Log.e(TAG, "Un exception occurred", e);
                        }
                    }
                }
            } else {

                Log.i(TAG, "Existing, loading from cache");
                BufferedInputStream bis = null;
                try {
                    bis = new BufferedInputStream(new FileInputStream(localFile));

                    result = BitmapFactory.decodeStream(bis);

                    bis.close();
                } catch (Exception eee) {
                    Log.e(TAG, "Exception", eee);
                } finally {
                    try {
                        Closeables.close(bis, false);
                    } catch (IOException e) {
                        Log.e(TAG, "Un exception occurred", e);
                    }
                }
            }
        }
        return result;
    }

    public static String loadGuilde(int guildeNumber, File filesDir) {
        String result = "";
        if (guildeNumber > 0) {
            File localFile = new File(filesDir, "guildes.txt");

            if (!localFile.exists()) {
                Log.i(TAG, "Not existing, fetching from " + "http://www.mountyhall.com/ftp/Public_Guildes.txt");
                BufferedInputStream bis = null;
                try {
                    URL url = new URL("http://www.mountyhall.com/ftp/Public_Guildes.txt");
                    URLConnection conn = url.openConnection();
                    conn.connect();
                    final BufferedInputStream fbis = new BufferedInputStream(conn.getInputStream());
                    bis = fbis;

                    Files.copy(new InputSupplier<InputStream>() {
                        @Override
                        public InputStream getInput() throws IOException {
                            return fbis;
                        }
                    }, localFile);
                } catch (Exception eee) {
                    Log.e(TAG, "Exception", eee);
                } finally {
                    try {
                        Closeables.close(bis, false);
                    } catch (IOException e) {
                        Log.e(TAG, "Un exception occurred", e);
                    }
                }
            }

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(localFile));
                String str;
                String beginsWith = guildeNumber + ";";
                while ((str = reader.readLine()) != null) {
                    if (str.startsWith(beginsWith)) {
                        List<String> split = Lists.newArrayList(Splitter.on(";").omitEmptyStrings().trimResults().split(str));
                        result = split.get(1);
                        break;
                    }
                }
            } catch (FileNotFoundException e) {
                // Forget...
            } catch (IOException e) {
                // Forget
            } finally {
                try {
                    Closeables.close(reader, false);
                } catch (IOException e) {
                    Log.e(TAG, "Un exception occurred", e);
                }
            }

        }
        return result;
    }

    public static final Function<Context, Intent> GET_PLAY_INTENT = new Function<Context, Intent>() {
        @Override
        public Intent apply(Context input) {
            Uri uri = MhDlaNotifierConstants.MH_PLAY_URI;
            PreferencesHolder preferences = PreferencesHolder.load(input);
            if (preferences.useSmartphoneInterface) {
                uri = MhDlaNotifierConstants.MH_PLAY_SMARTPHONE_URI;
            }
            Intent webIntent = new Intent(Intent.ACTION_VIEW, uri);
            return webIntent;
        }
    };

}
