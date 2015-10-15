package com.ingamedeo.avvisistudenti_polico.db;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;

import com.ingamedeo.avvisistudenti_polico.Constants;

/**
 * Created by ingamedeo on 25/04/14.
 */

public class DbAdapter {

    private Context c; //Context
    private Uri contentUri;

    /* Constructor for DbAdapter */
    public DbAdapter(Context context) {
        this.c = context; //Set context
        contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, NewsTable.TABLE_NAME); //Build Up my Content Provider UR
    }

    private ContentValues createContentValues(String date, String title, String link) { //Creating ContentValues resolved by a ContentResolver (ContentProvider)
        ContentValues values = new ContentValues(); //Create new ContentValues Obj
        values.put(NewsTable.DATE, DatabaseUtils.sqlEscapeString(date));
        values.put(NewsTable.TITLE, DatabaseUtils.sqlEscapeString(title));
        values.put(NewsTable.LINK, DatabaseUtils.sqlEscapeString(link));
        return values; //Return values
    }

    public long insertNews(String date, String title, String link) { //Insert a new user in our database
        ContentValues createValues = createContentValues(date, title, link);
        Uri newUri = c.getContentResolver().insert(contentUri, createValues); //Insert!
        return ContentUris.parseId(newUri); //Get operation ID
    /* Inserts new record, Return -1 if error, or new record ID if successful */
    }

    public boolean checkNewsExists(String date, String title, String link) {
        date = DatabaseUtils.sqlEscapeString(date);
        title = DatabaseUtils.sqlEscapeString(title);
        link = DatabaseUtils.sqlEscapeString(link);
        Cursor result = c.getContentResolver().query(contentUri, NewsTable.COLUMNS, NewsTable.DATE + "=? AND " + NewsTable.TITLE + "=? AND " + NewsTable.LINK + "=?", new String[]{date, title, link}, NewsTable._ID + " DESC");
        boolean exists = result.getCount() > 0;
        result.close();
        return exists;
    }

    public boolean deleteAllNews() {
        //If WHERE clause = 1, all rows will be deleted and their number will be returned
        return c.getContentResolver().delete(contentUri, null, null) > 0; //Parameters: Table Name, WHERE clause, Strings which replace ?s in WHERE clause (Optional - Only if you put ?s in the WHERE clause)
        /* Deletes a user from the database, returns True if the number of rows deleted is > 0 (Returns exactly the number of rows), returns False (0 without > 0) if no rows that match specified pattern are found */
    }
}