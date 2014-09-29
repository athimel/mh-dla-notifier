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

import org.zoumbox.mh_dla_notifier.MhDlaNotifierConstants;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class MhDlaSQLHelper extends SQLiteOpenHelper {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + MhDlaSQLHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_NAME = "mh-dla-notifier";

    public static final String SCRIPTS_TABLE = "scripts_logs";
    public static final String SCRIPTS_ID_COLUMN = "id";
    public static final String SCRIPTS_START_DATE_COLUMN = "start_date";
    public static final String SCRIPTS_END_DATE_COLUMN = "end_date";
    public static final String SCRIPTS_SCRIPT_COLUMN = "script";
    public static final String SCRIPTS_CATEGORY_COLUMN = "category";
    public static final String SCRIPTS_TROLL_COLUMN = "troll_number";
    public static final String SCRIPTS_STATUS_COLUMN = "status";

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ERROR = "ERROR";
    public static final String STATUS_SUCCESS = "SUCCESS";

    private static final String SCRIPTS_TABLE_CREATE =
            "CREATE TABLE " + SCRIPTS_TABLE + " (" +
                    SCRIPTS_ID_COLUMN + " TEXT, " +
                    SCRIPTS_START_DATE_COLUMN + " LONG, " +
                    SCRIPTS_END_DATE_COLUMN + " LONG, " +
                    SCRIPTS_SCRIPT_COLUMN + " TEXT, " +
                    SCRIPTS_CATEGORY_COLUMN + " TEXT, " +
                    SCRIPTS_STATUS_COLUMN + " TEXT, " +
                    SCRIPTS_TROLL_COLUMN + " TEXT" +
                    ");";

    MhDlaSQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.w(TAG, "Creating " + SCRIPTS_TABLE + " table");
        db.execSQL(SCRIPTS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.w(TAG, "onUpgrade, oldVersion=" + oldVersion + " ; newVersion=" + newVersion);
        if (oldVersion < 2 && newVersion >= 2) {
            Log.w(TAG, "Rename script_date to " + SCRIPTS_END_DATE_COLUMN);
            db.execSQL("ALTER TABLE " + SCRIPTS_TABLE + " RENAME TO tmp_" + SCRIPTS_TABLE);

            db.execSQL(SCRIPTS_TABLE_CREATE);

            db.execSQL(
                    "INSERT INTO " + SCRIPTS_TABLE +
                        "(" +
                            SCRIPTS_START_DATE_COLUMN + ", " +
                            SCRIPTS_END_DATE_COLUMN + ", " +
                            SCRIPTS_SCRIPT_COLUMN + ", " +
                            SCRIPTS_CATEGORY_COLUMN + ", " +
                            SCRIPTS_STATUS_COLUMN + ", " +
                            SCRIPTS_TROLL_COLUMN
                        + ")" +
                    "SELECT " +
                        "script_date, " +
                        "script_date, " +
                        SCRIPTS_SCRIPT_COLUMN + ", " +
                        SCRIPTS_CATEGORY_COLUMN + ", " +
                        " '"+STATUS_SUCCESS+"', " +
                        SCRIPTS_TROLL_COLUMN +
                    " FROM tmp_" + SCRIPTS_TABLE);

            db.execSQL("DROP TABLE tmp_" + SCRIPTS_TABLE);

        }
    }

}
