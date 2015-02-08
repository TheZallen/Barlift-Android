package com.barliftapp.barlift;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.TransitionDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private ProfilePictureView userProfilePictureView;
    private TextView userNameView;
    private TextView dealView;
    private TextView barNameView;
    private TextView barAddressView;
    private SlidingMenu menu;
    private String dealId = "";
    private String userId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayUseLogoEnabled(true);
        ab.setLogo(R.drawable.ic_launcher);

        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.RIGHT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        menu.setMenu(R.layout.menu);

        userProfilePictureView = (ProfilePictureView) findViewById(R.id.userProfilePicture);
        userNameView = (TextView) findViewById(R.id.tv_userName);
        dealView = (TextView) findViewById(R.id.tv_deal);
        barNameView = (TextView) findViewById(R.id.tv_barname);
        barAddressView = (TextView) findViewById(R.id.tv_baraddress);

        // Fetch Facebook user info if the session is active
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            makeMeRequest();
            getFriends();
        }

        final Button rsvpButton = (Button) findViewById(R.id.btn_rsvp);
        rsvpButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rsvpClicked();
            }
        });

        final Button shareButton = (Button) findViewById(R.id.btn_share);
        shareButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = dealView.getText() + " at " + barNameView.getText() + "! Go to http://www.barliftapp.com to get the app.";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, dealView.getText());
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share deal via"));
            }
        });

        refreshDeal();
    }

    private void rsvpClicked() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("deal_objectId", dealId);
        params.put("user_objectId", userId);
        ParseCloud.callFunctionInBackground("imGoing", params, new FunctionCallback<Integer>() {
            public void done(Integer result, ParseException e) {
                if (e == null){
                    RelativeLayout rl = (RelativeLayout) findViewById(R.id.rl_bar);
                    TransitionDrawable transition = (TransitionDrawable) rl.getBackground();
                    transition.startTransition(1000);
                }
            }
        });
    }

    private void refreshDeal() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("location", "Northwestern");
        ParseCloud.callFunctionInBackground("getCurrentDeal", params, new FunctionCallback<ArrayList<Object>>() {
            public void done(ArrayList<Object> result, ParseException e) {
                if (e == null){
                    ParseObject deal = (ParseObject)result.get(0);
                    dealView.setText(deal.getString("name"));
                    barNameView.setText(deal.getParseObject("user").getString("bar_name"));
                    barAddressView.setText(deal.getParseObject("user").getString("address"));
                    dealId = deal.getObjectId();
                    if (userId != "")
                        getWhosGoing();
                }
            }
        });
    }

    private void getWhosGoing(){
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("deal_objectId", dealId);
        params.put("user_objectId", userId);
        ParseCloud.callFunctionInBackground("getFriends", params, new FunctionCallback<ArrayList<Object>>() {
            public void done(ArrayList<Object> result, ParseException e) {
                if (e == null){
                    FriendGridView gridview = (FriendGridView) findViewById(R.id.gv_friends);
                    gridview.setAdapter(new FriendAdapter(MainActivity.this, result));
                }else
                    Log.d("HYE", e.getMessage());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // Check if the user is currently logged
            // and show any cached content
            updateViewsWithProfileInfo();
        } else {
            // If the user is not logged in, go to the
            // activity showing the login view.
            startLoginActivity();
        }
    }

    private void makeMeRequest() {
        Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
            new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null) {
                        // Create a JSON object to hold the profile info
                        JSONObject userProfile = new JSONObject();

                        // Save user info in appropriate columns
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        userId = currentUser.getObjectId();
                        try {
                            // Populate the JSON object - facebook id
                            userProfile.put("facebookId", user.getId());
                            currentUser.put("fb_id", user.getId());
                            // fb name, birthday, first_name, location, and prof pic
                            userProfile.put("name", user.getName());
                            userProfile.put("birthday", user.getBirthday());
                            userProfile.put("first_name", user.getFirstName());
                            userProfile.put("location", user.getLocation().getName());
                            userProfile.put("pictureURL", "https://graph.facebook.com/" + user.getId() + "/picture?type=normal&return_ssl_resources=1");
                            if (user.getProperty("gender") != null) {
                                userProfile.put("gender", user.getProperty("gender"));
                            }
                            if (user.getProperty("email") != null) {
                                userProfile.put("email", user.getProperty("email"));
                            }

                            // Save the user profile info in a user property
                            currentUser.put("profile", userProfile);
                            currentUser.put("university_name", "Northwestern"); // HARD CODED - CHANGE LATER
                            currentUser.put("deals_redeemed", currentUser.get("deals_redeemed") != null ? currentUser.get("deals_redeemed") : 0);
                            currentUser.saveInBackground();

                            // Show the user info
                            updateViewsWithProfileInfo();

                            if (dealId != "")
                                getWhosGoing();
                        } catch (JSONException e) {
                            Log.d(BarliftApplication.TAG, "Error parsing returned user data. " + e);
                        }

                    } else if (response.getError() != null) {
                        if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY) ||
                                (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
                            Log.d(BarliftApplication.TAG, "The facebook session was invalidated." + response.getError());
                            logout();
                        } else {
                            Log.d(BarliftApplication.TAG,
                                    "Some other error: " + response.getError());
                        }
                    }
                }
            }
        );
        request.executeAsync();
    }

    private void getFriends(){
        Session activeSession = Session.getActiveSession();
        if(activeSession.getState().isOpened()){
            Request friendRequest = Request.newMyFriendsRequest(activeSession,
                new Request.GraphUserListCallback(){
                    @Override
                    public void onCompleted(List<GraphUser> users, Response response) {
                        if (users != null) {
                            // Create a JSON array to hold the friends
                            JSONArray userFriends = new JSONArray();

                            // Save friend info in appropriate columns
                            ParseUser currentUser = ParseUser.getCurrentUser();
                            try {
                                for (GraphUser user : users){
                                    JSONObject userJSON = new JSONObject();
                                    userJSON.put("name", user.getName());
                                    userJSON.put("fb_id", user.getId());
                                    userFriends.put(userJSON);
                                }

                                currentUser.put("friends", userFriends);
                                currentUser.saveInBackground();

                                // Show the user info
//                                updateViewsWithProfileInfo();
                            } catch (JSONException e) {
                                Log.d(BarliftApplication.TAG, "Error parsing returned user data. " + e);
                            }

                        } else if (response.getError() != null) {
                            if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY) ||
                                    (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
                                Log.d(BarliftApplication.TAG, "The facebook session was invalidated." + response.getError());
                                logout();
                            } else {
                                Log.d(BarliftApplication.TAG,
                                        "Some other error: " + response.getError());
                            }
                        }
                    }
                });
            Bundle params = new Bundle();
            params.putString("fields", "id, name");
            friendRequest.setParameters(params);
            friendRequest.executeAsync();
        }
    }

    private void updateViewsWithProfileInfo() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser.has("profile")) {
            JSONObject userProfile = currentUser.getJSONObject("profile");
            try {

                if (userProfile.has("facebookId")) {
                    userProfilePictureView.setProfileId(userProfile.getString("facebookId"));
                } else {
                    // Show the default, blank user profile picture
                    userProfilePictureView.setProfileId(null);
                }

                if (userProfile.has("name")) {
                    userNameView.setText(userProfile.getString("name"));
                } else {
                    userNameView.setText("");
                }

            } catch (JSONException e) {
                Log.d(BarliftApplication.TAG, "Error parsing saved user data.");
            }
        }
    }

    public void onLogoutClick(View v) {
        logout();
    }

    private void logout() {
        // Log the user out
        ParseUser.logOut();

        // Go to the login view
        startLoginActivity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_menu:
                menu.showMenu();
                return true;
            case R.id.action_refresh:
                refreshDeal();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        this.finish();
    }
}
