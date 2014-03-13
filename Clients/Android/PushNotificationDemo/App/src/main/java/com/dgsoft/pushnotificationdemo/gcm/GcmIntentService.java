package com.dgsoft.pushnotificationdemo.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.dgsoft.pushnotificationdemo.MainActivity;
import com.dgsoft.pushnotificationdemo.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService{
    public static final String TAG = "GCM Demo";
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;

    public GcmIntentService() {
        super(GcmIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        Bundle extras = intent.getExtras();
        Log.i(TAG, "Received extras: " + extras.toString());
        if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            createNotification("Send error occured!", "GCM", "NONE");
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            createNotification("Messages deleted on the server!", "GCM", "NONE");
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            String message = extras.getString("message");
            String senderName = extras.getString("senderName");
            String recipientNames = extras.getString("recipientNames");
            createNotification(message, senderName, recipientNames);
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);//Signals "work completed", must be called to release wakelock
    }

    private void createNotification(String message, String senderName, String recipientNames) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setAutoCancel(true)
                        .setContentTitle(senderName + " says:")
                        .setContentText( "\"" + message + "\"")
                        .setSubText("Recipients: " + recipientNames.replace("#", ","))
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentIntent(pendingIntent);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}