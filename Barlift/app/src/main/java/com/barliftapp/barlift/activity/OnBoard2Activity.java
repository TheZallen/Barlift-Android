package com.barliftapp.barlift.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.barliftapp.barlift.R;
import com.barliftapp.barlift.util.BarliftApplication;
import com.barliftapp.barlift.util.CircleTransform;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.ConfigCallback;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class OnBoard2Activity extends ActionBarActivity {

    VideoView videoHolder;
    ParseUser currentUser;
    String[] mUniversities;
    RadioGroup radiogroup;
    boolean isStudent;
    MixpanelAPI mMixpanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_board2);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mMixpanel = MixpanelAPI.getInstance(this, BarliftApplication.MIXPANEL_TOKEN);

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

        Intent intent = getIntent();
        isStudent = intent.getBooleanExtra("isStudent", true);

        TextView questionView = (TextView) findViewById(R.id.tv_onboard_question);
        String question = (isStudent) ? "Where do you go to school?" : "Where did you go to school?";
        questionView.setText(question);

        currentUser = ParseUser.getCurrentUser();

        populateRadioGroup();

    }

    private void populateRadioGroup(){
        // get reference to radio group in layout
        radiogroup = (RadioGroup) findViewById(R.id.radiogroup);
        // layout params to use when adding each radio button
        final LinearLayout.LayoutParams layoutParams = new RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(10, 10, 10, 10);
        layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());

        ParseConfig.getInBackground(new ConfigCallback() {
            @Override
            public void done(ParseConfig config, ParseException e) {
                if (e == null) {
                    Log.d("TAG", "Yay! Config was fetched from the server.");
                } else {
                    Log.e("TAG", "Failed to fetch. Using Cached Config.");
                    config = ParseConfig.getCurrentConfig();
                }
                mUniversities = config.getList("universities").toArray(new String[config.getList("universities").size()]);

                // add 20 radio buttons to the group
                for (int i = 0; i < mUniversities.length; i++) {
                    RadioButton newRadioButton = new RadioButton(OnBoard2Activity.this);
                    String label = mUniversities[i];
                    newRadioButton.setBackground(getResources().getDrawable(R.drawable.white_btn));
                    newRadioButton.setButtonDrawable(R.color.trans);
                    newRadioButton.setText(label);
                    newRadioButton.setGravity(Gravity.CENTER);
                    newRadioButton.setId(i);
                    radiogroup.addView(newRadioButton, layoutParams);
                }
            }
        });
    }

    public void onNextClick(View v){
        if (radiogroup.getCheckedRadioButtonId() != -1) {
            currentUser.put("university_name", mUniversities[radiogroup.getCheckedRadioButtonId()]);
            if (radiogroup.getCheckedRadioButtonId() == 0){
                currentUser.put("community_name", "Evanston");
            }else{
                currentUser.put("community_name", "Lincoln Park");
            }
            currentUser.saveInBackground();
            if (isStudent) {
                Intent main = new Intent(this, OnBoard3Activity.class);
                startActivity(main);
            }else{
                JSONObject props = new JSONObject();
                try {
                    props.put("Fb_id", currentUser.getString("fb_id"));
                    props.put("University", mUniversities[radiogroup.getCheckedRadioButtonId()]);
                    props.put("is_student", isStudent);
                    props.put("Time", new Date());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mMixpanel.track("Finished sign up", props);
                currentUser.put("affiliation", "I've graduated");
                currentUser.put("newVersion", true);
                currentUser.saveInBackground();
                Intent main = new Intent(this, MainActivity.class);
                main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(main);
                finish();
            }
        }else{
            Toast.makeText(this, "You must select a university", Toast.LENGTH_SHORT).show();
        }
//        currentUser.put("is_student", true);
//        currentUser.saveInBackground();
    }

    @Override
    public void onResume(){
        super.onResume();
        videoHolder.resume();
    }

    @Override
    protected void onDestroy() {
        mMixpanel.flush();
        super.onDestroy();
    }

    @Override
    public void onPause(){
        super.onPause();
        videoHolder.pause();
    }
}
