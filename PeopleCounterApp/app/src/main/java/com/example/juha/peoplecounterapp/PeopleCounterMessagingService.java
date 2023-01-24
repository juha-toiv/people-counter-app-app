package com.example.juha.peoplecounterapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

// TODO: Push Notifications not working properly.
public class PeopleCounterMessagingService extends FirebaseMessagingService {

    private static int NOTIFICATION_ID = 0;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isDisplayNotificationEnabled = sharedPreferences.getBoolean("key_notifications_new_message", true);
        if (firebaseAuth.getCurrentUser() != null && isDisplayNotificationEnabled) {
            if (remoteMessage.getNotification() != null) {
                String messageTitle = remoteMessage.getNotification().getTitle();
                String messageText = remoteMessage.getNotification().getBody();
                String deviceId = null;
                if (remoteMessage.getData().containsKey(getString(R.string.notification_data_key_device_id))) {
                    deviceId = remoteMessage.getData().get(getString(R.string.notification_data_key_device_id));
                }
                displayPushNotification(messageTitle, messageText, deviceId);
            }
        }
    }

    // TODO: Notification light, sound and vibration are not working.
    private void displayPushNotification(String messageTitle, String messageText, String deviceId) {
        if (messageTitle == null || messageTitle.isEmpty()) {
            messageTitle = getString(R.string.app_name);
        }
        if (messageText == null || messageText.isEmpty()) {
            messageText = getString(R.string.notification_detected_motion_text);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notification_channe_id_default))
                .setContentTitle(messageTitle)
                .setContentText(messageText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if (deviceId != null) {
            Intent intent = new Intent(getApplicationContext(), DeviceDetailActivity.class);
            intent.putExtra(DeviceDetailActivity.DEVICE_ID, deviceId);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isVibrate = sharedPreferences.getBoolean("key_notifications_new_message_vibrate", true);
        String ringToneString = sharedPreferences.getString("key_notifications_new_message_ringtone", "DEFAULT_SOUND");
        String lightColorString = sharedPreferences.getString("key_notifications_new_message_light_color", getString(R.string.preference_no_color));
        if (lightColorString.equals(getString(R.string.preference_no_color)) == false) {
            builder.setLights(Color.parseColor(lightColorString), 1000, 1000);
        }
        if (isVibrate) {
            builder.setVibrate(new long[]{1000, 1000});
        }
        builder.setSound(Uri.parse(ringToneString));
        builder.setAutoCancel(true);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }

    // TODO: Initialize notification channel for devices with SDK version >= 26.
    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("default", getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(getString(R.string.notification_channel_desc));
        notificationManager.createNotificationChannel(channel);
    }

}
