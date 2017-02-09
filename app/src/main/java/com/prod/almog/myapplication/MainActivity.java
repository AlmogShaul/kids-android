package com.prod.almog.myapplication;

import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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

    public MainActivity() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();
        Helper.me().setActivity(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListenToKindergardensRef();
        selectedKindergarden = Helper.me().selectedKindergarden;
        Helper.me().context = this.getBaseContext();
        TextView header = (TextView) findViewById(R.id.kindergarden_header);
        header.setText(selectedKindergarden.name);

    }

    private void ListenToKidsRef() {
        DatabaseReference kidsRef = databaseReference.child("kids");
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
                                Kid kid = createKid(pair);
                                if (kinderGardenKidsIds.contains(kid.id)) {
                                    kids.add(kid);
                                }
                                it.remove();
                            }

                            // Create a storage reference from our app
                            storageRef = firebaseStorage.getReferenceFromUrl("gs://kids-f5aa3.appspot.com");

                            for (Kid kid : kids) {
                                getPicByKidsId(kid.id);
                            }


                        } catch (Exception e) {
                            e.toString();
                        }
                        Helper.me().setKids(kids);
                        GridView yourListView = (GridView) findViewById(R.id.kids_list_view);
                        customAdapter = new KidItemAdapter(getApplicationContext(), R.layout.kid_item, kids);
                        yourListView.setAdapter(customAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void getPicByKidsId(final String kidId) {

        // Create a reference with an initial file path and name
        StorageReference pathReference = storageRef.child(kidId + ".png");
        final long ONE_MEGABYTE = 3024 * 3024;
        pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {

                Helper.me().kidPicsMap.put(kidId, bytes);
                customAdapter.notifyDataSetChanged();

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

                        GridView yourListView = (GridView) findViewById(R.id.kids_list_view);
                        KidItemAdapter customAdapter = new KidItemAdapter(getApplicationContext(), R.layout.kid_item, kids);
                        yourListView.setAdapter(customAdapter);
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
        if (kid.arrived == null) kid.arrived = false;
        return kid;
    }


}
