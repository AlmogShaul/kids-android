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
import android.widget.Toast;

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

    private String getDevicePhoneNumber() {
        TelephonyManager tMgr = (TelephonyManager)this.getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        String serialNumber = tMgr.getSimSerialNumber();
        return serialNumber;
    }


}
