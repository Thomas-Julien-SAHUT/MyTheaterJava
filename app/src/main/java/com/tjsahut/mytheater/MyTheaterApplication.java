package com.tjsahut.mytheater;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MyTheaterApplication extends Application {
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate() {
        super.onCreate();

        String channelId = "com.example.mytheater.myApp";
        String description = "Notification d'accueuil";

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, Activity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder ;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            NotificationChannel notificationChannel  = new NotificationChannel(channelId,description, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.CYAN);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);

            builder = new Notification.Builder(this,channelId)
                    .setSmallIcon(R.drawable.ic_launcher_thumb)
                    .setContentTitle("Bonjour")
                    .setContentText("Quel cinéma recherchez-vous ?")
                    .setContentIntent(pendingIntent);

        } else {
            builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher_thumb)
                    .setContentTitle("Bonjour")
                    .setContentText("Quel cinéma recherchez-vous ?")
                    .setContentIntent(pendingIntent);
        }

        notificationManager.notify(1234, builder.build());

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .diskCacheFileCount(300)
                .threadPoolSize(5)
                .memoryCache(new WeakMemoryCache())
                .build();
        ImageLoader.getInstance().init(config);
    }
}
