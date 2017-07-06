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
import java.util.HashMap;

public class ResetEntrancesReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        final int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        Manager.me().setContext(context);
        FirebaseService fb = new FirebaseService();
        fb.getSettings(new IResult<HashMap<String, String>>() {
            @Override
            public void accept(HashMap<String, String> stringStringHashMap) {
                HashMap<String, String> settings = stringStringHashMap;
                int settingsHour = Integer.parseInt(settings.get("clearEntrances"));
                if (hour == settingsHour || (hour-1  == settingsHour)) {
                    Manager.me().log("INFO","מאפס כניסות");
                    Manager.me().clearEntrances();
                }
            }
        });

    }

}
