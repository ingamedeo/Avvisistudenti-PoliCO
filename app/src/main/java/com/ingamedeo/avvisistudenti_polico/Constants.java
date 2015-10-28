package com.ingamedeo.avvisistudenti_polico;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by ingamedeo on 11/10/15.
 */
public class Constants {

    public static final String TAG = "polico";

    public static final String BROADCAST_ACTION = "com.ingamedeo.avvisistudenti_polico.HtmlParseService.BROADCAST";
    public static final String EXTENDED_DATA_STATUS = "com.ingamedeo.avvisistudenti_polico.HtmlParseService.STATUS";

    public enum ServiceStatus {
        DONE,
        ERROR
    }

    public enum Lang {
        ENGLISH,
        ITALIAN
    }

    public enum Intervals {
        _30MIN,
        _1H,
        _3H,
        _6H,
        _12H
    }

    public static final Intervals defaultInterval = Constants.Intervals._6H;

    private static final long INTERVAL_ONE_SECOND = 1000;

    public static String unescapeString(String string) {
        if (string == null)
            return null;

        StringBuilder sb = new StringBuilder(string);

        sb = sb.deleteCharAt(0);
        sb = sb.deleteCharAt(sb.length() - 1);

        return sb.toString().replaceAll("''", "'");
    }

    public static Lang getDefaultLang(Context context) {
        if (Locale.getDefault().getLanguage().toLowerCase().equals(context.getResources().getString(R.string.it_lang_code))) {
            return Lang.ITALIAN;
        } else {
            return Lang.ENGLISH;
        }
    }

    /* Context Required, SharedPreferences used if not null, otherwise will be initialized. */
    public static void setSyncPeriod(Context context, SharedPreferences sharedPreferences) {
        if (sharedPreferences==null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }

        String intervalsStr = sharedPreferences.getString(context.getResources().getString(R.string.preference_syncinterval), String.valueOf(defaultInterval.ordinal()));
        Constants.Intervals intervals = Constants.Intervals.values()[Integer.valueOf(intervalsStr)];

        long alarmManagerInterval;

        switch (intervals) {
            case _30MIN:
                alarmManagerInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES*2;
                break;
            case _1H:
                alarmManagerInterval = AlarmManager.INTERVAL_HOUR;
                break;
            case _3H:
                alarmManagerInterval = AlarmManager.INTERVAL_HOUR*3;
                break;
            case _6H:
                alarmManagerInterval = AlarmManager.INTERVAL_HOUR*6;
                break;
            case _12H:
                alarmManagerInterval = AlarmManager.INTERVAL_HALF_DAY;
                break;
            default:
                    /* Fallback again here, not really needed. */
                alarmManagerInterval = AlarmManager.INTERVAL_HOUR*6;
                break;
        }

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent serviceIntent = new Intent(context, HtmlParseService.class);
        PendingIntent alarmIntent = PendingIntent.getService(context, 0, serviceIntent, 0);

        // Cancel alarms
        try {
            alarmMgr.cancel(alarmIntent);
        } catch (Exception e) {
            Log.i(Constants.TAG, "No alarms cancelled.");
        }

        //INTERVAL_FIFTEEN_MINUTES > Constant Value: 900000 milliseconds
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, Constants.INTERVAL_ONE_SECOND, alarmManagerInterval, alarmIntent);
    }

    /*
     public static String[] getFilterPatterns(String filterString) {
        ArrayList<String> filterOut = new ArrayList<>();
        if (filterString!=null) {
            String patterns[] = filterString.split(Constants.filterSeparator);
            Log.i(Constants.TAG, "Pattern len: " + patterns.length);
            for (String pattern : patterns) {
                pattern = pattern.trim();
                if (pattern.length()>0) {
                    filterOut.add(pattern);
                }
            }
        }

        return filterOut.toArray(new String[filterOut.size()]);
    }
    */

    /* Debug Code */
    protected static void sampleNotification(Context context) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setContentTitle("Avvisi studenti - PoliCO")
                        .setContentText("Boot")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Boot"))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(10, mBuilder.build());
    }
}
