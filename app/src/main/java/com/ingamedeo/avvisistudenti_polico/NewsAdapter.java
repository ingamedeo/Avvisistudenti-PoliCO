package com.ingamedeo.avvisistudenti_polico;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.CursorAdapter;
import android.widget.TextView;

import com.ingamedeo.avvisistudenti_polico.db.NewsTable;

/**
 * Created by ingamedeo on 25/07/15.
 */

public class NewsAdapter extends CursorAdapter {

    private static class ViewHolder {
        public TextView newsDate;
        public TextView newsTitle;
    }

    public NewsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();
        View v = null;

        LayoutInflater inflater = LayoutInflater.from(context);

        v = inflater.inflate(R.layout.list_item_news, null);
        holder.newsDate = (TextView) v.findViewById(R.id.newsDate);
        holder.newsTitle = (TextView) v.findViewById(R.id.newsTitle);

        v.setTag(holder);

        return v;
    }

    @Override
    public void bindView(View v, Context c, Cursor cursor) {

        /* We already have our holder ready at this stage */
        ViewHolder holder = (ViewHolder) v.getTag();

        String date = Constants.unescapeString(cursor.getString(cursor.getColumnIndex(NewsTable.DATE)));
        String title = Constants.unescapeString(cursor.getString(cursor.getColumnIndex(NewsTable.TITLE)));

        holder.newsDate.setText(date);
        holder.newsTitle.setText(title);
    }


}
