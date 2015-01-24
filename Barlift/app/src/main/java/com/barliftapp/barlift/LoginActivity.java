package com.barliftapp.barlift;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.List;


public class LoginActivity extends Activity {
    VideoView videoHolder;
    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        videoHolder = (VideoView)findViewById(R.id.videoview);
        videoHolder.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.wine));
        videoHolder.requestFocus();
        videoHolder.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        videoHolder.start();

        // Check if there is a currently logged in user
        // and it's linked to a Facebook account.
        ParseUser currentUser = ParseUser.getCurrentUser();
        if ((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
            // Go to the main activity
            showMainActivity();
        }

        //LoginButton button = (LoginButton) findViewById(R.id.authButton);
//        button.setReadPermissions(Arrays.asList("basic_info","email"));

        final Button aboutButton = (Button) findViewById(R.id.aboutButton);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent about = new Intent(LoginActivity.this, AboutActivity.class);
                startActivity(about);
            }
        });
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

        List<String> permissions = Arrays.asList("public_profile", "email", "user_friends", "user_relationships", "user_location");
        // NOTE: for extended permissions, like "user_about_me", your app must be reviewed by the Facebook team
        // (https://developers.facebook.com/docs/facebook-login/permissions/)

        ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                progressDialog.dismiss();
                if (user == null) {
                    Log.d(BarliftApplication.TAG, "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Log.d(BarliftApplication.TAG, "User signed up and logged in through Facebook!");
                    showMainActivity();
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
        videoHolder.resume();
    }

    @Override
    public void onPause(){
        super.onPause();
        videoHolder.suspend();
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
}
