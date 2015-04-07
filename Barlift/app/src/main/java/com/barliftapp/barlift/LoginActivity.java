package com.barliftapp.barlift;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.Arrays;
import java.util.List;


public class LoginActivity extends FragmentActivity {

    // Declare Variables
    ViewPager viewPager;
    CirclePageIndicator mIndicator;
    private Dialog progressDialog;

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
                    Log.d(BarliftApplication.TAG, "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Log.d(BarliftApplication.TAG, "User signed up and logged in through Facebook!");

                    showProfileActivity();
                    //show second step for signing up
                } else {
                    Log.d(BarliftApplication.TAG, "User logged in through Facebook!");
                    showMainActivity();
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
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
                case 1: return SecondFragment.newInstance("Drink Spontaneously", "Stay in the know with daily local drink deals.", R.drawable.slide2);
                case 2: return SecondFragment.newInstance("Never Drink Alone", "See friends that are interested in going with less hassle.", R.drawable.slide3);
                case 3: return SecondFragment.newInstance("Nudge your Friends", "Invite your friends out with a simple gesture.", R.drawable.slide4);
                default: return SecondFragment.newInstance("Drink Spontaneously", "Stay in the know with daily local drink deals.", R.drawable.slide2);
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

}
