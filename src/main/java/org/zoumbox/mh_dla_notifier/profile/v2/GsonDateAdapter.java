package org.zoumbox.mh_dla_notifier.profile.v2;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.zoumbox.mh_dla_notifier.MhDlaNotifierConstants;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Arnaud Thimel
 */
public class GsonDateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + GsonDateAdapter.class.getSimpleName();

    /*
     * Exemples de dates legacy :
     * Jan 21, 2011 14:07:48
     * Aug 6, 2014 00:03:49
     * May 13, 2004 16:58:51
     */
    public static final String LEGACY_DATE_FORMAT = "MMM dd, yyyy HH:mm:ss";
    public static final String SIMPLE_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";

    protected DateFormat newDateFormat(String dateFormat) {
        DateFormat result = new SimpleDateFormat(dateFormat);
        result.setTimeZone(TimeZone.getTimeZone("UTC"));
        return result;
    }

    protected DateFormat newSimpleDateFormat() {
        return newDateFormat(SIMPLE_DATE_FORMAT);
    }

    protected DateFormat newLegacyDateFormat() {
        return newDateFormat(LEGACY_DATE_FORMAT);
    }

    // These methods need to be synchronized since JDK DateFormat classes are not thread-safe
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {

        Log.d(TAG, "Will format date: " + src);
        String formatted = newSimpleDateFormat().format(src);
        Log.d(TAG, "Formatted date: " + formatted);
        JsonPrimitive result = new JsonPrimitive(formatted);
        return result;
    }

    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (!(json instanceof JsonPrimitive)) {
            throw new JsonParseException("The date should be a string value");
        }
        String stringDate = json.getAsString();
        try {
            Log.d(TAG, "Will parse date: " + stringDate);
            Date result = newSimpleDateFormat().parse(stringDate);
            Log.d(TAG, "Parsed date: " + result);
            return result;
        } catch (ParseException pe) {
            Log.w(TAG, "Unable to parse date ("+stringDate+") with standard format: " + pe.getMessage(), pe);
            try {
                return newLegacyDateFormat().parse(stringDate);
            } catch (ParseException pe2) {
                Log.w(TAG, "Unable to parse date ("+stringDate+") with legacy format: " + pe2.getMessage(), pe2);
            }
        }
        Log.w(TAG, "Unable to parse date: "+stringDate);
        return null;
    }

}
