package org.zoumbox.mh.notifier.sp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class MhDlaSQLHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "mh-dla-notifier";

    public static final String SCRIPTS_TABLE = "scripts_logs";
    public static final String SCRIPTS_DATE_COLUMN = "script_date";
    public static final String SCRIPTS_SCRIPT_COLUMN = "script";
    public static final String SCRIPTS_CATEGORY_COLUMN = "category";
    public static final String SCRIPTS_TROLL_COLUMN = "troll_number";

    private static final String SCRIPTS_TABLE_CREATE =
            "CREATE TABLE " + SCRIPTS_TABLE + " (" +
                    SCRIPTS_DATE_COLUMN + " LONG, " +
                    SCRIPTS_SCRIPT_COLUMN + " TEXT, " +
                    SCRIPTS_CATEGORY_COLUMN + " TEXT, " +
                    SCRIPTS_TROLL_COLUMN + " TEXT);";

    MhDlaSQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SCRIPTS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
