package com.barliftapp.barlift.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.barliftapp.barlift.R;
import com.barliftapp.barlift.util.CircleTransform;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

public class OnBoardActivity extends ActionBarActivity {

    VideoView videoHolder;
    ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_board);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        videoHolder = (VideoView) findViewById(R.id.iv_onboard);
        videoHolder.setVideoURI(Uri.parse("android.resource://com.barliftapp.barlift/" + R.raw.wine));
        videoHolder.requestFocus();
        videoHolder.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        videoHolder.start();

        currentUser = ParseUser.getCurrentUser();

        Picasso.with(this)
                .load("https://graph.facebook.com/" + currentUser.getString("fb_id") + "/picture?type=normal&height=250&width=250")
                .transform(new CircleTransform())
                .into((ImageView) findViewById(R.id.iv_prof_onboard));

        TextView welcome = (TextView) findViewById(R.id.tv_onboard_title);
        if (currentUser.getString("full_name") != null) {
            welcome.setText("Welcome, " + currentUser.getString("full_name").substring(0, currentUser.getString("full_name").indexOf(" ")));
        }else{
            welcome.setText("Welcome");
        }
    }

    public void onStudentClick(View v){
        currentUser.put("is_student", true);
        currentUser.saveInBackground();
        startOnBoard2(true);
    }

    public void onGradClick(View v){
        currentUser.put("is_student", false);
        currentUser.saveInBackground();
        startOnBoard2(false);
    }

    private void startOnBoard2(boolean isStudent){
        Intent main = new Intent(this, OnBoard2Activity.class);
        main.putExtra("isStudent", isStudent);
        startActivity(main);
    }

    @Override
    public void onResume(){
        super.onResume();
        videoHolder.resume();
    }

    @Override
    public void onPause(){
        super.onPause();
        videoHolder.pause();
    }
}
