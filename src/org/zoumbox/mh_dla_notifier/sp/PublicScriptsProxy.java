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
package org.zoumbox.mh_dla_notifier.sp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.zoumbox.mh_dla_notifier.Constants;
import org.zoumbox.mh_dla_notifier.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class PublicScriptsProxy {

    private static final String TAG = Constants.LOG_PREFIX + PublicScriptsProxy.class.getSimpleName();

    public static PublicScriptResponse doHttpGET(String url) throws NetworkUnavailableException {

        if (Constants.mock) {
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
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    Log.e(TAG, "IOException", ioe);
                }
            }
        }

        PublicScriptResponse result = new PublicScriptResponse(responseContent);
        return result;
    }

    protected static final String SQL_COUNT = String.format("SELECT COUNT(*) FROM %s WHERE %s=? AND %s=? AND %s>=?",
            MhDlaSQLHelper.SCRIPTS_TABLE, MhDlaSQLHelper.SCRIPTS_TROLL_COLUMN, MhDlaSQLHelper.SCRIPTS_CATEGORY_COLUMN, MhDlaSQLHelper.SCRIPTS_DATE_COLUMN);

    protected static final String SQL_LAST_UPDATE = String.format("SELECT MAX(%s) FROM %s WHERE %s=? AND %s=?",
            MhDlaSQLHelper.SCRIPTS_DATE_COLUMN, MhDlaSQLHelper.SCRIPTS_TABLE, MhDlaSQLHelper.SCRIPTS_TROLL_COLUMN, MhDlaSQLHelper.SCRIPTS_SCRIPT_COLUMN);

    protected static int checkQuota(Context context, PublicScript script, String trollNumber) {

        MhDlaSQLHelper helper = new MhDlaSQLHelper(context);
        SQLiteDatabase database = helper.getReadableDatabase();

        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.HOUR_OF_DAY, -24);
        Date sinceDate = instance.getTime();

        Cursor cursor = database.rawQuery(SQL_COUNT, new String[]{trollNumber, script.category.name(), "" + sinceDate.getTime()});
        int result = 0;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = cursor.getInt(0);
        }

        cursor.close();
        database.close();

        Log.i(TAG, String.format("Quota for category %s (script=%s) and troll=%s since '%s' is: %d", script.category, script, trollNumber, sinceDate, result));

        return result;
    }

    protected static void saveFetch(Context context, PublicScript script, String trollNumber) {

        Log.i(TAG, String.format("Saving fetch for category %s (script=%s) and troll=%s", script.category, script, trollNumber));

        MhDlaSQLHelper helper = new MhDlaSQLHelper(context);
        SQLiteDatabase database = helper.getWritableDatabase();

        ContentValues values = new ContentValues(4);
        values.put(MhDlaSQLHelper.SCRIPTS_DATE_COLUMN, System.currentTimeMillis());
        values.put(MhDlaSQLHelper.SCRIPTS_SCRIPT_COLUMN, script.name());
        values.put(MhDlaSQLHelper.SCRIPTS_CATEGORY_COLUMN, script.category.name());
        values.put(MhDlaSQLHelper.SCRIPTS_TROLL_COLUMN, trollNumber);

        database.insert(MhDlaSQLHelper.SCRIPTS_TABLE, null, values);

        database.close();
    }

    public static Date geLastUpdate(Context context, PublicScript script, String trollNumber) {

        MhDlaSQLHelper helper = new MhDlaSQLHelper(context);
        SQLiteDatabase database = helper.getReadableDatabase();

        Cursor cursor = database.rawQuery(SQL_LAST_UPDATE, new String[]{trollNumber, script.name()});
        Date result = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            long resultTimestamp = cursor.getLong(0);
            result = new Date(resultTimestamp);
        }

        cursor.close();
        database.close();

        Log.i(TAG, String.format("Last update for category %s (script=%s) and troll=%s is: '%s'", script.category, script, trollNumber, result));

        return result;
    }

    public static Map<String, String> fetch(Context context, PublicScript script, String trollNumber, String trollPassword) throws QuotaExceededException, PublicScriptException, NetworkUnavailableException {

        Log.i(TAG, String.format("Fetching in script=%s and troll=%s ", script, trollNumber));
        ScriptCategory category = script.category;
        int count = checkQuota(context, script, trollNumber);
        if (count >= category.quota) {
            Log.i(TAG, String.format("Quota is exceeded for category %s (script=%s) and troll=%s: %d§%d", category, script, trollNumber, count, category.quota));
            throw new QuotaExceededException(category, count);
        }

        String url = String.format(script.url, trollNumber, trollPassword);
        PublicScriptResponse spResult = doHttpGET(url);
        Log.i(TAG, "Public Script response: '" + spResult + "'");

        if (spResult.hasError()) {
            throw new PublicScriptException(spResult);
        } else {

            saveFetch(context, script, trollNumber);

            String raw = spResult.getRaw();
            Map<String, String> result = interpretFetchedContent(script, raw);

            return result;
        }

    }

    protected static Map<String, String> interpretFetchedContent(PublicScript script, String raw) {
        List<String> lines = Lists.newArrayList(Splitter.on("\n").omitEmptyStrings().trimResults().split(raw));
        Map<String, String> result = Maps.newLinkedHashMap();
        switch (script) {
            case Profil2:
//            case Profil3:
            case ProfilPublic2:
                Iterable<String> iterable = Splitter.on(";").split(lines.get(0));
                List<String> data = Lists.newArrayList(iterable);
                for (int i = 0; i < data.size() && i < script.properties.size(); i++) {
                    String key = script.properties.get(i);
                    String value = data.get(i);
                    result.put(key, value);
                }
                break;
            case Caract:
                result.put(PublicScriptProperties.CARACT.name(), raw);
                break;
//            case Mouche:
//                result.put(PublicScriptProperties.MOUCHES.name(), raw);
//                break;
//            case Equipement:
//                result.put(PublicScriptProperties.EQUIPEMENT.name(), raw);
//                break;
            case Vue:
                int monstresStart = lines.indexOf("#DEBUT MONSTRES");
                int monstresEnd = lines.indexOf("#FIN MONSTRES");
                List<String> monstresList = lines.subList(monstresStart + 1, monstresEnd);
                String monstres = Joiner.on("\n").join(monstresList);
                result.put(PublicScriptProperties.MONSTRES.name(), monstres);

                int trollsStart = lines.indexOf("#DEBUT TROLLS");
                int trollsEnd = lines.indexOf("#FIN TROLLS");
                List<String> trollsList = lines.subList(trollsStart + 1, trollsEnd);
                String trolls = Joiner.on("\n").join(trollsList);
                result.put(PublicScriptProperties.TROLLS.name(), trolls);
                break;
            default:
                throw new IllegalStateException("Unexpected script : " + script);
        }
        return result;
    }

    public static Map<String, String> fetch(Context context, PublicScript script, Pair<String, String> idAndPassword) throws PublicScriptException, QuotaExceededException, NetworkUnavailableException {
        String trollNumber = idAndPassword.left();
        String trollPassword = idAndPassword.right();
        return fetch(context, script, trollNumber, trollPassword);
    }
}
