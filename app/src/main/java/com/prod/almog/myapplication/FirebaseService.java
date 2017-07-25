package com.prod.almog.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.widget.TextView;
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

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by shaul.almog on 21/11/2016.
 */
public class FirebaseService {

    DatabaseReference kidsRef;
    DatabaseReference databaseReference;
    DatabaseReference settingsRef;
    HashSet<Kindergarden> kindergardens = new HashSet<>();
    ArrayList<Kid> kids = new ArrayList<>();

    FirebaseService() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        settingsRef = databaseReference.child("settings");
        kidsRef = databaseReference.child("kids");
    }


    public void getSettings(final IResult<HashMap<String, String>> Settings) {
        settingsRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        HashMap list = (HashMap) dataSnapshot.getValue();
                        if (list == null) return;
                        HashMap<String, String> settings = new HashMap<>();
                        try {
                            Iterator it = list.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pair = (Map.Entry) it.next();
                                if (pair.getValue() instanceof Boolean) {
                                    Boolean pairValue = (Boolean) pair.getValue();
                                    settings.put((String) pair.getKey(), pairValue.toString());
                                } else {
                                    settings.put((String) pair.getKey(), (String) pair.getValue());
                                }
                                it.remove();
                            }
                        } catch (Exception e) {
                            Manager.me().log("ERROR", "שגיאה במציאת הגדרות"+e.getMessage());
                        }
                        Settings.accept(settings);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }


    private void getKindergardens(final IResult<HashSet<Kindergarden>> result) {
        databaseReference = FirebaseDatabase.getInstance().getReference();
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
                                try {
                                    Map.Entry pair = (Map.Entry) it.next();
                                    HashMap val = (HashMap) pair.getValue();
                                    Kindergarden kg = new Kindergarden();
                                    kg.id = (String) pair.getKey();
                                    kg.absenceConfirmedPhones = (String) val.get("absenceConfirmedPhones");
                                    kg.name = (String) val.get("name");
                                    kg.phone = (String) val.get("phone");
                                    kg.serial = (String) val.get("simSerialNumber");
                                    if (val.get("kids") != null) {
                                        Set<String> rawArr = ((HashMap<String, String>) val.get("kids")).keySet();
                                        kg.kidIds = new ArrayList<>(rawArr);
                                    }
                                    kindergardens.add(kg);
                                    it.remove();
                                } catch (Exception e1) {

                                }
                            }
                            result.accept(kindergardens);
                        } catch (Exception e) {
                            Manager.me().log("ERROR", "שגיאה במציאת גנים"+e.getMessage());
                        }


                        //setAutoComplete();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    int callCounter = 0;

    public void getKids(final IResult<ArrayList<Kid>> callback) {

        getKindergardens(new IResult<HashSet<Kindergarden>>() {
            @Override
            public void accept(HashSet<Kindergarden> kindergardens) {
                Kindergarden kindergarden = MatchKindergardenByPhone();
                if(kindergarden == null) {
                    Manager.me().log("ERROR", "Cant find kindergarden");
                    return;
                }


                Manager.me().setSelectedKindergarden(kindergarden);
                for (Kindergarden kg : kindergardens) {

                    if (kg.id.equals(kindergarden.id)) {
                        callCounter = kg.kidIds.size();
                        kids.clear();
                        for (String kidId : kg.kidIds) {
                            kidsRef.child(kidId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    callCounter--;
                                    updateKid(dataSnapshot);

                                    if (callCounter <= 0) {
                                        callback.accept(kids);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }


                    }

                }


            }
        });


    }


    private void updateKid(DataSnapshot dataSnapshot) {
        HashMap list = (HashMap) dataSnapshot.getValue();
        if (list == null) return;
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

            Kid kid = new Kid();
            kid.id = (String) dataSnapshot.getKey();
            kid.name = (String) list.get("name");
            kid.father = (String) list.get("father");
            kid.mother = (String) list.get("mother");
            kid.motherPhone = (String) list.get("motherPhone");
            kid.fatherPhone = (String) list.get("fatherPhone");
            kid.reminderTime = (String) list.get("reminderTime");
            if(list.get("vacationPeriodTo") != null)
            {
                try {
                    kid.vacationPeriodTo = format.parse((String)list.get("vacationPeriodTo"));
                }catch (Exception e){
                    Manager.me().log("ERROR",kid.name + ": vacationPeriodTo is not valid");
                }

            }
            if(list.get("vacationPeriodFrom") != null){
                try {
                    kid.vacationPeriodFrom = format.parse((String)list.get("vacationPeriodFrom"));
                }catch (Exception e){
                    Manager.me().log("ERROR",kid.name + ": vacationPeriodFrom is not valid");
                }
            }

            kid.arrived = (Boolean) list.get("arrived");
            kid.absentConfirmed = (Boolean) list.get("absentConfirmed");

            if (kid.absentConfirmed == null) kid.absentConfirmed = false;
            if (kid.arrived == null) kid.arrived = false;
//
//            Kid removeKid = null;
//            for (Kid _kid : kids) {
//                if (kid.id.equals(_kid.id)) {
//                    removeKid = _kid;
//                }
//            }
//            kids.remove(removeKid);
            kids.add(kid);


        } catch (Exception e) {
            Manager.me().log("ERROR", "שגיאה בעדכון הילד מהשרת"+e.getMessage());
        }

    }


    private Kindergarden MatchKindergardenByPhone() {

        String serial = Manager.me().getDevicePhoneNumber();
        for (Kindergarden kg : kindergardens) {
            if(kg.name.equals("גן ניסיון")){
                if(serial == null) {
                    return kg;
                }
            }else if ((kg.serial != null) && kg.serial.equals(serial)) {
                return kg;
            }
        }
        return null;
    }


}

