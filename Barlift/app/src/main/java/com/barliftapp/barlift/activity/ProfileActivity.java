package com.barliftapp.barlift.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.barliftapp.barlift.util.BlurTransform;
import com.barliftapp.barlift.util.CircleTransform;
import com.barliftapp.barlift.R;
import com.facebook.Session;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class ProfileActivity extends ActionBarActivity {

    static final String TAG = "BarliftProfile";

    private static final float MAX_TEXT_SCALE_DELTA = 0.3f;
    private static final boolean TOOLBAR_IS_STICKY = false;

    private View mToolbar;
    private View mImageView;
    private View mOverlayView;
    private View mListBackgroundView;
    private TextView mTitleView;
    private View mFab;
    private int mActionBarSize;
    private int mFlexibleSpaceShowFabOffset;
    private int mFlexibleSpaceImageHeight;
    private int mFabMargin;
    private int mToolbarColor;
    private boolean mFabIsShown;

    private ImageView userProfilePictureImageView;
    private ImageView profileImageView;
    private TextView userNameView;
    private TextView friendsOnView;
    private TextView dealsRedeemedView;
    private TextView partyScoreView;
    private TextView collegeView;
    private TextView affiliationView;
    private TextView lastSeenView;
    private TextView goingOutDaysView;
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
//        userNameView = (TextView) findViewById(R.id.tv_userNameProf);
        friendsOnView = (TextView) findViewById(R.id.tv_friends_on);
        dealsRedeemedView = (TextView) findViewById(R.id.tv_deals_redeemed);
        partyScoreView = (TextView) findViewById(R.id.tv_partyscore);
        collegeView = (TextView) findViewById(R.id.tv_college);
        affiliationView = (TextView) findViewById(R.id.tv_affiliation);
        lastSeenView = (TextView) findViewById(R.id.tv_last);
        goingOutDaysView = (TextView) findViewById(R.id.tv_goingDays);

        Intent intent = getIntent();
        String fbId = intent.getStringExtra("userId");

        // Fetch Facebook user info if the session is active
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            getProfileInfo(fbId);
        }
    }

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

                Picasso.with(this)
                        .load("https://graph.facebook.com/" + tempurl + "/picture?type=normal&height=250&width=250")
                        .transform(new BlurTransform(this))
                        .into(profileImageView);

                if (userProfile.has("name")) {
//                    userNameView.setText(userProfile.getString("name"));
                    setTitle(userProfile.getString("name"));
                } else {
//                    userNameView.setText("");
                }
//                    mOptionsMenu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_action_edit));
//                    mOptionsMenu.findItem(R.id.action_edit).setIcon(R.drawable.ic_action_edit);
            } catch (JSONException e) {
                Log.d(TAG, "Error parsing saved user data.");
            }
        }

        if (user.getString("university_name") != null){
            collegeView.setText(user.getString("university_name"));
        }else{
            collegeView.setText("");
        }
        if (user.getString("affiliation") != null){
            affiliationView.setText(user.getString("affiliation"));
        }else{
            affiliationView.setText("Tell them to add it!");
        }
        if (user.getString("bar_visited") != null){
            lastSeenView.setText(user.getString("bar_visited"));
        }else{
            lastSeenView.setText("Tell them to redeem some deals");
        }
        if (user.getList("selected_days") != null){
            ArrayList<Object> days = (ArrayList<Object>) user.getList("selected_days");
            String dayText = "";
            for (int x = 0; x < days.size(); x++){
                dayText += days.get(x) + ", ";
            }
            goingOutDaysView.setText(dayText.substring(0, dayText.length() - 2));
        }else{
            goingOutDaysView.setText("Nudge them");
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
                Intent edit = new Intent(this, EditActivity.class);
                startActivity(edit);
                finish();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
