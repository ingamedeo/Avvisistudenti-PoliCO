package com.ingamedeo.avvisistudenti_polico.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ingamedeo on 25/04/14.
 */

/* 1st Level */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "policonews.db"; //This is database name
    private static final int DATABASE_VERSION = 1; //This is database version

    /* Create Table Query */
    private static final String DATABASE_CREATE = "create table "
            + NewsTable.TABLE_NAME
            + "("
            + NewsTable._ID + " integer primary key autoincrement, " //This is the primary key and autoincrement
            + NewsTable.DATE + " text not null, " //This is a text, should not be null
            + NewsTable.TITLE + " text not null, " //This is a text, should not be null
            + NewsTable.LINK + " text not null unique" //Unique
            + ");";

    /* Constructor for DatabaseHelper */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) { //This method is called when database is created for the first time
        db.execSQL(DATABASE_CREATE); //Exec SQL Query above (Create Users Table)
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { //This method is called when DATABASE_VERSION is changed
        Log.w(DatabaseHelper.class.getName(), "Upgrading database from " + oldVersion + " to " + newVersion); //Log current operation
        db.execSQL("DROP TABLE IF EXISTS " + NewsTable.TABLE_NAME); //Destroy old table
        onCreate(db); //Create new table structure
    }
}
