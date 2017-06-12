package com.prod.almog.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.GridView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ScheduleService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        setResetEntrancesReceiver();
    }



    private void setResetEntrancesReceiver() {
        setAlarm(this);
    }


    AlarmManager am;

    public void setAlarm(Context context) {
        if (am == null) {
            Toast.makeText(context, "מאתחל תזמון", Toast.LENGTH_SHORT).show();
            Calendar calendar = Calendar.getInstance();
            am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(context, ResetEntrancesReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + 1000, 1000 * 60 * 60, pi); // Millisec * Second * Minute
        }
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, ResetEntrancesReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

}
