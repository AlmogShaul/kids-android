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
import com.google.android.gms.tasks.Task;
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
    public ArrayList<String> stopSendingSMSNumbers = new ArrayList<>();
    public Alarm alarm;


    public void setActivity(Activity _activity) {
        activity = _activity;
    }

    private static Helper instance = null;
    private ArrayList<Kid> kids = new ArrayList<>();
    public Context context;

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
            DebugScheduler.me().clearWorkers();
            DebugScheduler.me().startSchedule();
        } else {
            //TODO: need to fix the read scheduler
            Scheduler.me().start(kids, holidays);

        }

    }

    public static Helper me() {
        if (instance == null) {
            instance = new Helper();
        }
        return instance;
    }

    private Helper() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();

        storageRef = firebaseStorage.getReferenceFromUrl("gs://kids-f5aa3.appspot.com");

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

    public HashMap<String, String> settings = new HashMap<>();



    private void decideIfRestart() {
        String restart = settings.get("restart");
        if (restart != null && restart.equals("true")) {
            restart();

        }
    }

    public void restart() {
        DebugScheduler.me().clearWorkers();
        if (context != null) {
            Intent i = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(i);
        }
    }


    private void mapCongratFiles() {
        String congratsFilesStr = settings.get("congratsFiles");
        String[] congratsFiles;
        if (congratsFilesStr != null) {
            congratsFiles = congratsFilesStr.split(";");
            for (String file : congratsFiles) {
                getAudioFileToMap(file);
            }
        }
    }

    public byte[] getRandomCongrat() {
        Set<String> keys = Helper.me().congrats.keySet();
        if (keys != null && keys.size() > 0) {
            Integer randomAccessNumber = new Random().nextInt(keys.size());
            return Helper.me().congrats.get(keys.toArray()[randomAccessNumber]);
        } else {
            return null;
        }
    }


    public void toast(final String message) {

        if(activity!=null){
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                }
            });
        }


    }


    public void updateServer(Kid kid) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference kidsRef = databaseReference.child("kids").child(kid.id);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("arrived", kid.arrived);
        childUpdates.put("absentConfirmed",kid.absentConfirmed);
        kidsRef.updateChildren(childUpdates);

    }

    public void clearEntrances() {

        DebugScheduler.me().clearEntrances();
    }

    public void startSchedule() {
        DebugScheduler.me().startSchedule();
    }

    public String getShortPhoneNum(String senderNum) {
        Integer rawLength = senderNum.length();
        return "0" + senderNum.substring(rawLength - 9);
    }

    public void clearStopSendingList() {
        stopSendingSMSNumbers.clear();
    }

    public void confirmAbsent(String shortPhoneNum) {
        changeConformStatus(shortPhoneNum,true);

    }

    private void changeConformStatus(String shortPhoneNum,Boolean status) {
        for (Kid kid:kids) {
            if((kid.fatherPhone.equals(shortPhoneNum)) || (kid.fatherPhone.equals(shortPhoneNum)))
            {
                kid.absentConfirmed = status;
                updateServer(kid);
            }
        }
    }

    public void clearConfirmAbsent(String shortPhoneNum) {
        changeConformStatus(shortPhoneNum,false);

    }
}

