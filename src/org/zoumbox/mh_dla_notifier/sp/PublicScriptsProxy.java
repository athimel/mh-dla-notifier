package org.zoumbox.mh_dla_notifier.sp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.zoumbox.mh_dla_notifier.Constants;

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

    protected static PublicScriptResponse doHttpGET(String url) throws NetworkUnavailableException {

        if (Constants.mock) {
            return doMockHttpGET(url);
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

    protected static PublicScriptResponse doMockHttpGET(String url) {
        String rawResult;
        if (url.contains("SP_Profil2.php")) {
            rawResult = "123456;57;-75;-41;85;80;4;2012-03-24 22:05:00;8;4;13;4;4;6;360;361;0;5;0;0;0;0;0;585;0;1;0";
        } else if (url.contains("SP_Profil3.php")) {
            rawResult = "123456;Mon Trõll;57;-75;-41;4;2012-03-24 22:05:00;3;0;0;0;2;22;88;6042";
        } else {
            rawResult = "123456;Mon Trõll;Kastar;19;2011-01-21 14:07:48;;http://zoumbox.org/mh/DevelZimZoumMH.png;17;122;9;1900;20;0";
        }
        PublicScriptResponse result = new PublicScriptResponse(rawResult);
        return result;
    }

    protected static final String SQL_COUNT = String.format("SELECT COUNT(*) FROM %s WHERE %s=? AND %s=? AND %s>=?",
            MhDlaSQLHelper.SCRIPTS_TABLE, MhDlaSQLHelper.SCRIPTS_TROLL_COLUMN, MhDlaSQLHelper.SCRIPTS_CATEGORY_COLUMN, MhDlaSQLHelper.SCRIPTS_DATE_COLUMN);

    protected static final String SQL_LAST_UPDATE = String.format("SELECT MAX(%s) FROM %s WHERE %s=? AND %s=?",
            MhDlaSQLHelper.SCRIPTS_DATE_COLUMN, MhDlaSQLHelper.SCRIPTS_TABLE, MhDlaSQLHelper.SCRIPTS_TROLL_COLUMN, MhDlaSQLHelper.SCRIPTS_SCRIPT_COLUMN);

    protected static int checkQuota(Context context, PublicScript script, String trollNumber) {

        Log.i(TAG, "Check quota for category " + script + "(" + script.category + ") and troll " + trollNumber);

        MhDlaSQLHelper helper = new MhDlaSQLHelper(context);
        SQLiteDatabase database = helper.getReadableDatabase();

        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.HOUR_OF_DAY, -24);
        Date sinceDate = instance.getTime();

        Log.i(TAG, "Since: " + sinceDate);

        Cursor cursor = database.rawQuery(SQL_COUNT, new String[]{trollNumber, script.category.name(), "" + sinceDate.getTime()});
        int result = 0;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = cursor.getInt(0);
        }

        cursor.close();
        database.close();

        Log.i(TAG, "Quota is : " + result);

        return result;
    }

    protected static void saveFetch(Context context, PublicScript script, String trollNumber) {

        Log.i(TAG, "Save fetch for category " + script + "(" + script.category + ") and troll " + trollNumber);

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

        Log.i(TAG, "Get last update for category " + script + "(" + script.category + ") and troll " + trollNumber);

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

        Log.i(TAG, "Last update is : " + result);

        return result;
    }

    public static Map<String, String> fetch(Context context, PublicScript script, String trollNumber, String trollPassword, boolean force) throws QuotaExceededException, PublicScriptException, NetworkUnavailableException {

        Log.i(TAG, "Fetch " + script.name() + " for troll " + trollNumber);
        ScriptCategory category = script.category;
        int count = checkQuota(context, script, trollNumber);
        if (count >= category.quota) {
            Log.i(TAG, "Quota is exceeded for category '" + category + "': " + count + "/" + category.quota + ". Force usage ? " + force);
            if (!force) {
                throw new QuotaExceededException(category, count);
            }
        }

        String url = String.format(script.url, trollNumber, trollPassword);
        PublicScriptResponse spResult = doHttpGET(url);
        Log.i(TAG, "Public Script response: '" + spResult + "'");

        if (spResult.hasError()) {
            throw new PublicScriptException(spResult);
        } else {

            saveFetch(context, script, trollNumber);

            Map<String, String> result = Maps.newLinkedHashMap();

            Iterable<String> iterable = Splitter.on(";").split(spResult.getRaw());
            List<String> data = Lists.newArrayList(iterable);

            for (int i = 0; i < data.size(); i++) {
                String key = script.properties.get(i);
                String value = data.get(i);
                result.put(key, value);
            }
            return result;
        }

    }

}
