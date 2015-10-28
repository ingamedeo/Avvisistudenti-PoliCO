package com.ingamedeo.avvisistudenti_polico;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ingamedeo.avvisistudenti_polico.db.ContentProviderDb;
import com.ingamedeo.avvisistudenti_polico.db.NewsTable;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int SETTINGS_RESULT = 0;
    private static final int LOADER_ID = 1;

    private Context context = MainActivity.this;

    private NewsAdapter newsAdapter;
    private Uri contentUri;

    private SharedPreferences sharedPreferences;
    private boolean isFirstStart = true;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadfromPref();

        setContentView(R.layout.activity_main);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccent);

        ListView newsContainer = (ListView) findViewById(R.id.newsContainer);
        newsAdapter = new NewsAdapter(getApplicationContext(), null, 0);
        newsContainer.setAdapter(newsAdapter);

        contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, NewsTable.TABLE_NAME);

        //alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        //Intent intent = new Intent(context, HtmlParseService.class);
        //alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        //alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
        //        AlarmManager.INTERVAL_FIFTEEN_MINUTES,
        //        AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);

        //Init our loader
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
        //getLoaderManager().initLoader(LOADER_ID, null, this);

        /* Why do you call restartLoader instead of initLoader?
        *
        * I've found out that restart does exactly the same thing as init if our loader doesn't exist yet
        */

        newsContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent newsIntent = new Intent(MainActivity.this, NewsActivity.class);
                Cursor c = (Cursor) parent.getItemAtPosition(position);
                newsIntent.putExtra(getResources().getString(R.string.news_extra_title), Constants.unescapeString(c.getString(c.getColumnIndex(NewsTable.TITLE))));
                newsIntent.putExtra(getResources().getString(R.string.news_extra_link), Constants.unescapeString(c.getString(c.getColumnIndex(NewsTable.LINK))));
                startActivity(newsIntent);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                runHtmlParseService();
            }
        });

        /* First Startup Handling */
        if (isFirstStart) {
            swipeRefreshLayout.setRefreshing(true); //Set refreshing

            runHtmlParseService();
            /* Set to false */
            getSharedPreferences().edit().putBoolean(getResources().getString(R.string.preference_isfirststart), false).apply();
        }

    }

    private BroadcastReceiver serviceStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(Constants.TAG, "Receiver called!");
            Constants.ServiceStatus serviceStatus = Constants.ServiceStatus.values()[intent.getIntExtra(Constants.EXTENDED_DATA_STATUS, 0)];

            /* Stop refreshing */
            if (swipeRefreshLayout!=null) {
                swipeRefreshLayout.setRefreshing(false);
            }

            switch (serviceStatus) {
                case DONE:
                    //Do nothing
                    break;
                case ERROR:
                    Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.failed_update_news), Snackbar.LENGTH_LONG)
                            .setAction(getResources().getString(R.string.retry), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    swipeRefreshLayout.setRefreshing(true); //Set refreshing
                                    runHtmlParseService();
                                }
                            })
                            .setActionTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                            .show();
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(serviceStatusReceiver, new IntentFilter(Constants.BROADCAST_ACTION));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceStatusReceiver);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivityForResult(settingsIntent, SETTINGS_RESULT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void runHtmlParseService() {
        Intent serviceIntent = new Intent(MainActivity.this, HtmlParseService.class);
        startService(serviceIntent);
    }

    private SharedPreferences getSharedPreferences() {
        if (sharedPreferences==null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        }
        return sharedPreferences;
    }

    private void loadfromPref() {
        isFirstStart = getSharedPreferences().getBoolean(getResources().getString(R.string.preference_isfirststart), true);
        boolean isAutoSyncEnabled = getSharedPreferences().getBoolean(getResources().getString(R.string.preference_sync), true);
        toggleBootReceiver(isAutoSyncEnabled);

        String filterString = getSharedPreferences().getString(getResources().getString(R.string.filter_preference), null);
        //Constants.getFilterPatterns(filterString); //WTF?!
    }

    private void toggleBootReceiver(boolean enable) {
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                contentUri,
                NewsTable.COLUMNS,
                null,
                null,
                NewsTable._ID + " ASC"); /* (Should adjust updated messages automatically) */
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            newsAdapter.swapCursor(cursor);
        } else {
            newsAdapter.swapCursor(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        newsAdapter.swapCursor(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode==SETTINGS_RESULT) {

            swipeRefreshLayout.setRefreshing(true); //Set refreshing

            //Set sync period. (Apart from pref sync cos I don't want to set it when the app is first opened. (BootReceiver does it)
            Constants.setSyncPeriod(context, sharedPreferences);

            /* Load new pref */
            loadfromPref();

            /* NOTE: Removed. The alarm set by setSyncPeriod is already calling the service causing the screen to flash. */
            /* Call service to refresh */
            //runHtmlParseService();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
