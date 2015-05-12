package com.barliftapp.barlift.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;

import com.barliftapp.barlift.fragment.FirstFragment;
import com.barliftapp.barlift.R;
import com.barliftapp.barlift.util.ReachabilityTest;
import com.barliftapp.barlift.fragment.SecondFragment;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.Arrays;
import java.util.List;


public class LoginActivity extends FragmentActivity {

    static final String TAG = "BarliftLogin";

    // Declare Variables
    ViewPager viewPager;
    CirclePageIndicator mIndicator;
    private Dialog progressDialog;
    VideoView videoHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ParseUser currentUser = ParseUser.getCurrentUser();
        if ((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
            // Go to the main activity
            showMainActivity();
        }

        videoHolder = (VideoView) findViewById(R.id.videoview);
        videoHolder.setVideoURI(Uri.parse("android.resource://com.barliftapp.barlift/" + R.raw.wine));
        videoHolder.requestFocus();
        videoHolder.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        videoHolder.start();

        // Locate the ViewPager in viewpager_main.xml
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        // ViewPager Indicator
        mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        mIndicator.setSnap(true);
        mIndicator.setViewPager(viewPager);
    }

    public void onLoginClick(View v) {
        new ReachabilityTest(this, "google.com", 80, new ReachabilityTest.Callback() {
            @Override
            public void onReachabilityTestPassed() {
                logUserIn();
            }

            @Override
            public void onReachabilityTestFailed() {
                Toast.makeText(LoginActivity.this, "No Internet Connection.", Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }

    private void logUserIn(){
        progressDialog = ProgressDialog.show(LoginActivity.this, "", "Logging in...", true);

        List<String> permissions = Arrays.asList("public_profile", "email", "user_friends");

        ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                progressDialog.dismiss();
                if (user == null) {
                    Toast.makeText(LoginActivity.this, "Error logging in. Check Internet Connection.", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Log.d(TAG, "User signed up and logged in through Facebook!");

                    showProfileActivity();
                    //show second step for signing up
                } else {
                    Log.d(TAG, "User logged in through Facebook!");
                    showMainActivity();
                }
            }
        });
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }

    private void showMainActivity(){
        Intent main = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(main);
        LoginActivity.this.finish();
    }
    private void showProfileActivity(){
        Intent main = new Intent(LoginActivity.this, ProfileActivity.class);
        startActivity(main);
        LoginActivity.this.finish();
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            switch(pos) {

                case 0: return FirstFragment.newInstance();
                case 1: return SecondFragment.newInstance("Drink Spontaneously", "Stay in the know with daily local drink deals.", R.drawable.login1);
                case 2: return SecondFragment.newInstance("Never Drink Alone", "See friends that are interested in going with less hassle.", R.drawable.login2);
                case 3: return SecondFragment.newInstance("Nudge your Friends", "Invite your friends out with a simple gesture.", R.drawable.login3);
                default: return SecondFragment.newInstance("Drink Spontaneously", "Stay in the know with daily local drink deals.", R.drawable.login1);
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

}
