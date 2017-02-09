package com.prod.almog.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import java.util.Random;
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
    Calendar c = Calendar.getInstance();
    private Activity activity;
    public void setActivity(Activity _activity){
        activity = _activity;
    }
    private static Helper instance = null;
    private ArrayList<Kid> kids = new ArrayList<>();
    public Context context;
    DatabaseReference settingsRef;

    public void setKids(ArrayList<Kid> kids) {
        this.kids = kids;
        String debugMode = Helper.me().settings.get("debugMode");

        ArrayList<Date> holidays = new ArrayList<>();
        String _holidays = settings.get("holidays");
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        if (_holidays != null) {
            String[] arr = _holidays.split(";");
            for (String item : arr) {
                try {
                    holidays.add(formatter.parse(item));
                } catch (ParseException e) {
                }
            }
        }
        if (debugMode != null && debugMode.toLowerCase().equals("true")) {
            DebugScheduler.me().start(kids, holidays);
        } else {
            Scheduler.me().start(kids, holidays);
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
        settingsRef = databaseReference.child("settings");

        storageRef = firebaseStorage.getReferenceFromUrl("gs://kids-f5aa3.appspot.com");
        getSettings();
    }

    private void getAudioFileToMap(final String audioFile) {
        StorageReference pathReference = storageRef.child("audio/" + audioFile);
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

        settingsRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        HashMap list = (HashMap) dataSnapshot.getValue();
                        if (list == null) return;
                        try {
                            Iterator it = list.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pair = (Map.Entry) it.next();
                                if(pair.getValue() instanceof Boolean)
                                {
                                    Boolean pairValue = (Boolean) pair.getValue();
                                    settings.put((String)pair.getKey(),pairValue.toString());
                                }
                                else{
                                    settings.put((String)pair.getKey(),(String)pair.getValue());
                                }
                                it.remove();
                            }

                        } catch (Exception e) {
                            e.toString();
                        }
                        mapCongratFiles();
                        decideIfRestart();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void decideIfRestart() {
        String restart =settings.get("restart");
        if(restart!=null && restart.equals("true"))
        {
            DebugScheduler.me().clearWorkers();
            if(context!=null){
                Intent i = context.getPackageManager()
                        .getLaunchIntentForPackage( context.getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(i);
            }

        }
    }


    private void mapCongratFiles() {
        String congratsFilesStr = settings.get("congratsFiles");
        String[] congratsFiles;
        if(congratsFilesStr!=null){
            congratsFiles = congratsFilesStr.split(";");
            for (String file : congratsFiles) {
                getAudioFileToMap(file);
            }
        }
    }

    public byte[] getRandomCongrat() {
        Set<String> keys = Helper.me().congrats.keySet();
        if(keys !=null && keys.size() > 0) {
            Integer randomAccessNumber = new Random().nextInt(keys.size());
            return Helper.me().congrats.get(keys.toArray()[randomAccessNumber]);
        }
        else {
            return null;
        }
    }


    public void toast(final String message) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        });

    }
}

