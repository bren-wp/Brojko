package com.brendigo.brojko;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import java.util.Calendar;

/**
 * Local daily reminder. No server, background network traffic or exact alarm.
 * Only active when enabled in settings. Notifications occur around 18:00.
 */
public final class AppReminder extends BroadcastReceiver {
    private static final int REMINDER_ID=2048;
    private static final String CHANNEL="brojko_daily_challenge";
    private static PendingIntent alarmIntent(Context context){
        Intent i=new Intent(context,AppReminder.class);
        return PendingIntent.getBroadcast(context,REMINDER_ID,i,
            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    public static void configure(Context context,boolean enabled){
        AlarmManager manager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if(manager==null)return;
        PendingIntent pi=alarmIntent(context);
        manager.cancel(pi);
        if(!enabled)return;
        Calendar c=Calendar.getInstance();
        Calendar now=Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,18);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);
        if(!c.after(now))c.add(Calendar.DAY_OF_YEAR,1);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),
            AlarmManager.INTERVAL_DAY,pi);
    }
    @Override public void onReceive(Context context,Intent intent){
        if(!context.getSharedPreferences("brojko",Context.MODE_PRIVATE)
            .getBoolean("notifications",false))return;
        if(Build.VERSION.SDK_INT>=33&&context.checkSelfPermission(
            Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return;
        NotificationManager manager=(NotificationManager)context.getSystemService(
            Context.NOTIFICATION_SERVICE);
        if(manager==null)return;
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel channel=new NotificationChannel(CHANNEL,
                "Brojko dnevni izazov",NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Podsjetnik za dnevnu logičku igru Brojko.");
            manager.createNotificationChannel(channel);
        }
        Intent open=new Intent(context,MainActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent tap=PendingIntent.getActivity(context,REMINDER_ID,open,
            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder=Build.VERSION.SDK_INT>=26?
            new Notification.Builder(context,CHANNEL):new Notification.Builder(context);
        Notification notification=builder.setSmallIcon(R.drawable.ic_brojko)
            .setContentTitle("Brojko — novi dnevni izazov")
            .setContentText("Spajaj brojeve, osvoji zvjezdice i postavi novi rekord!")
            .setContentIntent(tap).setAutoCancel(true).build();
        manager.notify(REMINDER_ID,notification);
    }
}
