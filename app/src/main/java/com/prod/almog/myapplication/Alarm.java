package com.prod.almog.myapplication;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;

import java.util.Calendar;

public class Alarm extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Helper.me().toast("בודק אם צריך לאפס כניסות");
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if(hour == Integer.parseInt(Helper.me().settings.get("clearEntrances"))) {
            Helper.me().toast("מאפס כניסות...");
            Helper.me().clearStopSendingList();
            Helper.me().clearEntrances();
        }

    }

    AlarmManager am;
    public void setAlarm(Context context) {
        Toast.makeText(context,"בודק תזמון",Toast.LENGTH_SHORT).show();
        if(am == null) {
            Toast.makeText(context,"מאתחל תזמון",Toast.LENGTH_SHORT).show();
            Calendar calendar = Calendar.getInstance();
            am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(context, Alarm.class);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * 60, pi); // Millisec * Second * Minute
        }
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
