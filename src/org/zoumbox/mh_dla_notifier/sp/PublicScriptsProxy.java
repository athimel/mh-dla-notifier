/*
 * #%L
 * MountyHall DLA Notifier
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2012 - 2013 Zoumbox.org
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

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.zoumbox.mh_dla_notifier.MhDlaNotifierConstants;
import org.zoumbox.mh_dla_notifier.Pair;
import org.zoumbox.mh_dla_notifier.profile.v1.ProfileProxyV1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class PublicScriptsProxy {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + PublicScriptsProxy.class.getSimpleName();

    public static PublicScriptResponse doHttpGET(String url) throws NetworkUnavailableException, PublicScriptException {

        long start = System.currentTimeMillis();

        if (url.contains("?Numero=" + MhDlaNotifierConstants.MOCK_TROLL_ID)) {
            return PublicScriptsProxyMock.doMockHttpGET(url);
        }

        String responseContent = "";
        BufferedReader in = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            InputStream content = response.getEntity().getContent();
            in = new BufferedReader(new InputStreamReader(content));
            String line;
            while ((line = in.readLine()) != null) {
                if (!Strings.isNullOrEmpty(responseContent)) {
                    responseContent += "\n";
                }
                responseContent += line;
            }
            in.close();
        } catch (UnknownHostException uhe) {
            Log.e(TAG, "Network error", uhe);
            throw new NetworkUnavailableException(uhe);
        } catch (Exception eee) {
            Log.e(TAG, "Exception", eee);
            throw new PublicScriptException(eee);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    Log.e(TAG, "IOException", ioe);
                }
            }
        }

        long end = System.currentTimeMillis();
        PublicScriptResponse result = new PublicScriptResponse(responseContent, end - start);
        return result;
    }

    protected static final String SQL_COUNT = String.format("SELECT COUNT(*) FROM %s WHERE %s=? AND %s=? AND %s>=?",
            MhDlaSQLHelper.SCRIPTS_TABLE, MhDlaSQLHelper.SCRIPTS_TROLL_COLUMN, MhDlaSQLHelper.SCRIPTS_CATEGORY_COLUMN, MhDlaSQLHelper.SCRIPTS_DATE_COLUMN);

    protected static final String SQL_LAST_UPDATE = String.format("SELECT MAX(%s) FROM %s WHERE %s=? AND %s=?",
            MhDlaSQLHelper.SCRIPTS_DATE_COLUMN, MhDlaSQLHelper.SCRIPTS_TABLE, MhDlaSQLHelper.SCRIPTS_TROLL_COLUMN, MhDlaSQLHelper.SCRIPTS_SCRIPT_COLUMN);

    protected static final String SQL_LIST_REQUESTS = String.format("SELECT %s, %s FROM %s WHERE %s=? ORDER BY %s DESC LIMIT %s",
            MhDlaSQLHelper.SCRIPTS_DATE_COLUMN,  MhDlaSQLHelper.SCRIPTS_SCRIPT_COLUMN, MhDlaSQLHelper.SCRIPTS_TABLE, MhDlaSQLHelper.SCRIPTS_TROLL_COLUMN, MhDlaSQLHelper.SCRIPTS_DATE_COLUMN, "%d");

    protected static final String SQL_LIST_REQUESTS_SINCE = String.format("SELECT %s, %s FROM %s WHERE %s=? AND %s>=? ORDER BY %s DESC ",
            MhDlaSQLHelper.SCRIPTS_DATE_COLUMN,  MhDlaSQLHelper.SCRIPTS_SCRIPT_COLUMN, MhDlaSQLHelper.SCRIPTS_TABLE, MhDlaSQLHelper.SCRIPTS_TROLL_COLUMN, MhDlaSQLHelper.SCRIPTS_DATE_COLUMN, MhDlaSQLHelper.SCRIPTS_DATE_COLUMN);

    protected static int computeRequestCount(Context context, ScriptCategory category, String trollId) {

        MhDlaSQLHelper helper = new MhDlaSQLHelper(context);
        SQLiteDatabase database = helper.getReadableDatabase();

        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.HOUR_OF_DAY, -24);
        Date sinceDate = instance.getTime();

        Cursor cursor = database.rawQuery(SQL_COUNT, new String[]{trollId, category.name(), "" + sinceDate.getTime()});
        int result = 0;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = cursor.getInt(0);
        }

        cursor.close();
        database.close();

        String format = "Quota for category %s and troll=%s since '%s' is: %d";
        String message = String.format(format, category, trollId, sinceDate, result);
        Log.d(TAG, message);

        return result;
    }

    protected static void saveFetch(Context context, PublicScript script, String trollId) {

        String format = "Saving fetch for category %s (script=%s) and troll=%s";
        String message = String.format(format, script.category, script, trollId);
        Log.d(TAG, message);

        MhDlaSQLHelper helper = new MhDlaSQLHelper(context);
        SQLiteDatabase database = helper.getWritableDatabase();

        ContentValues values = new ContentValues(4);
        values.put(MhDlaSQLHelper.SCRIPTS_DATE_COLUMN, System.currentTimeMillis());
        values.put(MhDlaSQLHelper.SCRIPTS_SCRIPT_COLUMN, script.name());
        values.put(MhDlaSQLHelper.SCRIPTS_CATEGORY_COLUMN, script.category.name());
        values.put(MhDlaSQLHelper.SCRIPTS_TROLL_COLUMN, trollId);

        database.insert(MhDlaSQLHelper.SCRIPTS_TABLE, null, values);

        database.close();
    }

    public static Date geLastUpdate(Context context, PublicScript script, String trollId) {

        MhDlaSQLHelper helper = new MhDlaSQLHelper(context);
        SQLiteDatabase database = helper.getReadableDatabase();

        Cursor cursor = database.rawQuery(SQL_LAST_UPDATE, new String[]{trollId, script.name()});
        Date result = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            long resultTimestamp = cursor.getLong(0);
            result = new Date(resultTimestamp);
        }

        cursor.close();
        database.close();

        String format = "Last update for category %s (script=%s) and troll=%s is: '%s'";
        String message = String.format(format, script.category, script, trollId, result);
        Log.d(TAG, message);

        return result;
    }

    public static Map<PublicScript, Date> geLastUpdates(Context context, String trollId, Set<PublicScript> scripts) {

        MhDlaSQLHelper helper = new MhDlaSQLHelper(context);
        SQLiteDatabase database = helper.getReadableDatabase();

        Map<PublicScript, Date> result = Maps.newHashMap();

        for (PublicScript script : scripts) {

            Cursor cursor = database.rawQuery(SQL_LAST_UPDATE, new String[]{trollId, script.name()});
            Date lastUpdate = null;
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                long resultTimestamp = cursor.getLong(0);
                lastUpdate = new Date(resultTimestamp);
            }

            result.put(script, lastUpdate);
            cursor.close();
        }
        database.close();

        String format = "Last updates for troll=%s are: '%s'";
        String message = String.format(format, trollId, result);
        Log.i(TAG, message);

        return result;
    }


    public static PublicScriptResult fetchScript(Context context, PublicScript script,
                                                 String trollId, String trollPassword)
            throws QuotaExceededException, PublicScriptException, NetworkUnavailableException {

        Log.i(TAG, String.format("Fetching in script=%s and troll=%s ", script, trollId));
        ScriptCategory category = script.category;
        int requestCount = computeRequestCount(context, script.category, trollId);
//        if (requestCount >= category.quota) {
        if (requestCount >= (category.quota / 2)) {
            String format = "Quota is exceeded for category %s (script=%s) and troll=%s: %d§%d";
            String message = String.format(format, category, script, trollId, requestCount, category.quota);
            Log.w(TAG, message);
            throw new QuotaExceededException(category, requestCount);
        }

        String url = String.format(script.url, Uri.encode(trollId), Uri.encode(trollPassword));
        PublicScriptResponse spResult = doHttpGET(url);
        Log.i(TAG, String.format("Script response: '%s'", spResult));

        if (spResult.hasError()) {
            throw new PublicScriptException(spResult);
        } else {

            saveFetch(context, script, trollId);

            String raw = spResult.getRaw();
            PublicScriptResult result = new PublicScriptResult(script, raw);

            return result;
        }

    }

    @Deprecated
    public static Map<String, String> fetchProperties(Context context, PublicScript script, Pair<String, String> idAndPassword)
            throws PublicScriptException, QuotaExceededException, NetworkUnavailableException {
        long now = System.currentTimeMillis();
        Map<String, String> result;
        try {
            PublicScriptResult publicScriptResult = fetchScript(context, script, idAndPassword.left(), idAndPassword.right());
            result = PublicScripts.SCRIPT_RESULT_TO_MAP.apply(publicScriptResult);
            saveUpdateResult(context, script, now, null);
        } catch (PublicScriptException pse) {
            saveUpdateResult(context, script, now, pse);
            throw new PublicScriptException(pse);
        } catch (QuotaExceededException qee) {
            saveUpdateResult(context, script, now, qee);
            throw new QuotaExceededException(qee);
        } catch (NetworkUnavailableException nue) {
            saveUpdateResult(context, script, now, nue);
            throw new NetworkUnavailableException(nue);
        }
        return result;
    }

    @Deprecated
    protected static void saveUpdateResult(Context context, PublicScript script, long date, Exception exception) {
        if (PublicScript.Profil2.equals(script)) {

            SharedPreferences preferences = context.getSharedPreferences(ProfileProxyV1.PREFS_NAME, 0);

            boolean success = false;
            String result;
            if (exception == null) {
                result = "SUCCESS";
                success = true;
            } else {
                result = exception.getClass().getName();
                if (exception instanceof NetworkUnavailableException || (exception instanceof PublicScriptException && exception.getCause() != null)) {
                    result = "NETWORK ERROR: " + result;
                }
            }

            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong("LAST_UPDATE_ATTEMPT", date);
            editor.putString("LAST_UPDATE_RESULT", result);
            if (success) {
                editor.putLong("LAST_UPDATE_SUCCESS", date);
            }

            editor.commit();
        }
    }

    public static List<MhSpRequest> listLatestRequests(Context context, String trollId, int count) {
        List<MhSpRequest> result = Lists.newArrayList();

        String query = String.format(SQL_LIST_REQUESTS, count);


        MhDlaSQLHelper helper = new MhDlaSQLHelper(context);
        SQLiteDatabase database = helper.getReadableDatabase();

        Calendar calendar = Calendar.getInstance();

        Cursor cursor = database.rawQuery(query, new String[]{trollId});
        while (cursor.moveToNext()) {
            Long timeMillis = cursor.getLong(0);
            String scriptName = cursor.getString(1);

            calendar.setTimeInMillis(timeMillis);
            Date date = calendar.getTime();
            PublicScript script = PublicScript.valueOf(scriptName);

            MhSpRequest request = new MhSpRequest(date, script);
            result.add(request);
        }

        cursor.close();
        database.close();

        return result;
    }

    public static List<MhSpRequest> listLatestRequestsSince(Context context, String trollId, int dayCount) {
        List<MhSpRequest> result = Lists.newArrayList();

        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.HOUR_OF_DAY, dayCount * -24);
        Date sinceDate = instance.getTime();

        MhDlaSQLHelper helper = new MhDlaSQLHelper(context);
        SQLiteDatabase database = helper.getReadableDatabase();

        Calendar calendar = Calendar.getInstance();

        Cursor cursor = database.rawQuery(SQL_LIST_REQUESTS_SINCE, new String[]{trollId, "" + sinceDate.getTime()});
        while (cursor.moveToNext()) {
            Long timeMillis = cursor.getLong(0);
            String scriptName = cursor.getString(1);

            calendar.setTimeInMillis(timeMillis);
            Date date = calendar.getTime();
            PublicScript script = PublicScript.valueOf(scriptName);

            MhSpRequest request = new MhSpRequest(date, script);
            result.add(request);
        }

        cursor.close();
        database.close();

        return result;
    }

    public static Map<ScriptCategory, Integer> listQuotas(Context context, String trollId) {
        Map<ScriptCategory, Integer> result = Maps.newLinkedHashMap();
        for (ScriptCategory scriptCategory : ScriptCategory.values()) {
            int count = computeRequestCount(context, scriptCategory, trollId);
            result.put(scriptCategory, count);
        }
        return result;
    }

}
