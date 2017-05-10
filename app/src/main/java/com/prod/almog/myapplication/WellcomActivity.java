package com.prod.almog.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WellcomActivity extends AppCompatActivity {


    private DatabaseReference databaseReference;
    private List<Kindergarden> kindergardens = new ArrayList<Kindergarden>();

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        super.onWindowFocusChanged(hasFocus);

        if(hasFocus)
            init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        setContentView(R.layout.activity_wellcom);
        startService(new Intent(this, ScheduleService.class));
        startService(new Intent(this, KidsNotifierService.class));

        Manager.me().context = this.getBaseContext();
        FirebaseService fb = new FirebaseService();
        fb.getKids(new IResult<ArrayList<Kid>>() {
            @Override
            public void accept(ArrayList<Kid> kids) {

                if(Manager.me().getSelectedKindergarden() == null) {
                    TextView textView = (TextView) findViewById(R.id.tv_match);
                    textView.setText("לא נמצאה התאמה");
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                }

                Manager.me().setKids(kids);
            }
        });
    }


    private void getKindergardens(){
        databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference kindergardensRef = databaseReference.child("kindergardens");
        kindergardensRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        HashMap list = (HashMap) dataSnapshot.getValue();
                        if(list == null) return;
                        try {
                            Iterator it = list.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pair = (Map.Entry) it.next();
                                HashMap val = (HashMap)pair.getValue();
                                Kindergarden kg = new Kindergarden();
                                kg.id = (String)pair.getKey();
                                kg.name = (String)val.get("name");
                                kg.phone = (String)val.get("phone");
                                kg.serial = (String)val.get("simSerialNumber");
                                kindergardens.add(kg);
                                it.remove();
                            }
                            MatchKindergardenByPhone();

                        }
                        catch(Exception e){
                        }


                        //setAutoComplete();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }

    private void setAutoComplete() {
//        AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.kg_auto_complete);
//        KindergardenItemAdapter adapter = new KindergardenItemAdapter(getApplicationContext(), R.layout.kindergarden_item,kindergardens);
//        actv.setAdapter(adapter);
//        actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                TextView tv = (TextView) view;
//                String kindergardenName = tv.getText().toString();
//                for (Kindergarden kg :kindergardens) {
//                    if(kg.toString().equals(kindergardenName)) {
//                        Manager.me().selectedKindergarden = kg;
//                    }
//
//                }
//                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                getApplicationContext().startActivity(intent);
//            }
//        });
    }

    private void MatchKindergardenByPhone() {
        String serial = getDevicePhoneNumber();
        for (Kindergarden kg :kindergardens) {
            if((kg.serial!=null)&&kg.serial.equals(serial)) {
                Manager.me().setSelectedKindergarden(kg);
            }
        }
        if(Manager.me().getSelectedKindergarden() == null) {
            TextView textView = (TextView) findViewById(R.id.tv_match);
            textView.setText("לא נמצאה התאמה");
        }
        else {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
        }
    }

    private String getDevicePhoneNumber() {
        TelephonyManager tMgr = (TelephonyManager)this.getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        String serialNumber = tMgr.getSimSerialNumber();
        return serialNumber;
    }


}
