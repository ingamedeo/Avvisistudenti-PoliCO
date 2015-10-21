package com.ingamedeo.avvisistudenti_polico;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ingamedeo.avvisistudenti_polico.db.DbAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ingamedeo on 10/10/15.
 */
public class HtmlParseService extends IntentService {

    private SharedPreferences sharedPreferences;
    private Constants.Lang lang;
    private DbAdapter dbAdapter;
    private String filterStr = null; //Don't change this line...I'm still working on it

    public HtmlParseService() {
        super(HtmlParseService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        /* Demo notification - helps ensure the service is launched */
        Constants.sampleNotification(HtmlParseService.this);

        dbAdapter = new DbAdapter(this); //New dpAdapter instance, used for everything except query()

        initArgsfromPref(); //Load args from SharedPreferences

        try {
            Document document = Jsoup.connect(generateURLString(lang)).get();
            Elements divNewsList = document.select("#avvisi-studenti-list-pagina-interna ul");
            if (divNewsList.size()==1) { //There should be only one
                Elements newsItems = divNewsList.get(0).getElementsByTag("li");

                String[] newTitles = scanNews(newsItems);

                if (newTitles!=null && newTitles.length>0) {

                    String contentText;
                    if (newTitles.length==1) {
                        contentText = newTitles[0];
                    } else {
                        contentText = "There is " + newTitles.length + " unread news!";
                    }

                    Intent notificationIntent = new Intent(this, MainActivity.class);
                    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(this)
                                    .setContentTitle("Avvisi studenti - PoliCO")
                                    .setContentText(contentText)
                                    .setStyle(new NotificationCompat.BigTextStyle()
                                            .bigText(contentText))
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                                    .setContentIntent(contentIntent)
                                    .setAutoCancel(true);

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(10, mBuilder.build());
                }

                /* Clean Up */
                dbAdapter.deleteAllNews();

                for (int i = 0; i < newsItems.size(); i++) {
                    Element item = newsItems.get(i);

                    Elements date = item.select(".avvisi-studenti-list-date");
                    Elements link = item.select("a");

                    if (date.size()==1 && link.size()==1) {
                        Log.i("tag", "Date: " + date.text());
                        Log.i("tag", "Title: " + link.text());
                        Log.i("tag", "Link: " + "http://www.polo-como.polimi.it/" + link.attr("href"));

                        dbAdapter.insertNews(date.text(), link.text(), "http://www.polo-como.polimi.it/"+link.attr("href"));

                    } else {
                        Log.e(Constants.TAG, "Invalid document; expected 1, found more...");
                        broadcastServiceStatus(Constants.ServiceStatus.ERROR);
                    }
                }
                broadcastServiceStatus(Constants.ServiceStatus.DONE);
            } else {
                Log.e(Constants.TAG, "Invalid document; expected 1, found more...");
                broadcastServiceStatus(Constants.ServiceStatus.ERROR);
            }
        } catch (IOException e) {
            e.printStackTrace();
            broadcastServiceStatus(Constants.ServiceStatus.ERROR);
        }

    }

    private void broadcastServiceStatus(Constants.ServiceStatus serviceStatus) {
        Intent broadcastIntent = new Intent(Constants.BROADCAST_ACTION)
                // Puts the status into the Intent
                .putExtra(Constants.EXTENDED_DATA_STATUS, serviceStatus.ordinal());
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    private void initArgsfromPref() {
        if (sharedPreferences==null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        }

        filterStr = sharedPreferences.getString(getResources().getString(R.string.filter_preference), null);
        /* Android ListPreference seems to accept Strings only, i don't get why, but that's it. */
        String langIndexStr = sharedPreferences.getString(getResources().getString(R.string.preference_lang), String.valueOf(Constants.getDefaultLang(this).ordinal()));
        lang = Constants.Lang.values()[Integer.valueOf(langIndexStr)];
        Log.i(Constants.TAG, "Lang: " + lang);
    }

    private String generateURLString(Constants.Lang lang) {
        if (lang==Constants.Lang.ITALIAN) {
            return getResources().getString(R.string.poli_como_url_prefix)+getResources().getString(R.string.poli_como_url_suff_it);
        } else {
            return getResources().getString(R.string.poli_como_url_prefix)+getResources().getString(R.string.poli_como_url_suff_en);
        }
    }

    private String[] scanNews(Elements newsItems) {

        ArrayList<String> newTitles = new ArrayList<>();

        for (Element item : newsItems) {

            Elements date = item.select(".avvisi-studenti-list-date");
            Elements link = item.select("a");
            String text = link.text();

            if (date.size()==1 && link.size()==1) {

                /* Check link.text() contains filter keywords ... + Db Check */
                if (((filterStr!=null && filterStr.contains(text)) || filterStr==null) && !dbAdapter.checkNewsExists(date.text(), text, "http://www.polo-como.polimi.it/"+link.attr("href"))) {
                    newTitles.add(link.text());
                }

                //if (!dbAdapter.checkNewsExists(date.text(), text, "http://www.polo-como.polimi.it/"+link.attr("href"))) {
                //}
            } else {
                return null;
            }
        }
        return newTitles.toArray( new String[newTitles.size()] );
    }
}
