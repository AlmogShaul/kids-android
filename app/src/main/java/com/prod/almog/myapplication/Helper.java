package com.prod.almog.myapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.telephony.SmsManager;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by shaul.almog on 21/11/2016.
 */
public class Helper {
    public HashMap<String, byte[]> kidPicsMap = new HashMap<>();
    public Kindergarden selectedKindergarden;
    private DatabaseReference databaseReference;
    public HashMap<String, byte[]> congrats = new HashMap<>();
    private StorageReference storageRef;
    private FirebaseStorage firebaseStorage;
    SmsManager smsManager = SmsManager.getDefault();
    Calendar c = Calendar.getInstance();

    private static Helper instance = null;
    private ArrayList<Kid> kids = new ArrayList<>();
    public boolean stopSMS;
    public Context context;

    public void setKids(ArrayList<Kid> kids) {
        this.kids = kids;
        Scheduler.me().start(kids);
    }

    public static Helper me() {
        if(instance == null) {
            instance = new Helper();
        }
        return instance;
    }

    private Helper() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();
        storageRef = firebaseStorage.getReferenceFromUrl("gs://kids-f5aa3.appspot.com");

        final String audioFile = "good_morning";
        getAudioFileToMap(audioFile);
//        scheduleSMS();

    }

    private void getAudioFileToMap(final String audioFile) {
        StorageReference pathReference = storageRef.child("audio/" + audioFile + ".mp3");
        final long ONE_MEGABYTE = 3024 * 3024;
        pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {

                congrats.put(audioFile, bytes);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    private void getSettings(){

        DatabaseReference kidsRef = databaseReference.child("settings");
        kidsRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        HashMap list = (HashMap) dataSnapshot.getValue();
                        if (list == null) return;
                        try {
                            Iterator it = list.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pair = (Map.Entry) it.next();
                                HashMap rawKid = (HashMap) pair.getValue();
                                stopSMS = (boolean)rawKid.get("stopSMS");
                                it.remove();
                            }
                        } catch (Exception e) {
                            e.toString();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    void scheduleSMS(){
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while(true) {
                        sleep(1000*60);
                        if(stopSMS) return;
                        for (Kid kid :kids) {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                            Date kidReminderTime = simpleDateFormat.parse(kid.reminderTime);

                            String minute = String.valueOf( c.get(Calendar.MINUTE));
                            String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
                            String time = hour + ":" + minute;
                            Date nowTime = simpleDateFormat.parse(time);
                            boolean timePassed = nowTime.compareTo(kidReminderTime) > 0 ;
                            if(!kid.arrived && timePassed && !kid.messageSent ){
                                smsManager.sendTextMessage(kid.fatherPhone, "0000000", kid.name +" לא הגיע היום לגן.", null, null);
                                kid.messageSent = true;
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }

    public byte[] getRandomCongrats() {
        Set<String> keys = Helper.me().congrats.keySet();
        return Helper.me().congrats.get(keys.toArray()[0]);
    }


    public void toast(String message) {
        Toast toast = Toast.makeText(context, message,Toast.LENGTH_LONG);
        toast.show();
    }
}

