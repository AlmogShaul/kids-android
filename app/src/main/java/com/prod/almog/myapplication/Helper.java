//package com.prod.almog.myapplication;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Handler;
//import android.os.Looper;
//import android.os.Message;
//import android.support.annotation.NonNull;
//import android.telephony.SmsManager;
//import android.widget.GridView;
//import android.widget.Toast;
//
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//import java.util.Set;
//
///**
// * Created by shaul.almog on 21/11/2016.
// */
//public class Helper {
//    public HashMap<String, byte[]> kidPicsMap = new HashMap<>();
//    public Kindergarden selectedKindergarden;
//    private DatabaseReference databaseReference;
//    public HashMap<String, byte[]> congrats = new HashMap<>();
//    private StorageReference storageRef;
//    private FirebaseStorage firebaseStorage;
//    Calendar c = Calendar.getInstance();
//    private Activity activity;
//    public ArrayList<String> stopSendingSMSNumbers = new ArrayList<>();
//
//
//    public void setActivity(Activity _activity) {
//        activity = _activity;
//    }
//
//    private static Helper instance = null;
//    private ArrayList<Kid> kids = new ArrayList<>();
//    public Context context;
//
//    public void setKids(ArrayList<Kid> kids) {
//        this.kids = kids;
//        String debugMode = Manager.me().settings.get("debugMode");
//        ArrayList<Date> holidays = new ArrayList<>();
//        String _holidays = settings.get("holidays");
//        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
//        if (_holidays != null) {
//            String[] arr = _holidays.split(";");
//            for (String item : arr) {
//                try {
//                    holidays.add(formatter.parse(item));
//                } catch (ParseException e) {
//                }
//            }
//        }
//        if (debugMode != null && debugMode.toLowerCase().equals("true")) {
//            DebugScheduler.me().start(kids, holidays);
//            DebugScheduler.me().clearWorkers();
//            DebugScheduler.me().startSchedule();
//        } else {
//            //TODO: need to fix the read scheduler
//            Scheduler.me().start(kids, holidays);
//
//        }
//
//    }
//
//    public static Helper me() {
//        if (instance == null) {
//            instance = new Helper();
//        }
//        return instance;
//    }
//
//    private Helper() {
//        databaseReference = FirebaseDatabase.getInstance().getReference();
//        firebaseStorage = FirebaseStorage.getInstance();
//        FirebaseService fb = new FirebaseService();
//        fb.getSettings(new IResult<HashMap<String, String>>() {
//            @Override
//            public void accept(HashMap<String, String> _settings) {
//                settings = _settings;
//                mapCongratFiles();
//
//            }
//        });
//        storageRef = firebaseStorage.getReferenceFromUrl("gs://kids-f5aa3.appspot.com");
//
//    }
//
//
//
//}
//
