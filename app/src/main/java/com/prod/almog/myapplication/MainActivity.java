package com.prod.almog.myapplication;

import android.content.ContentResolver;
import android.content.Intent;
import android.media.MediaPlayer;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;


public class MainActivity extends AppCompatActivity {
    StorageReference storageRef;
    FirebaseStorage firebaseStorage;
    private DatabaseReference databaseReference;
    private String reminderTime = "09:00";
    private ArrayList<Kid> kids = new ArrayList<>();
    Kindergarden selectedKindergarden;
    ArrayList<String> kinderGardenKidsIds = new ArrayList<>();
    KidItemAdapter customAdapter;
    DatabaseReference kidsRef;

    public MainActivity() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();
        kidsRef = databaseReference.child("kids");
        Manager.me().setActivity(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Manager.me().setContext(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        getKids();
        scheduleUpdate();
        scheduleScreenMode();
        selectedKindergarden = Manager.me().getSelectedKindergarden();
        if (selectedKindergarden == null) {
            Manager.me().log("INFO", "מאתחל לאחר שלא נמצא גן ילדים");
            Manager.me().restart();
        } else {
            Manager.me().log("INFO",selectedKindergarden.name + " למעלה ");
            TextView header = (TextView) findViewById(R.id.kindergarden_header);
            header.setText(selectedKindergarden.name);
        }


    }

    private void scheduleUpdate() {
        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                getKids();
            }
        }, 0, 1, TimeUnit.MINUTES);

    }

    private void scheduleScreenMode() {
        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                FirebaseService firebaseService = new FirebaseService();
                firebaseService.getSettings(new IResult<HashMap<String, String>>() {
                    @Override
                    public void accept(HashMap<String, String> stringStringHashMap) {
                        ArrayList<Date> holidays = new ArrayList<>();
                        HashMap<String, String> settings = stringStringHashMap;
                        String _holidays = settings.get("holidays");
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                        if (_holidays != null) {
                            String[] arr = _holidays.split(";");
                            for (String item : arr) {
                                try {
                                    holidays.add(formatter.parse(item));
                                } catch (ParseException e) {
                                    Manager.me().log("ERROR", "שגיאה בניתוח יום חופשה" + e.getMessage());
                                }
                            }
                        }
                        String darkScreen = Manager.me().settings.get("darkScreen");
                        if ((isHoliday(holidays) || Manager.me().passesWorkingHours()) && darkScreen.equals("true")) {
                                Manager.me().darkScreen();
                        }else{
                            Manager.me().lightScreen();
                        }
                    }
                });
            }
        }, 0, 1, TimeUnit.HOURS);

    }

    private boolean isHoliday(ArrayList<Date> holidays) {

        try {
            boolean sameDay = false;
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(new Date());
            if (cal1.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
                return true;
            else {
                for (Date date : holidays) {
                    cal2.setTime(date);
                    sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
                }
                return sameDay;
            }
        }
        catch (Exception e){
            return  false;
        }
    }

    GridView yourListView;

    private void UpdateKidsViewAdapter() {

        yourListView = (GridView) findViewById(R.id.kids_list_view);
        customAdapter = new KidItemAdapter(getApplicationContext(), R.layout.kid_item, kids);
        yourListView.setAdapter(customAdapter);
        updateList();

    }

    private void updateKid(DataSnapshot dataSnapshot) {
        HashMap list = (HashMap) dataSnapshot.getValue();
        if (list == null) return;
        try {
            Kid kid = new Kid();
            kid.id = (String) dataSnapshot.getKey();
            kid.name = (String) list.get("name");
            kid.father = (String) list.get("father");
            kid.mother = (String) list.get("mother");
            kid.motherPhone = (String) list.get("motherPhone");
            kid.fatherPhone = (String) list.get("fatherPhone");
            kid.reminderTime = (String) list.get("reminderTime");
            kid.arrived = (Boolean) list.get("arrived");
            kid.absentConfirmed = (Boolean) list.get("absentConfirmed");

            if (kid.absentConfirmed == null) kid.absentConfirmed = false;
            if (kid.arrived == null) kid.arrived = false;


            for (int i = 0; i < kids.size(); i++) {
                if (kids.get(i).id.equals(kid.id)) {
                    kids.remove(i);
                }
            }
            kids.add(kid);

            storageRef = firebaseStorage.getReferenceFromUrl("gs://kids-f5aa3.appspot.com");

            for (Kid _kid : kids) {
                getPicByKidsId(_kid.id);
            }

            updateList();


        } catch (Exception e) {
        }

    }

    private void updateList() {
        customAdapter.sort(new Comparator<Kid>() {
            @Override
            public int compare(Kid lhs, Kid rhs) {
                return lhs.name.compareTo(rhs.name);   //or whatever your sorting algorithm
            }
        });
        customAdapter.notifyDataSetChanged();
    }

    private void updateKids(DataSnapshot dataSnapshot) {
        HashMap list = (HashMap) dataSnapshot.getValue();
        if (list == null) return;
        kids.clear();
        try {
            Iterator it = list.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                Kid kid = createKid(pair);
                if (kinderGardenKidsIds.contains(kid.id)) {
                    kids.add(kid);
                }
                it.remove();
            }

            updateList();


        } catch (Exception e) {
        }
    }


    private void getPicByKidsId(final String kidId) {

        // Create a reference with an initial file path and name
        StorageReference pathReference = storageRef.child(kidId + ".png");
        final long ONE_MEGABYTE = 3024 * 3024;
        pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {

            String copiedKidId = kidId;

            @Override
            public void onSuccess(byte[] bytes) {
                Manager.me().kidPicsMap.put(copiedKidId, bytes);
                customAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });


    }

    @NonNull
    private Kid createKid(Map.Entry pair) {
        Kid kid = new Kid();
        HashMap rawKid = (HashMap) pair.getValue();
        kid.id = (String) pair.getKey();
        kid.name = (String) rawKid.get("name");
        kid.father = (String) rawKid.get("father");
        kid.mother = (String) rawKid.get("mother");
        kid.motherPhone = (String) rawKid.get("motherPhone");
        kid.fatherPhone = (String) rawKid.get("fatherPhone");
        kid.reminderTime = (String) rawKid.get("reminderTime");
        kid.arrived = (Boolean) rawKid.get("arrived");
        kid.absentConfirmed = (Boolean) rawKid.get("absentConfirmed");
        if (kid.absentConfirmed == null) kid.absentConfirmed = false;
        if (kid.arrived == null) kid.arrived = false;
        return kid;
    }


    public void getKids() {

        FirebaseService fb = new FirebaseService();
        fb.getKids(new IResult<ArrayList<Kid>>() {
            @Override
            public void accept(ArrayList<Kid> _kids) {

                kids = _kids;
                selectedKindergarden = Manager.me().getSelectedKindergarden();
                TextView header = (TextView) findViewById(R.id.kindergarden_header);
                header.setText(selectedKindergarden.name);

                storageRef = firebaseStorage.getReferenceFromUrl("gs://kids-f5aa3.appspot.com");

                if (dayPassed()) {
                    Manager.me().log("INFO", "מעדכן תמונות ילדים");
                    for (Kid kid : kids) {
                        getPicByKidsId(kid.id);
                    }
                }

                UpdateKidsViewAdapter();
            }
        });
    }

//    private Integer lastRecHour = 0;
//    private boolean hourPassed() {
//        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
//        if (hour != lastRecHour) {
//            lastRecHour = hour;
//            return true;
//        } else {
//            return false;
//        }
//    }

    private boolean dayPassed() {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        if (day != Manager.me().lastRecDay)
        {
            Manager.me().lastRecDay = day;
            return true;
        }
        else
        {
            return false;
        }
    }
}
