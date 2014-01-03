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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.zoumbox.mh_dla_notifier.MhDlaNotifierConstants;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class MhDlaSQLHelper extends SQLiteOpenHelper {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + MhDlaSQLHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_NAME = "mh-dla-notifier";

    public static final String SCRIPTS_TABLE = "scripts_logs";
    public static final String SCRIPTS_COLUMN_DATE = "script_date";
    public static final String SCRIPTS_COLUMN_SCRIPT = "script";
    public static final String SCRIPTS_COLUMN_CATEGORY = "category";
    public static final String SCRIPTS_COLUMN_TROLL = "troll_number";

    public static final String LOCKS_TABLE = "locks";
    public static final String LOCKS_COLUMN_ID = "id";
    public static final String LOCKS_COLUMN_DATE = "date";
    public static final String LOCKS_COLUMN_SCRIPT = "script";
    public static final String LOCKS_COLUMN_TROLL_ID = "trollId";

    private static final String SCRIPTS_TABLE_CREATE =
            "CREATE TABLE " + SCRIPTS_TABLE + " (" +
                    SCRIPTS_COLUMN_DATE + " LONG, " +
                    SCRIPTS_COLUMN_SCRIPT + " TEXT, " +
                    SCRIPTS_COLUMN_CATEGORY + " TEXT, " +
                    SCRIPTS_COLUMN_TROLL + " TEXT);";

    private static final String LOCKS_TABLE_CREATE =
            "CREATE TABLE " + LOCKS_TABLE + " (" +
                    LOCKS_COLUMN_ID + " TEXT, " +
                    LOCKS_COLUMN_DATE + " LONG, " +
                    LOCKS_COLUMN_SCRIPT + " TEXT, " +
                    LOCKS_COLUMN_TROLL_ID + " TEXT);";

    MhDlaSQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.w(TAG, "Creating " + SCRIPTS_TABLE + " table");
        db.execSQL(SCRIPTS_TABLE_CREATE);

        Log.w(TAG, "Creating " + LOCKS_TABLE + " table");
        db.execSQL(LOCKS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "onUpgrade, oldVersion=" + oldVersion + " ; newVersion=" + newVersion);
        if (oldVersion < 2 && newVersion >= 2) {
            Log.w(TAG, "Creating " + LOCKS_TABLE + " table");
            db.execSQL(LOCKS_TABLE_CREATE);
        }
    }

}
