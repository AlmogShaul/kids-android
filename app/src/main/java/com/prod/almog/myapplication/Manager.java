package com.prod.almog.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by shaul.almog on 30/03/2017.
 */

public class Manager {

    public HashMap<String, byte[]> kidPicsMap = new HashMap<>();
    private DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageRef;
    public HashMap<String, byte[]> congrats = new HashMap<>();

    Context context;
    private Kindergarden selectedKindergarden;
    private ArrayList<Kid> kids;
    public HashMap<String, String> settings = new HashMap<>();
    private Activity activity;
    public ArrayList<String> stopSendingSMSNumbers = new ArrayList<>();
    DatabaseReference logsRef;

    long id = 0;

    private Manager() {
        id = Calendar.getInstance().getTimeInMillis();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();
        storageRef = firebaseStorage.getReferenceFromUrl("gs://kids-f5aa3.appspot.com");
        logsRef = databaseReference.child("logs");
        getSettingsFromServer();
        scheduleUpdateSettings();
    }

    private void scheduleUpdateSettings() {
        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                getSettingsFromServer();
            }
        }, 0, 2, TimeUnit.MINUTES);
    }

    private void getSettingsFromServer() {
        FirebaseService fb = new FirebaseService();
        fb.getSettings(new IResult<HashMap<String, String>>() {
            @Override
            public void accept(HashMap<String, String> _settings) {
                settings = _settings;
                mapCongratFiles();

            }
        });
    }

    public void setContext(Context _context) {
        context = _context;
    }

    private static Manager me;

    public static Manager me() {
        if (me == null) {
            me = new Manager();
        }
        return me;
    }

    public void sendSMS(String message, String target) {
        SmsManager smsManager = SmsManager.getDefault();
        try {
            smsManager.sendTextMessage(target, null, message, null, null);
        } catch (Exception e) {
        }
    }

    public String getDevicePhoneNumber() {
        TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String serialNumber = tMgr.getSimSerialNumber();
        return serialNumber;
    }

    public void setSelectedKindergarden(Kindergarden kg) {
        selectedKindergarden = kg;
    }

    public Kindergarden getSelectedKindergarden() {
        return selectedKindergarden;
    }

    public void setKids(ArrayList<Kid> kids) {
        this.kids = kids;
    }

    public ArrayList<Kid> getKids() {
        return this.kids;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }


    public void restart() {
//        DebugScheduler.me().clearWorkers();
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


    public byte[] getRandomCongrat() {
        Set<String> keys = congrats.keySet();
        if (keys != null && keys.size() > 0) {
            Integer randomAccessNumber = new Random().nextInt(keys.size());
            return congrats.get(keys.toArray()[randomAccessNumber]);
        } else {
            return null;
        }
    }


    public void toast(final String message) {

        if (activity != null) {
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
        childUpdates.put("absentConfirmed", kid.absentConfirmed);
        kidsRef.updateChildren(childUpdates);


    }

    public void clearEntrances() {

        if (kids == null) return;

        for (Kid kid : kids) {
            kid.arrived = false;
            kid.absentConfirmed = false;
            Manager.me().updateServer(kid);
        }
    }

    public void startSchedule() {
//        DebugScheduler.me().startSchedule();
    }

    public String getShortPhoneNum(String senderNum) {
        Integer rawLength = senderNum.length();
        return "0" + senderNum.substring(rawLength - 9);
    }

//    public void clearStopSendingList() {
//        stopSendingSMSNumbers.clear();
//    }

    public void confirmAbsent(String shortPhoneNum) {
        changeConfirmStatus(shortPhoneNum, true);
    }

    private void changeConfirmStatus(String shortPhoneNum, Boolean status) {
        for (Kid kid : kids) {
            if ((kid.motherPhone.equals(shortPhoneNum)) || (kid.fatherPhone.equals(shortPhoneNum))) {
                kid.absentConfirmed = status;
                updateServer(kid);
            }
        }
    }

    private void decideIfRestart() {
        String restart = settings.get("restart");
        if (restart != null && restart.equals("true")) {
            restart();

        }
    }

    public void log(String seveirity, String message) {
        String kindergarden = "";
        if (selectedKindergarden != null)
            kindergarden = selectedKindergarden.name;
        else
            kindergarden = getDevicePhoneNumber();

        String time = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-" +
                (Calendar.getInstance().get(Calendar.MONTH)+1) + "  " +
                Calendar.getInstance().get(Calendar.HOUR) +":" +
                Calendar.getInstance().get(Calendar.MINUTE) +":" +
                Calendar.getInstance().get(Calendar.SECOND) +":" +
                Calendar.getInstance().get(Calendar.MILLISECOND);
        logsRef.child(time).setValue(kindergarden + ":" + seveirity + ":" + message);
    }

    public void clearConfirmAbsent(String shortPhoneNum) {
        changeConfirmStatus(shortPhoneNum, false);

    }
}
