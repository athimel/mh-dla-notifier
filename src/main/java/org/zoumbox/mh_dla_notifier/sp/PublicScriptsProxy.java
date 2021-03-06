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

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import org.zoumbox.mh_dla_notifier.MhDlaNotifierConstants;
import org.zoumbox.mh_dla_notifier.Pair;
import org.zoumbox.mh_dla_notifier.profile.v1.ProfileProxyV1;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class PublicScriptsProxy {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + PublicScriptsProxy.class.getSimpleName();

    protected static final Long THIRTY_MINUTES = 30L * 1000;

    protected static final String SQL_COUNT = String.format("SELECT COUNT(*) FROM %s WHERE %s=? AND %s=? AND %s>=?",
            MhDlaSQLHelper.SCRIPTS_TABLE, MhDlaSQLHelper.SCRIPTS_TROLL_COLUMN, MhDlaSQLHelper.SCRIPTS_CATEGORY_COLUMN, MhDlaSQLHelper.SCRIPTS_END_DATE_COLUMN);

    protected static final String SQL_LAST_REQUEST = String.format("SELECT MAX(%s) FROM %s WHERE %s=? AND %s=?",
            MhDlaSQLHelper.SCRIPTS_END_DATE_COLUMN, MhDlaSQLHelper.SCRIPTS_TABLE, MhDlaSQLHelper.SCRIPTS_TROLL_COLUMN, MhDlaSQLHelper.SCRIPTS_SCRIPT_COLUMN);

    protected static final String SQL_LAST_UPDATE = String.format("SELECT MAX(%s) FROM %s WHERE %s=? AND %s=? AND %s=?",
            MhDlaSQLHelper.SCRIPTS_END_DATE_COLUMN, MhDlaSQLHelper.SCRIPTS_TABLE, MhDlaSQLHelper.SCRIPTS_TROLL_COLUMN, MhDlaSQLHelper.SCRIPTS_SCRIPT_COLUMN, MhDlaSQLHelper.SCRIPTS_STATUS_COLUMN);

    protected static final String SQL_LIST_REQUESTS = String.format("SELECT %s, %s, %s, %s FROM %s WHERE %s=? ORDER BY %s DESC LIMIT %s",
            MhDlaSQLHelper.SCRIPTS_START_DATE_COLUMN,  MhDlaSQLHelper.SCRIPTS_END_DATE_COLUMN,  MhDlaSQLHelper.SCRIPTS_SCRIPT_COLUMN, MhDlaSQLHelper.SCRIPTS_STATUS_COLUMN, MhDlaSQLHelper.SCRIPTS_TABLE, MhDlaSQLHelper.SCRIPTS_TROLL_COLUMN, MhDlaSQLHelper.SCRIPTS_END_DATE_COLUMN, "%d");

    protected static final String SQL_LIST_REQUESTS_SINCE = String.format("SELECT %s, %s, %s, %s FROM %s WHERE %s=? AND %s>=? ORDER BY %s DESC ",
            MhDlaSQLHelper.SCRIPTS_START_DATE_COLUMN,  MhDlaSQLHelper.SCRIPTS_END_DATE_COLUMN,  MhDlaSQLHelper.SCRIPTS_SCRIPT_COLUMN, MhDlaSQLHelper.SCRIPTS_STATUS_COLUMN, MhDlaSQLHelper.SCRIPTS_TABLE, MhDlaSQLHelper.SCRIPTS_TROLL_COLUMN, MhDlaSQLHelper.SCRIPTS_END_DATE_COLUMN, MhDlaSQLHelper.SCRIPTS_END_DATE_COLUMN);

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

    protected static void saveFetch(Context context, PublicScript script, String trollId, String uuid, String status) {

        String format = "Saving fetch for category %s (script=%s) and troll=%s";
        String message = String.format(format, script.category, script, trollId);
        Log.d(TAG, message);

        MhDlaSQLHelper helper = new MhDlaSQLHelper(context);
        SQLiteDatabase database = helper.getWritableDatabase();

        ContentValues values = new ContentValues(2);
        long now = System.currentTimeMillis();
        values.put(MhDlaSQLHelper.SCRIPTS_END_DATE_COLUMN, now);
        values.put(MhDlaSQLHelper.SCRIPTS_STATUS_COLUMN, status);

        String whereClause = String.format("%s = ?", MhDlaSQLHelper.SCRIPTS_ID_COLUMN);
        database.update(MhDlaSQLHelper.SCRIPTS_TABLE, values, whereClause, new String[]{uuid});

        database.close();
    }

    public static Date geLastRequest(Context context, PublicScript script, String trollId) {

        MhDlaSQLHelper helper = new MhDlaSQLHelper(context);
        SQLiteDatabase database = helper.getReadableDatabase();

        Cursor cursor = database.rawQuery(SQL_LAST_REQUEST, new String[]{trollId, script.name()});
        Date result = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            long resultTimestamp = cursor.getLong(0);
            result = new Date(resultTimestamp);
        }

        cursor.close();
        database.close();

        String format = "Last request for category %s (script=%s) and troll=%s is: '%s'";
        String message = String.format(format, script.category, script, trollId, result);
        Log.d(TAG, message);

        return result;
    }

    public static Date geLastUpdate(Context context, PublicScript script, String trollId) {

        MhDlaSQLHelper helper = new MhDlaSQLHelper(context);
        SQLiteDatabase database = helper.getReadableDatabase();

        Cursor cursor = database.rawQuery(SQL_LAST_UPDATE, new String[]{trollId, script.name(), MhDlaSQLHelper.STATUS_SUCCESS});
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

        Map<PublicScript, Date> result = new HashMap<PublicScript, Date>();

        for (PublicScript script : scripts) {

            Cursor cursor = database.rawQuery(SQL_LAST_UPDATE, new String[]{trollId, script.name(), MhDlaSQLHelper.STATUS_SUCCESS});
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
            throws QuotaExceededException, PublicScriptException, NetworkUnavailableException, HighUpdateRateException {

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

        Date lastRequest = geLastRequest(context, script, trollId);
        if (System.currentTimeMillis() - lastRequest.getTime() < THIRTY_MINUTES) {
            throw new HighUpdateRateException(script, lastRequest);
        }

        String uuid = UUID.randomUUID().toString();
        createFetchLog(context, script, trollId, uuid);

        String url = String.format(script.url, Uri.encode(trollId), Uri.encode(trollPassword));
        PublicScriptResponse spResult = AsyncHttpFetcher.doHttpGET(url);
        Log.i(TAG, String.format("Script response: '%s'", spResult));

        if (spResult.hasError()) {
            saveFetch(context, script, trollId, uuid, MhDlaSQLHelper.STATUS_ERROR);
            throw new PublicScriptException(spResult);
        } else {

            saveFetch(context, script, trollId, uuid, MhDlaSQLHelper.STATUS_SUCCESS);

            String raw = spResult.getRaw();
            PublicScriptResult result = new PublicScriptResult(script, raw);

            return result;
        }

    }

    protected static void createFetchLog(Context context, PublicScript script, String trollId, String uuid) {

        String format = "Create fetch log for script=%s and troll=%s";
        String message = String.format(format, script, trollId);
        Log.d(TAG, message);

        MhDlaSQLHelper helper = new MhDlaSQLHelper(context);
        SQLiteDatabase database = helper.getWritableDatabase();

        ContentValues values = new ContentValues(6);
        long now = System.currentTimeMillis();
        values.put(MhDlaSQLHelper.SCRIPTS_ID_COLUMN, uuid);
        values.put(MhDlaSQLHelper.SCRIPTS_START_DATE_COLUMN, now);
        values.put(MhDlaSQLHelper.SCRIPTS_SCRIPT_COLUMN, script.name());
        values.put(MhDlaSQLHelper.SCRIPTS_CATEGORY_COLUMN, script.category.name());
        values.put(MhDlaSQLHelper.SCRIPTS_TROLL_COLUMN, trollId);
        values.put(MhDlaSQLHelper.SCRIPTS_STATUS_COLUMN, "PENDING");

        database.insert(MhDlaSQLHelper.SCRIPTS_TABLE, null, values);

        database.close();
    }

    @Deprecated
    public static Map<String, String> fetchProperties(Context context, PublicScript script, Pair<String, String> idAndPassword)
            throws PublicScriptException, QuotaExceededException, NetworkUnavailableException, HighUpdateRateException {
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
        } catch (HighUpdateRateException hure) {
            saveUpdateResult(context, script, now, hure);
            throw new HighUpdateRateException(hure);
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
        List<MhSpRequest> result = new ArrayList<MhSpRequest>();

        String query = String.format(SQL_LIST_REQUESTS, count);


        MhDlaSQLHelper helper = new MhDlaSQLHelper(context);
        SQLiteDatabase database = helper.getReadableDatabase();

        Calendar calendar = Calendar.getInstance();

        Cursor cursor = database.rawQuery(query, new String[]{trollId});
        while (cursor.moveToNext()) {
            long startTimeMillis = cursor.getLong(0);
            long endTimeMillis = cursor.getLong(1);
            String scriptName = cursor.getString(2);
            String status = cursor.getString(3);

            calendar.setTimeInMillis(startTimeMillis);
            Date date = calendar.getTime();
            PublicScript script = PublicScript.valueOf(scriptName);

            long duration = 0;
            if (endTimeMillis > 0) {
                duration = endTimeMillis - startTimeMillis;
            }
            MhSpRequest request = new MhSpRequest(date, duration, script, status);
            result.add(request);
        }

        cursor.close();
        database.close();

        return result;
    }

    public static List<MhSpRequest> listLatestRequestsSince(Context context, String trollId, int dayCount) {
        List<MhSpRequest> result = new ArrayList<MhSpRequest>();

        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.HOUR_OF_DAY, dayCount * -24);
        Date sinceDate = instance.getTime();

        MhDlaSQLHelper helper = new MhDlaSQLHelper(context);
        SQLiteDatabase database = helper.getReadableDatabase();

        Calendar calendar = Calendar.getInstance();

        Cursor cursor = database.rawQuery(SQL_LIST_REQUESTS_SINCE, new String[]{trollId, "" + sinceDate.getTime()});
        while (cursor.moveToNext()) {
            long startTimeMillis = cursor.getLong(0);
            long endTimeMillis = cursor.getLong(1);
            String scriptName = cursor.getString(2);
            String status = cursor.getString(3);

            calendar.setTimeInMillis(startTimeMillis);
            Date date = calendar.getTime();
            PublicScript script = PublicScript.valueOf(scriptName);

            long duration = 0;
            if (endTimeMillis > 0) {
                duration = endTimeMillis - startTimeMillis;
            }
            MhSpRequest request = new MhSpRequest(date, duration, script, status);
            result.add(request);
        }

        cursor.close();
        database.close();

        return result;
    }

    public static Map<ScriptCategory, Integer> listQuotas(Context context, String trollId) {
        Map<ScriptCategory, Integer> result = new LinkedHashMap<ScriptCategory, Integer>();
        for (ScriptCategory scriptCategory : ScriptCategory.values()) {
            int count = computeRequestCount(context, scriptCategory, trollId);
            result.put(scriptCategory, count);
        }
        return result;
    }

}
