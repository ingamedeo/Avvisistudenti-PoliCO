package com.ingamedeo.avvisistudenti_polico.db;

import android.provider.BaseColumns;

/**
 * Created by ingamedeo on 25/04/14.
 */

public interface NewsTable extends BaseColumns { //This extends BaseColumns implements "String	_ID	The unique ID for a row."

    String TABLE_NAME = "News"; //Table Name
    String DATE = "_date";
    String TITLE = "_title";
    String LINK = "_link";

    String[] COLUMNS = new String[] {_ID, DATE, TITLE, LINK};
}
