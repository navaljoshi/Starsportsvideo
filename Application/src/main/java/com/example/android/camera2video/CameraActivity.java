/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.camera2video;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.VideoView;

import java.io.File;
import java.io.FileWriter;


public class CameraActivity extends Activity {

    EditText name,email;
    Dialog dialog = null,dialog1 = null;
    Button button = null;
    Context context;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        dialog = new Dialog(this);
       // dialog1 = new Dialog(this);
        dialog.setContentView(R.layout.formdialog);
        dialog.setTitle("Enter Contact Details");
        dialog.setCanceledOnTouchOutside(false);


        name = (EditText)dialog.findViewById(R.id.editTextName);
        email = (EditText)dialog.findViewById(R.id.editTextMail);

        button = (Button) dialog.findViewById(R.id.buttonSend);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateNoteOnSD(getApplicationContext(),null,null); //saving the number

                       // calling Front camera
                setContentView(R.layout.activity_camera);
                dialog.cancel();
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2VideoFragment.newInstance())
                    .commit();


            }
        });dialog.show();





    }

    public void generateNoteOnSD(Context context, String sFileName, String sBody) {
        try {


            // rateDisplay.setText("Rate: " + ratingValue);
            // Toast.makeText(getApplicationContext(), "Rate: " + ratingValue, Toast.LENGTH_LONG).show();

            File gpxfile = new File(Environment.getExternalStorageDirectory(), "star.txt");

            Log.d("star","path of file :" +gpxfile.toString());
            Log.d("star","Name:" +name.getText().toString());
            Log.d("star","Email:" +email.getText().toString());
            try {
                FileWriter writer = new FileWriter(gpxfile,true);
                writer.append("\n Name :" +name.getText().toString() + " Star rating :" +email.getText().toString());
                writer.flush();
                writer.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            name.setText("");
            email.setText("");

        }finally {

        }
    }

    public void openVideoDialog()
    {

        Log.d("LVMH", " opening Video dialog ");

        dialog1 = new Dialog(CameraActivity.this);
        dialog1.setContentView(R.layout.video);
//        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog1.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        dialog1.setCanceledOnTouchOutside(true);
        dialog1.setCancelable(true);

        //calling mail send button inside popup
        final VideoView vid = (VideoView) dialog1.findViewById(R.id.video1);
        final Button play = (Button)dialog1.findViewById(R.id.videoPlay);
        final Button retake = (Button)dialog1.findViewById(R.id.retake);
        final Button share = (Button)dialog1.findViewById(R.id.share);

        Log.d("LVMH", "play video button clicked with path :"+Camera2VideoFragment.imagePath);
        Uri uri = Uri.parse(Camera2VideoFragment.imagePath);
        vid.setVideoURI(uri);
        vid.start();

        dialog1.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {

                        dialog1.dismiss();
                        Log.d("LVMH", " cancelled Video dialog ");
                    }
                }
        );


        play.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                Log.d("LVMH", "play video button clicked with path :"+Camera2VideoFragment.imagePath);
                Uri uri = Uri.parse(Camera2VideoFragment.imagePath);
                vid.setVideoURI(uri);
                vid.start();


            }
        });

        retake.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                Log.d("LVMH", "Retake Video ");

                //closeButton.setVisibility(GONE);
                setContentView(R.layout.activity_camera);
                dialog.cancel();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, Camera2VideoFragment.newInstance())
                        .commit();
                dialog1.dismiss();
                // sharingScreen();

            }
        });

        share.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                Log.d("LVMH", "Lets share on Facebook ");

                //closeButton.setVisibility(GONE);
                vid.stopPlayback();
                dialog1.dismiss();
                // sharingScreen();

            }
        });


        vid.setVideoPath(Camera2VideoFragment.imagePath);
        //videoflag= false;
        dialog1.show();
    }

}
