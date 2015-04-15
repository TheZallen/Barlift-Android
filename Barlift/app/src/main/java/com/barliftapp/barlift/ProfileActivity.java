package com.barliftapp.barlift;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.widget.ProfilePictureView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class ProfileActivity extends ActionBarActivity {

    private ImageView userProfilePictureImageView;
    private ImageView profileImageView;
    private TextView userNameView;
    private TextView friendsOnView;
    private TextView dealsRedeemedView;
    private TextView partyScoreView;
    private String teamSelection = "";
    private String nightSelection = "";
    private Menu mOptionsMenu;

    private Toolbar toolbar;                              // Declaring the Toolbar Object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userProfilePictureImageView = (ImageView) findViewById(R.id.userProfileImageView);
        profileImageView = (ImageView) findViewById(R.id.iv_profileback);
        userNameView = (TextView) findViewById(R.id.tv_userNameProf);
        friendsOnView = (TextView) findViewById(R.id.tv_friends_on);
        dealsRedeemedView = (TextView) findViewById(R.id.tv_deals_redeemed);
        partyScoreView = (TextView) findViewById(R.id.tv_partyscore);

        Intent intent = getIntent();
        String fbId = intent.getStringExtra("userId");

        Picasso.with(this)
                .load(R.drawable.coverphoto)
                .into(profileImageView);

        // Fetch Facebook user info if the session is active
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            getProfileInfo(fbId);
        }

//        Spinner spinner = (Spinner) findViewById(R.id.spinner);
//        // Create an ArrayAdapter using the string array and a default spinner layout
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//                R.array.teams_array, android.R.layout.simple_spinner_item);
//        // Specify the layout to use when the list of choices appears
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        // Apply the adapter to the spinner
//        spinner.setAdapter(adapter);
//        spinner.setOnItemSelectedListener(this);
//
//        Spinner spinner1 = (Spinner) findViewById(R.id.spinner2);
//        // Create an ArrayAdapter using the string array and a default spinner layout
//        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
//                R.array.nights_array, android.R.layout.simple_spinner_item);
//        // Specify the layout to use when the list of choices appears
//        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        // Apply the adapter to the spinner
//        spinner1.setAdapter(adapter1);
//        spinner1.setOnItemSelectedListener(this);
//
//        final ParseUser currentUser = ParseUser.getCurrentUser();
//        if (currentUser.get("dm_team") != null){
//
//            int spinnerPosition = adapter.getPosition(currentUser.get("dm_team").toString());
//            int spinnerPosition1 = adapter1.getPosition(currentUser.get("num_nights").toString());
//
//            //set the default according to value
//            spinner.setSelection(spinnerPosition);
//            spinner1.setSelection(spinnerPosition1);
//        }
//
//        final Button shareButton = (Button) findViewById(R.id.btn_save);
//        shareButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                saveUserData(currentUser);
//            }
//        });
    }
//
//    private void saveUserData(ParseUser currentUser){
//        Spinner mySpinner = (Spinner) findViewById(R.id.spinner);
//        Spinner mySpinner1 = (Spinner) findViewById(R.id.spinner2);
//        currentUser.put("dm_team", mySpinner.getSelectedItem().toString());
//        currentUser.put("num_nights", mySpinner1.getSelectedItem().toString());
//        currentUser.saveInBackground();
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//        this.finish();
//    }

    private void getProfileInfo(String fbId) {
        if (fbId == null) {
            ParseUser currentUser = ParseUser.getCurrentUser();
            updateViewsWithInfo(currentUser);

        }else{
            ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
            query.whereEqualTo("fb_id", fbId);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                public void done(ParseObject object, ParseException e) {
                    if (object == null) {
                        Log.d("score", "The getFirst request failed.");
                    } else {
                        Log.d("score", "Retrieved the object.");
                        updateViewsWithInfo(object);
                    }
                }
            });
        }
    }

    private void updateViewsWithInfo(ParseObject user){
        if (user.has("profile")) {
            JSONObject userProfile = user.getJSONObject("profile");
            try {
                String tempurl = "";
                if (userProfile.has("fb_id")) {
                    tempurl = userProfile.getString("fb_id");
                }else if (userProfile.has("facebookId")){
                    tempurl = userProfile.getString("facebookId");
                }
                Picasso.with(this)
                        .load("https://graph.facebook.com/" + tempurl + "/picture?type=normal&height=250&width=250")
                        .transform(new CircleTransform())
                        .into(userProfilePictureImageView);

                if (userProfile.has("name")) {
                    userNameView.setText(userProfile.getString("name"));
                    setTitle(userProfile.getString("name"));
                } else {
                    userNameView.setText("");
                }
//                    mOptionsMenu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_action_edit));
//                    mOptionsMenu.findItem(R.id.action_edit).setIcon(R.drawable.ic_action_edit);
            } catch (JSONException e) {
                Log.d(BarliftApplication.TAG, "Error parsing saved user data.");
            }
        }

        friendsOnView.setText("" + user.getList("friends").size());
        dealsRedeemedView.setText("" + user.getNumber("deals_redeemed"));
        partyScoreView.setText("" + ((user.getList("friends").size() * (int)user.getNumber("deals_redeemed"))+132+user.getList("friends").size()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        Intent intent = getIntent();
        String fbId = intent.getStringExtra("userId");
        if (fbId == null) {
            menu.add(0, R.id.editbuttonId, Menu.NONE, "Edit")
                    .setIcon(R.drawable.ic_action_edit)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.editbuttonId:
//                openSearch();
                Toast.makeText(this, "HEYY", Toast.LENGTH_SHORT).show();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
