package com.barliftapp.barlift;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;

public class DealActivity extends Activity {

    private static final String TAG = "MainActivity";

    private ImageView userProfilePictureView;
    private TextView userNameView;
    private TextView dealView;
    private TextView barNameView;
    private TextView barAddressView;
    private String barDesc;
    private ProgressBar spinnerProg;
    private SlidingMenu menu;
    private String dealId = "";
    private String userId = "";
    private String fbId = "";
    private String barLat = "";
    private String barLng = "";
    private boolean isGoing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT_RIGHT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        menu.setMenu(R.layout.menu);
        menu.setSecondaryMenu(R.layout.activity_nudge);


        userProfilePictureView = (ImageView) findViewById(R.id.userProfilePicture);
        userNameView = (TextView) findViewById(R.id.tv_userName);
        dealView = (TextView) findViewById(R.id.tv_deal);
        barNameView = (TextView) findViewById(R.id.tv_barname);
        barAddressView = (TextView) findViewById(R.id.tv_baraddress);
        spinnerProg = (ProgressBar) findViewById(R.id.spinnerProgress);


        loadPicsWithPicasso();

        // Fetch Facebook user info if the session is active
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            makeMeRequest();
            getFriends();
        }

        RelativeLayout rlContainer = (RelativeLayout) findViewById(R.id.rl_parawrapper);
        rlContainer.requestFocus();

        loadNudgePics();

        ImageView menuButton = (ImageView) findViewById(R.id.menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                menu.showMenu();
            }
        });
        ImageView nudgeButton = (ImageView) findViewById(R.id.nudgeButton);
        nudgeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                menu.showSecondaryMenu();
            }
        });

        refreshDeal();
    }

    private void loadPicsWithPicasso() {
        Picasso.with(this)
                .load(R.drawable.bg)
                .into((ImageView) findViewById(R.id.bg_view));

        Picasso.with(this)
                .load(R.drawable.hamburger)
                .into((ImageView)findViewById(R.id.menuButton));

        Picasso.with(this)
                .load(R.drawable.nudgeicon)
                .into((ImageView)findViewById(R.id.nudgeButton));

        Picasso.with(this)
                .load(R.drawable.martini)
                .into((ImageView)findViewById(R.id.iv_deal));

        Picasso.with(this)
                .load(R.drawable.purchase)
                .into((ImageView)findViewById(R.id.btn_purchase));
    }

    private void loadNudgePics() {
        ParseUser currentUser = ParseUser.getCurrentUser();

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("user_objectId", userId);
        ParseCloud.callFunctionInBackground("loadNudges", params, new FunctionCallback<Integer>() {
            public void done(Integer result, ParseException e) {
                if (e == null) {
                    TextView tv_count = (TextView) findViewById(R.id.tv_count);
                    tv_count.setText("" + result);
                }
            }
        });

        final ArrayList<Object> arrayList = (ArrayList<Object>) currentUser.getList("friends");


        if (arrayList != null){
            Collections.sort(arrayList, new Comparator<Object>() {
                @Override public int compare(Object p1, Object p2) {
                    HashMap<String, String> hash1 = (HashMap<String, String>) p1;
                    HashMap<String, String> hash2 = (HashMap<String, String>) p2;
                    return hash1.get("name").compareTo(hash2.get("name"));
                }

            });
            ListView listView = (ListView) findViewById(R.id.listView);
            listView.setAdapter(new NudgeAdapter(DealActivity.this, arrayList));
            YoYo.with(Techniques.Pulse)
                    .duration(1200)
                    .delay(1000)
                    .playOn(findViewById(R.id.tv_nudge));

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, final int position, long id) {
                    Log.d("TAG", "item clicked: " + position);
                    final TextView tv_count = (TextView) findViewById(R.id.tv_count);
                    final MaterialDialog mMaterialDialog = new MaterialDialog(DealActivity.this);
                    if (tv_count.getText().equals("0")) {
                        mMaterialDialog
                                .setTitle("Out of Nudges.")
                                .setMessage("You do not have any nudges left. Nudge someone tomorrow! :(")
                                .setPositiveButton("Ok", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mMaterialDialog.dismiss();
                                    }
                                });
                        mMaterialDialog.show();
                    } else {
                        mMaterialDialog
                                .setTitle("Nudge " + ((HashMap<String, String>) arrayList.get(position)).get("name") + "?")
                                .setMessage("You only have " + tv_count.getText() + " nudges left. Are you sure?")
                                .setPositiveButton("Yes", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mMaterialDialog.dismiss();
                                        HashMap<String, Object> params = new HashMap<String, Object>();
                                        params.put("receipient", ((HashMap<String, String>) arrayList.get(position)).get("fb_id"));
                                        ParseCloud.callFunctionInBackground("nudge", params, new FunctionCallback<Integer>() {
                                            public void done(Integer result, ParseException e) {
                                                if (e == null) {
                                                    Log.d("APP", "nudge sent");
                                                    tv_count.setText("" + result);
                                                }
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton("No", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mMaterialDialog.dismiss();
                                    }
                                });

                        mMaterialDialog.show();
                    }
                }
            });
        }
    }

    private void rsvpClicked() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("deal_objectId", dealId);
        params.put("user_objectId", userId);
        ImageView menuButton = (ImageView) findViewById(R.id.btn_going);
        menuButton.setImageResource(R.drawable.interested2);
        ParseCloud.callFunctionInBackground("imGoing", params, new FunctionCallback<Integer>() {
            public void done(Integer result, ParseException e) {
                if (e == null){
                    isGoing = true;
                }
            }
        });
    }

    private void notGoing() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("deal_objectId", dealId);
        params.put("user_objectId", userId);

        final ImageView menuButton = (ImageView) findViewById(R.id.btn_going);
        menuButton.setImageResource(R.drawable.interested3);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            public void run(){
                menuButton.setImageResource(R.drawable.interested1);
            }
        }, 500);
        ParseCloud.callFunctionInBackground("notGoing", params, new FunctionCallback<Integer>() {
            public void done(Integer result, ParseException e) {
                if (e == null){
                    isGoing = false;
                }
            }
        });
    }

    private void animateYoYo(Techniques tech, int duration, int delay, int id){
        YoYo.with(tech)
                .duration(duration)
                .delay(delay)
                .playOn(findViewById(id));
    }

    private void refreshDeal() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("location", "Northwestern");
        ParseCloud.callFunctionInBackground("getCurrentDeal", params, new FunctionCallback<ArrayList<Object>>() {
            public void done(ArrayList<Object> result, ParseException e) {
                spinnerProg.setVisibility(View.INVISIBLE);
                if (e == null && result.size() > 0){
                    ParseObject deal = (ParseObject)result.get(0);
                    dealView.setText(deal.getString("name"));
                    barNameView.setText(deal.getParseObject("user").getString("bar_name"));
                    barAddressView.setText(deal.getParseObject("user").getString("address"));
//                    barNameView.setText("Celtic Knot");
//                    barAddressView.setText("626 Church St, Evanston, IL 60201");
                    barLng = "" + deal.getParseObject("user").getParseGeoPoint("location").getLatitude();
                    barLat = "" + deal.getParseObject("user").getParseGeoPoint("location").getLongitude();
                    barDesc = deal.getString("description");
                    dealId = deal.getObjectId();
                    if (userId != "")
                        getWhosGoing();
                }else{
                    dealView.setText("No Current Deal");
                    barNameView.setText("Check back later");
                    barAddressView.setText("We usually release a deal for the night in the afternoon");
                }
                animateYoYo(Techniques.SlideInUp, 1200, 0, R.id.rl_bar);
                animateYoYo(Techniques.FadeIn, 1200, 0, R.id.rl_deal);
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
                        if (friend_detail.get(1).equals(fbId)) {
                            //result.remove(x);
                            ImageView menuButton = (ImageView) findViewById(R.id.btn_going);
                            menuButton.setImageResource(R.drawable.interested2);
                            isGoing = true;
                            break;
                        }
                    }
                    if (!isGoing)
                        animateYoYo(Techniques.Flash, 1000, 1500, R.id.btn_going);
                    animateYoYo(Techniques.Shake, 1000, 3000, R.id.nudgeButton);
                    FriendGridView gridview = (FriendGridView) findViewById(R.id.gridView);
                    gridview.setAdapter(new FriendAdapter(DealActivity.this, result));
                    animateYoYo(Techniques.Pulse, 1000, 3000, R.id.tv_going);

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

                        ParseInstallation install = ParseInstallation.getCurrentInstallation();

                        install.put("fb_id", user.getId());
                        install.put("user", currentUser);
                        install.addAllUnique("channels", Arrays.asList("Northwestern"));
                        install.saveInBackground();
                        try {
                            // Populate the JSON object - facebook id
                            fbId = user.getId();
                            userProfile.put("fb_id", user.getId());
                            currentUser.put("fb_id", user.getId());
                            // fb name, birthday, first_name, location, and prof pic
                            userProfile.put("name", user.getName());
                            userProfile.put("birthday", user.getBirthday());
                            userProfile.put("first_name", user.getFirstName());
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
            params.putInt("limit", 1000);
            friendRequest.setParameters(params);
            friendRequest.executeAsync();
        }
    }

    private void updateViewsWithProfileInfo() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser.has("profile")) {
            JSONObject userProfile = currentUser.getJSONObject("profile");
            try {

                if (userProfile.has("fb_id")) {
                    Picasso.with(this)
                            .load("https://graph.facebook.com/" + userProfile.getString("fb_id") + "/picture?type=normal&height=200&width=200")
                            .transform(new CircleTransform())
                            .into(userProfilePictureView);
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
    public void onDealClick(View v) {
        final MaterialDialog mMaterialDialog = new MaterialDialog(DealActivity.this);
        mMaterialDialog
                .setTitle("Deal Details")
                .setMessage(barDesc)
                .setPositiveButton("Cool", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();
                    }
                });

        mMaterialDialog.show();
    }
    public void onBarClick(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:<"+barLat+">,<"+barLng+">?q=<"+barNameView.getText()+">"));
        startActivity(intent);
    }
    public void onWhatClick(View v) {
        final MaterialDialog mMaterialDialog = new MaterialDialog(DealActivity.this);
        mMaterialDialog
                .setTitle("What is a Nudge?")
                .setMessage("A nudge sends a subtle push notification to your friends letting them know you want to see them out tonight. All you need to do is tap their photo or name, and they will get your notification. You have 5 nudges per day - so use them wisely!")
                .setPositiveButton("Cool", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();
                    }
                });

        mMaterialDialog.show();
    }
    public void onUberClick(View v) {
        PackageManager pm = DealActivity.this.getPackageManager();
        try
        {
            pm.getPackageInfo("com.ubercab", PackageManager.GET_ACTIVITIES);
            Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("com.ubercab");
            startActivity(LaunchIntent);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.ubercab"));
            startActivity(browserIntent);
        }
    }
    public void onPurchaseClick(View v){
        final MaterialDialog mMaterialDialog = new MaterialDialog(DealActivity.this);
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
    public void onShareClick(View v){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = dealView.getText() + " at " + barNameView.getText() + "! Go to http://www.barliftapp.com to get the app.";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, dealView.getText());
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share deal via"));
    }
    public void onRSVPClick(View v){
        if (isGoing)
            notGoing();
        else
            rsvpClicked();
    }
}
