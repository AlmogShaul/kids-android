package com.prod.almog.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KidItemAdapter extends ArrayAdapter<Kid> {

    List<Kid> kids = new ArrayList<>();
    public KidItemAdapter(Context context, int resource, List<Kid> items) {
        super(context, resource, items);
        kids = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.kid_item, null);
        }

        Kid kid = getItem(position);

        if (kid != null) {
            TextView name_text = (TextView) v.findViewById(R.id.name);
            ImageView image_view = (ImageView)v.findViewById(R.id.pic);
            AppCompatButton arrived_check = (AppCompatButton) v.findViewById(R.id.arrived);
            SetToggleButtonColor(arrived_check, position);

            arrived_check.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    View parentRow = (View)view.getParent();
                    GridView gridView = (GridView) parentRow.getParent();
                    final int position = gridView.getPositionForView(parentRow);
                    kids.get(position).arrived =  !kids.get(position).arrived;
                    SetToggleButtonColor(view, position);
                    updateServer(kids.get(position));
                    play(Helper.me().getRandomCongrats());
                }

            });

                name_text.setText(kid.name);

            byte[] pic = Helper.me().kidPicsMap.get(kid.id);
                if((pic != null) && (pic.length != 0)) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(pic, 0, pic.length);
                    image_view.setImageBitmap(bmp);
                }

        }

        return v;
    }

    private void updateServer(Kid kid) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference kidsRef = databaseReference.child("kids");
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(kid.id ,kid);
        Task<Void> task = kidsRef.updateChildren(childUpdates);
    }

    private void SetToggleButtonColor(View view, int position) {
        if((kids.get(position).arrived != null) && (kids.get(position).arrived  == true))
            view.setBackgroundColor(Color.GREEN);
        else
            view.setBackgroundColor(Color.LTGRAY);
    }



    private MediaPlayer mediaPlayer = new MediaPlayer();

    private void play(byte[] mp3SoundByteArray) {
        try {
            // create temp file that will hold byte array
            File tempMp3 = File.createTempFile("temp_kids_audio", "mp3", getContext().getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();

            // resetting mediaplayer instance to evade problems
            mediaPlayer.reset();

            // In case you run into issues with threading consider new instance like:
            // MediaPlayer mediaPlayer = new MediaPlayer();

            // Tried passing path directly, but kept getting
            // "Prepare failed.: status=0x1"
            // so using file descriptor instead
            FileInputStream fis = new FileInputStream(tempMp3);
            mediaPlayer.setDataSource(fis.getFD());

            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }

}