package com.prod.almog.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.GridView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ScheduleService extends Service {

    private DatabaseReference databaseReference;
    FirebaseStorage firebaseStorage;
    DatabaseReference kidsRef;
    private ArrayList<Kid> kids = new ArrayList<>();
    ArrayList<String> kinderGardenKidsIds = new ArrayList<>();
    StorageReference storageRef;
    private Kindergarden selectedKindergarden;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        databaseReference = FirebaseDatabase.getInstance().getReference();

        ListenToKindergardensRef();

        if(Helper.me().alarm==null) {
            Helper.me().alarm = new Alarm();
            Helper.me().alarm.setAlarm(this);
        }
        return START_STICKY;
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
        kid.arrived = (Boolean)rawKid.get("arrived");
        kid.absentConfirmed = (Boolean)rawKid.get("absentConfirmed");
        if(kid.absentConfirmed == null) kid.absentConfirmed = false;
        if (kid.arrived == null) kid.arrived = false;
        return kid;
    }

    private void ListenToKidsRef() {

        firebaseStorage = FirebaseStorage.getInstance();
        kidsRef = databaseReference.child("kids");

        kidsRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
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


                        } catch (Exception e) {
                            e.toString();
                        }
                        Helper.me().setKids(kids);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void ListenToKindergardensRef() {
        selectedKindergarden = Helper.me().selectedKindergarden;

        DatabaseReference kindergardensRef = databaseReference.child("kindergardens");
        kindergardensRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        HashMap list = (HashMap) dataSnapshot.getValue();
                        if (list == null) return;
                        try {
                            Iterator it = list.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pair = (Map.Entry) it.next();
                                HashMap val = (HashMap) pair.getValue();

                                if (val.get("name").equals(selectedKindergarden.name)) {
                                    HashMap rawKids = (HashMap) val.get("kids");
                                    Iterator _it = rawKids.entrySet().iterator();
                                    while (_it.hasNext()) {
                                        Map.Entry _pair = (Map.Entry) _it.next();
                                        String kidKey = (String) _pair.getKey();
                                        boolean kidVal = (boolean) _pair.getValue();
                                        if (kidVal == true) kinderGardenKidsIds.add(kidKey);
                                        _it.remove();
                                    }

                                    ListenToKidsRef();
                                }
                                it.remove();
                            }
                        } catch (Exception e) {
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }



}
