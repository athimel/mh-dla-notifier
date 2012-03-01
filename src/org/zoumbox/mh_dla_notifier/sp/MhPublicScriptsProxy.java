package org.zoumbox.mh_dla_notifier.sp;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class MhPublicScriptsProxy {

    private static final String TAG = "MhDlaNotifier-" + MhPublicScriptsProxy.class.getSimpleName();

//    protected static String query(String url) {
//
//        String responseContent = "";
//        BufferedReader in = null;
//        try {
//            HttpClient client = new DefaultHttpClient();
//            HttpGet request = new HttpGet(url);
//            HttpResponse response = client.execute(request);
//            InputStream content = response.getEntity().getContent();
//            in = new BufferedReader(new InputStreamReader(content));
//            String line;
//            while ((line = in.readLine()) != null) {
//                responseContent += line;
//            }
//            in.close();
//        } catch (Exception eee) {
//            Log.e(TAG, "Exception", eee);
//        } finally {
//            if (in != null) {
//                try {
//                    in.close();
//                } catch (IOException ioe) {
//                    Log.e(TAG, "IOException", ioe);
//                }
//            }
//        }
//
//        Log.i(TAG, "Response: '" + responseContent + "'");
//
//        // TODO 01/03/2012 AThimel Handle errors
//
//        return responseContent;
//    }

    protected static String query(String url) {
        if (url.contains("SP_Profil2.php")) {
            return "104259;57;-75;-41;85;80;0;2012-02-28 16:58:55;8;4;13;4;4;6;360;361;0;5;0;0;0;0;0;585;0;1;0";
        } else if (url.contains("SP_Profil3.php")) {
            return "104259;DevelZimZoum;57;-75;-41;6;2012-02-25 01:22:55;3;0;0;0;2;22;88;6042";
        }
        return "104259;DevelZimZoum;Kastar;19;2011-01-21 14:07:48;;http://zoumbox.org/mh/DevelZimZoumMH.png;17;122;9;1900;20;0";
    }

    protected static final String SQL_COUNT = String.format("SELECT COUNT(*) FROM %s WHERE %s=? AND %s=? AND %s>=?",
            MhDlaSQLHelper.SCRIPTS_TABLE, MhDlaSQLHelper.SCRIPTS_TROLL_COLUMN, MhDlaSQLHelper.SCRIPTS_CATEGORY_COLUMN, MhDlaSQLHelper.SCRIPTS_DATE_COLUMN);

    protected static final String SQL_LAST_UPDATE = String.format("SELECT MAX(%s) FROM %s WHERE %s=? AND %s=?",
            MhDlaSQLHelper.SCRIPTS_DATE_COLUMN, MhDlaSQLHelper.SCRIPTS_TABLE, MhDlaSQLHelper.SCRIPTS_TROLL_COLUMN, MhDlaSQLHelper.SCRIPTS_SCRIPT_COLUMN);

    protected static int checkQuota(Activity activity, PublicScript script, String trollNumber) {

        Log.i(TAG, "Check quota for category " + script + "(" + script.category + ") and troll " + trollNumber);

        MhDlaSQLHelper helper = new MhDlaSQLHelper(activity);
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

    protected static void saveFetch(Activity activity, PublicScript script, String trollNumber) {

        Log.i(TAG, "Save fetch for category " + script + "(" + script.category + ") and troll " + trollNumber);

        MhDlaSQLHelper helper = new MhDlaSQLHelper(activity);
        SQLiteDatabase database = helper.getWritableDatabase();

        ContentValues values = new ContentValues(4);
        values.put(MhDlaSQLHelper.SCRIPTS_DATE_COLUMN, System.currentTimeMillis());
        values.put(MhDlaSQLHelper.SCRIPTS_SCRIPT_COLUMN, script.name());
        values.put(MhDlaSQLHelper.SCRIPTS_CATEGORY_COLUMN, script.category.name());
        values.put(MhDlaSQLHelper.SCRIPTS_TROLL_COLUMN, trollNumber);

        database.insert(MhDlaSQLHelper.SCRIPTS_TABLE, null, values);

        database.close();
    }

    public static Date geLastUpdate(Activity activity, PublicScript script, String trollNumber) {

        Log.i(TAG, "Get last update for category " + script + "(" + script.category + ") and troll " + trollNumber);

        MhDlaSQLHelper helper = new MhDlaSQLHelper(activity);
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

    public static Map<String, String> fetch(Activity activity, PublicScript script, String trollNumber, String trollPassword, boolean force) throws QuotaExceededException {

        Log.i(TAG, "Fetch " + script.name() + " for troll " + trollNumber);
        ScriptCategory category = script.category;
        int count = checkQuota(activity, script, trollNumber);
        if (count >= category.quota) {
            Log.i(TAG, "Quota is exceeded for category '" + category + "': " + count + "/" + category.quota + ". Force usage ? " + force);
            if (!force) {
                throw new QuotaExceededException(category, count);
            }
        }

        String url = String.format(script.url, trollNumber, trollPassword);
        String rawResult = query(url);
        saveFetch(activity, script, trollNumber);

        Map<String, String> result = Maps.newLinkedHashMap();

        Iterable<String> iterable = Splitter.on(";").split(rawResult);
        List<String> data = Lists.newArrayList(iterable);

        for (int i = 0; i < data.size(); i++) {
            result.put(script.properties.get(i), data.get(i));
        }

        return result;
    }

}
