package com.prod.almog.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KidsNotifierService extends Service {


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                final FirebaseService fb = new FirebaseService();
                fb.getSettings(new IResult<HashMap<String, String>>() {
                    @Override
                    public void accept(HashMap<String, String> _settings) {

                        long notificationPeriod = Long.parseLong(_settings.get("notificationPeriod"));
                        if(lastNotificationPeriod != notificationPeriod){
                            lastNotificationPeriod = notificationPeriod;
                            setResetEntrancesReceiver();
                        }
                    }
                });            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    private long lastNotificationPeriod = 0;

    private void setResetEntrancesReceiver() {
        cancelAlarm(this);
        setAlarm(this,lastNotificationPeriod);
    }


    AlarmManager am;
    public void setAlarm(Context context, final long period) {

        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        final Intent i = new Intent(context, KidsNotifierReceiver.class);
        final PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        Calendar calendar = Calendar.getInstance();
        am.setRepeating(AlarmManager.RTC_WAKEUP,0, 1000 * 60 * period,  pi); // Millisec * Second * Minute
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, KidsNotifierReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
