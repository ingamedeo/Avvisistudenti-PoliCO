package com.ingamedeo.avvisistudenti_polico;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class NewsActivity extends AppCompatActivity {

    private WebView newsWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Display progress
        getWindow().requestFeature(Window.FEATURE_PROGRESS);

        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.activity_news);

        Bundle b = getIntent().getExtras();
        final String title = b.getString(getResources().getString(R.string.news_extra_title));
        final String link = b.getString(getResources().getString(R.string.news_extra_link));

        setTitle(title);

        // Makes Progress bar Visible
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

        newsWebView = (WebView) findViewById(R.id.newsWebView);
        newsWebView.getSettings().setJavaScriptEnabled(true);
        newsWebView.getSettings().setBuiltInZoomControls(true);

        newsWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                //Make the bar disappear after URL is loaded, and changes string to Loading...
                setTitle(getResources().getString(R.string.loading)+"... ("+ progress+"%)");
                setProgress(progress * 100); //Make the bar disappear after URL is loaded

                // Return the app name after finish loading
                if (progress == 100)
                    setTitle(title);
            }
        });

        newsWebView.loadUrl(link);

    }

}
