package com.barliftapp.barlift;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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

import me.drakeet.materialdialog.MaterialDialog;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private ProfilePictureView userProfilePictureView;
    private TextView userNameView;
    private TextView dealView;
    private TextView barNameView;
    private TextView barAddressView;
    private TextView barDescView;
    private SlidingMenu menu;
    private String dealId = "";
    private String userId = "";
    private String fbId = "";
    private boolean isGoing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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
//        barDescView = (TextView) findViewById(R.id.tv_desc);
        dealView = (TextView) findViewById(R.id.tv_deal);
        barNameView = (TextView) findViewById(R.id.tv_barname);
        barAddressView = (TextView) findViewById(R.id.tv_baraddress);

        // Fetch Facebook user info if the session is active
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            makeMeRequest();
            getFriends();
        }

        ImageView rsvpButton = (ImageView) findViewById(R.id.btn_going);
        rsvpButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isGoing)
                    notGoing();
                else
                    rsvpClicked();
            }
        });

        ImageView menuButton = (ImageView) findViewById(R.id.menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                menu.showMenu();
            }
        });

//        final Button shareButton = (Button) findViewById(R.id.btn_share);
//        shareButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
//                sharingIntent.setType("text/plain");
//                String shareBody = dealView.getText() + " at " + barNameView.getText() + "! Go to http://www.barliftapp.com to get the app.";
//                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, dealView.getText());
//                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
//                startActivity(Intent.createChooser(sharingIntent, "Share deal via"));
//            }
//        });

        final ImageView purchaseButton = (ImageView) findViewById(R.id.btn_purchase);
        purchaseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final MaterialDialog mMaterialDialog = new MaterialDialog(MainActivity.this);
                mMaterialDialog
                        .setTitle("Purchase Drinks")
                        .setMessage("Hey there, we haven't added this feature yet but would you be interested in purchasing drinks through the app in the future?")
                        .setPositiveButton("Yes", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mMaterialDialog.dismiss();
                                ParseUser currentUser = ParseUser.getCurrentUser();
                                currentUser.put("pay_interest", true);
                                currentUser.saveInBackground();
                            }
                        })
                        .setNegativeButton("No", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mMaterialDialog.dismiss();
                                ParseUser currentUser = ParseUser.getCurrentUser();
                                currentUser.put("pay_interest", false);
                                currentUser.saveInBackground();
                            }
                        });

                mMaterialDialog.show();
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
                    ImageView menuButton = (ImageView) findViewById(R.id.btn_going);
                    menuButton.setImageResource(R.drawable.interested2);
                    isGoing = true;
                }
            }
        });
    }

    private void notGoing() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("deal_objectId", dealId);
        params.put("user_objectId", userId);
        ParseCloud.callFunctionInBackground("notGoing", params, new FunctionCallback<Integer>() {
            public void done(Integer result, ParseException e) {
                if (e == null){
                    final ImageView menuButton = (ImageView) findViewById(R.id.btn_going);
                    menuButton.setImageResource(R.drawable.interested3);
                    isGoing = false;
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            menuButton.setImageResource(R.drawable.interested1);
                        }
                    }, 500);
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
                    //barDescView.setText(deal.getString("description"));
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
                    for (int x = 0; x < result.size(); x++){
                        ArrayList<String> friend_detail = (ArrayList<String>)result.get(x);
                        if (friend_detail.get(1).equals(fbId)){
                            result.remove(x);
                            ImageView menuButton = (ImageView) findViewById(R.id.btn_going);
                            menuButton.setImageResource(R.drawable.interested2);
                            isGoing = true;
                            break;
                        }
                    }
                    result.addAll(result);
                    result.addAll(result);
                    result.addAll(result);
                    FriendGridView gridview = (FriendGridView) findViewById(R.id.gridView);
                    gridview.setAdapter(new FriendAdapter(MainActivity.this, result));
                }else
                    Log.d(TAG, e.getMessage());
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
                            fbId = user.getId();
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

    public void onProfileClick(View v) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void logout() {
        // Log the user out
        ParseUser.logOut();

        // Go to the login view
        startLoginActivity();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        this.finish();
    }
}
