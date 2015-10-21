package com.ingamedeo.avvisistudenti_polico;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.util.Locale;

/**
 * Created by ingamedeo on 11/10/15.
 */
public class Constants {

    public static final String TAG = "polico";

    public static final String BROADCAST_ACTION = "com.ingamedeo.avvisistudenti_polico.HtmlParseService.BROADCAST";
    public static final String EXTENDED_DATA_STATUS = "com.ingamedeo.avvisistudenti_polico.HtmlParseService.STATUS";

    public static enum ServiceStatus {
        DONE,
        ERROR
    }

    public enum Lang {
        ENGLISH,
        ITALIAN
    }

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
