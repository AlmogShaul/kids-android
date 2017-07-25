package com.prod.almog.babysave;

import android.content.Context;
import android.content.res.Resources;
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
import java.util.Comparator;
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
                    Manager.me().updateServer(kids.get(position));
                    if(kids.get(position).arrived == true)
                    {
                        byte[] congrat = Manager.me().getRandomCongrat();
                        if(congrat !=null)
                        play(congrat);
                    }
                }

            });

                name_text.setText(kid.name);

            byte[] pic = Manager.me().kidPicsMap.get(kid.id);
                if((pic != null) && (pic.length != 0)) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(pic, 0, pic.length);
                    Bitmap resized = Bitmap.createScaledBitmap(bmp, 200, 200, true);
                    image_view.setImageBitmap(resized);
                }
            else{
                    Drawable face = this.getContext().getResources().getDrawable(R.drawable.kid_face_light,null);
                    image_view.setImageDrawable(face);
//                    image_view.setImageBitmap(Drawable.createFromResourceStream(R.drawable.kid_face));

                }

        }

        return v;
    }



    private void SetToggleButtonColor(View view, int position) {
        if((kids.get(position).arrived != null) && (kids.get(position).arrived  == true))
            view.setBackgroundColor(Color.GREEN);
        else {

            if(kids.get(position).absentConfirmed == true)
            {
                view.setBackgroundColor(Color.YELLOW);
            }
            else{
                view.setBackgroundColor(Color.LTGRAY);
            }

        }
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
            mediaPlayer.reset();
            FileInputStream fis = new FileInputStream(tempMp3);
            mediaPlayer.setDataSource(fis.getFD());

            mediaPlayer.prepare();
            mediaPlayer.start();

            //TODO:(shaul) check if needed
//            FileOutputStream fos = new FileOutputStream(tempMp3);
//            fos.write(mp3SoundByteArray);
//            fos.close();

        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }

}