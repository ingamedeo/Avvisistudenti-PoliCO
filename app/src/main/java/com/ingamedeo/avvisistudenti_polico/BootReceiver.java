package com.ingamedeo.avvisistudenti_polico;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Created by ingamedeo on 17/10/15.
 */

public class BootReceiver extends BroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.

            Constants.sampleNotification(context);

            alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent serviceIntent = new Intent(context, HtmlParseService.class);
            alarmIntent = PendingIntent.getService(context, 0, serviceIntent, 0);

            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
        }
    }
}
