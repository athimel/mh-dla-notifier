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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Arno <arno@zoumbox.org>
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
