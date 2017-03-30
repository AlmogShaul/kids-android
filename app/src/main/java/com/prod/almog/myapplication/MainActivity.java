package com.prod.almog.myapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
        Helper.me().setActivity(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Helper.me().toast("בודק");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        ListenToKindergardensRef();
        ListenToKidsRef();
        selectedKindergarden = Helper.me().selectedKindergarden;
        if(selectedKindergarden==null){
            Helper.me().restart();
        }

        else {
            TextView header = (TextView) findViewById(R.id.kindergarden_header);
            header.setText(selectedKindergarden.name);
        }
        startService(new Intent(this, ScheduleService.class));

    }
    GridView yourListView ;
    private void ListenToKidsRef() {

        yourListView = (GridView) findViewById(R.id.kids_list_view);
        customAdapter = new KidItemAdapter(getApplicationContext(), R.layout.kid_item, kids);
        yourListView.setAdapter(customAdapter);

        kidsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //no need to , the kindergarden func create new listener to get the new added child
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                updateKid(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        kidsRef.addValueEventListener(
                new ValueEventListener() {
                    boolean initialized = false;
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(initialized == false) {
                            updateKids(dataSnapshot);
                            initialized = true;
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
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
            kid.arrived = (Boolean)list.get("arrived");
            kid.absentConfirmed = (Boolean)list.get("absentConfirmed");

            if(kid.absentConfirmed == null) kid.absentConfirmed = false;
            if (kid.arrived == null) kid.arrived = false;


            for (int i=0;i<kids.size();i++){
                if(kids.get(i).id.equals(kid.id)){
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
            e.toString();
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

            storageRef = firebaseStorage.getReferenceFromUrl("gs://kids-f5aa3.appspot.com");

            for (Kid kid : kids) {
                getPicByKidsId(kid.id);
            }

            updateList();


        } catch (Exception e) {
            e.toString();
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

                Helper.me().kidPicsMap.put(copiedKidId, bytes);
                updateList();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });


    }

    private void ListenToKindergardensRef() {
        DatabaseReference kindergardensRef = databaseReference.child("kindergardens");
        kindergardensRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        kinderGardenKidsIds.clear();
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

                                    kidsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            updateKids(dataSnapshot);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
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


    private void updateKidsList(ArrayList<String> kinderGardenKidsIds) {
        for (String kidId : kinderGardenKidsIds) {

        }
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


}
