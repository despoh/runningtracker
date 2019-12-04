package com.example.runningtracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class TrackerService extends Service {

    final String CHANNEL_ID = "MY_CHANNEL";
    final int NOTIFY_ID = 2222;
    String exerciseMode;
    long startDateAndTime;

    private IBinder binder = new MyBinder();

    GPSTracker tracker;

    public TrackerService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        tracker.stopGPS();
        Toast.makeText(this, "GPS STOPPED", Toast.LENGTH_LONG).show();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        exerciseMode = intent.getStringExtra("mode");
        startDateAndTime = intent.getLongExtra("startDateAndTime",0);
        tracker = new GPSTracker(getApplicationContext());
        tracker.startGPS();
        Toast.makeText(this, "GPS START", Toast.LENGTH_LONG).show();
        createChannel();
        sendNotification();
        return START_STICKY;
    }

    public class MyBinder extends Binder {
        TrackerService getService(){
            return TrackerService.this;
        }
    }

    public void sendNotification(){
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra("return",true);
        resultIntent.putExtra("mode",exerciseMode);
        resultIntent.putExtra("startDateAndTime",startDateAndTime);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        final Icon icon = Icon.createWithResource(this,
                android.R.drawable.ic_dialog_info);

        Notification.Action action =
                new Notification.Action.Builder(icon, "Open", pendingIntent)
                        .build();

        Notification notification =
                new Notification.Builder(this,
                        CHANNEL_ID)
                        .setContentTitle("Running Tracker")
                        .setContentText("Tracking your journey")
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setChannelId(CHANNEL_ID)
                        .setContentIntent(pendingIntent)
                        .setActions(action)
                        .build();

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFY_ID, notification);
        startForeground(NOTIFY_ID,notification);
    }

    public void createChannel(){
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        CharSequence name = "Running Tracker";
        String description = "Tracking";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel =
                new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        channel.enableLights(true);
        channel.setLightColor(Color.RED);
        channel.enableVibration(true);
        channel.setVibrationPattern(
                new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        notificationManager.createNotificationChannel(channel);
    }

}
