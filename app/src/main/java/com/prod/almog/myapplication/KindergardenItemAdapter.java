package com.prod.almog.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.preference.PreferenceActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KindergardenItemAdapter extends ArrayAdapter<Kindergarden> {

    List<Kindergarden> kindergardens = new ArrayList<>();
    Context localContext;
    public KindergardenItemAdapter(Context context, int resource, List<Kindergarden> items) {
        super(context, resource, items);
        localContext = context;
        kindergardens = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.kindergarden_item, null);
            TextView name_text = (TextView) v.findViewById(R.id.kindergarden_name);
            final Kindergarden kindergarden = getItem(position);
            name_text.setText(kindergarden.name);

        }


        return v;
    }




}