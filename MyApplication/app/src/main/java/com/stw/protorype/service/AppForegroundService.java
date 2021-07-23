/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Fri, 10 Jan 2020 17:13:33 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Fri, 10 Jan 2020 17:12:23 +0100
 */

package com.stw.protorype.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Pair;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.streamwide.smartms.lib.core.api.environment.logger.STWLoggerHelper;
import com.stw.protorype.MainConstant;
import com.stw.protorype.R;
import com.stw.protorype.ui.activity.loneworker.LoneWorkerActivity;

/*
 * Due to Android background execution limitation, it's necessary to have a
 * foreground service started while a given service is in running, to
 * prevent kill of tasks. For this, a service is started to handle notifications of running services as :
 * 
 * * Lone Worker service
 * * Geoloc service
 */

public class AppForegroundService extends Service {

 
    @Override
    public void onCreate()
    {

        /*
         * Update notification is mandatory especially when start this service
         * in foreground and call stopSelf immediately
         */
        STWLoggerHelper.LOGGER.d(Pair.create("AppForegroundService", "LoneWorker switched to on"), MainConstant.LOGIN,
                        "Start notification service");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        String action = intent.getAction();

        if (action == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        String content = null;
        String description = null;
        int notificationId = 0;
        if (NotificationConstant.NotificationServiceAction.LONE_WORKER_NOTIFICATION_SERVICE.equals(action)) {
            content = "LoneWorker";
            description = "Notification for LoneWorker service";
            notificationId = NotificationConstant.NotificationKey.LONE_WORKER_SERVICE_NOTIFICATION_ID;

        } else if (NotificationConstant.NotificationServiceAction.GEOLOCATION_NOTIFICATION_SERVICE.equals(action)) {
            content = "Geolocation";
            description = "Notification for Geolocation service";
            notificationId = NotificationConstant.NotificationKey.GEOLOCATION_SERVICE_NOTIFICATION_ID;
        } else if (NotificationConstant.NotificationServiceAction.VOIP_NOTIFICATION_SERVICE.equals(action)) {
            content = "VoIP Call";
            description = "Notification for VoIP calls service";
            notificationId = NotificationConstant.NotificationKey.VOIP_SERVICE_NOTIFICATION_ID;
        }

        createOrUpdateServiceNotification(content,
                description,
                R.drawable.ic_service,
                NotificationConstant.ChannelId.FOREGROUND_SERVICE_CHANNEL_ID,
                notificationId,
                new Intent(this, LoneWorkerActivity.class));

        /**
         * else start other notification services using action
         */
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    /**
     * Create or update notification for foreground service
     * 
     * @param content
     * @param description
     * @param icon
     * @param channelId
     * @param notificationId
     * @param intent
     */
    private void createOrUpdateServiceNotification(String content, String description, int icon, String channelId,
                                                   int notificationId, Intent intent)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelId);
                if (notificationChannel == null) {
                    notificationChannel = new NotificationChannel(channelId,
                                    NotificationConstant.NotificationChannelName.NOTIFICATION_CHANNEL_NAME,
                                    NotificationManager.IMPORTANCE_LOW);
                    notificationChannel.setShowBadge(false);

                    if (!TextUtils.isEmpty(description)) {
                        notificationChannel.setDescription(description);
                    }

                    notificationManager.createNotificationChannel(notificationChannel);
                }
            }
        }

        // This intent will be fired when the notification is tapped
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1001, intent, 0);

        //@formatter:off
        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(this, channelId)
                .setSmallIcon(icon)
                .setContentTitle(content)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setWhen(System.currentTimeMillis())
                .setContentText(description)
                .setContentIntent(pendingIntent)
                .setShowWhen(false);
        //@formatter:on

        Notification notification = builder.build();
        notification.sound = null;
        notification.vibrate = null;
        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_FOREGROUND_SERVICE;
        startForeground(notificationId, notification);

    }

    /**
     * Call this method to stop notification service
     */

    public static void stopNotificationService(Context context, int notificationID, String channelID)
    {
        NotificationManager notificationManager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelID);

            if (notificationChannel != null) {
                STWLoggerHelper.LOGGER.d(Pair.create("AppForegroundService", "deleteChannel"), "NOTIFICATION",
                                "delete NotificationChannel with Id : " + channelID);

                notificationManager.deleteNotificationChannel(channelID);
            }
        }
    }

    @Override
    public void onDestroy()
    {
        stopNotificationService(getApplicationContext(),
                        NotificationConstant.NotificationKey.LONE_WORKER_SERVICE_NOTIFICATION_ID,
                        NotificationConstant.ChannelId.FOREGROUND_SERVICE_CHANNEL_ID);
        super.onDestroy();
    }
}
