package com.example.paulina;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";

    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(
                context, RootActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(RootActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(context)
                .setContentTitle("Gibbets")
                .setContentText("Let's play now?")
                .setContentIntent(pIntent)
                .setDefaults(
                        Notification.DEFAULT_SOUND
                                | Notification.DEFAULT_VIBRATE)
                .setContentIntent(pIntent).setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher).build();
        notificationManager.notify(2, notification);
    }
}