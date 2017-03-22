package com.prod.almog.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Calendar;

public class Boot extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context,"איתחול גנים",Toast.LENGTH_SHORT).show();
        if(Helper.me().alarm == null) {
            Helper.me().alarm = new Alarm();
            Helper.me().alarm.setAlarm(context);
        }
    }


    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, Boot.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
