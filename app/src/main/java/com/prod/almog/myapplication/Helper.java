package com.prod.almog.myapplication;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
    private Activity activity;
    public void setActivity(Activity _activity){
        activity = _activity;
    }
    private static Helper instance = null;
    private ArrayList<Kid> kids = new ArrayList<>();
    public boolean stopSMS;
    public Context context;

    public void setKids(ArrayList<Kid> kids) {
        this.kids = kids;
        String debugMode = Helper.me().settings.get("debugMode");
        if(debugMode!= null && debugMode.equals("true"))
        {
            DebugScheduler.me().start(kids);
        }
        else{
            Scheduler.me().start(kids);
        }

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

    public HashMap<String,String> settings = new HashMap<>();
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
                                settings.put("stopSMS",(String)rawKid.get("stopSMS"));
                                settings.put("debugMode",(String)rawKid.get("debugMode"));
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

    public byte[] getRandomCongrats() {
        Set<String> keys = Helper.me().congrats.keySet();
        return Helper.me().congrats.get(keys.toArray()[0]);
    }


    public void toast(final String message) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        });

    }
}

