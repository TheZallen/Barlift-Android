package com.barliftapp.barlift;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.yalantis.phoenix.PullToRefreshView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class MainActivity extends ActionBarActivity {

    private String userId = "";
    private String fbId = "";

    //First We Declare Titles And Icons For Our Navigation Drawer List View
    //This Icons And Titles Are holded in an Array as you can see

    String TITLES[] = {"PROFILE","FRIENDS","SHARE","CALL UBER","LOG OUT"};
    int ICONS[] = {R.drawable.ic_action_person,R.drawable.ic_action_group,R.drawable.ic_action_share,R.drawable.ic_action_place,R.drawable.ic_action_warning};

    //Similarly we Create a String Resource for the name and email in the header view
    //And we also create a int resource for profile picture in the header view

    private Toolbar toolbar;                              // Declaring the Toolbar Object

    RecyclerView mRecyclerView;                           // Declaring RecyclerView
    RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    DrawerLayout Drawer;                                  // Declaring DrawerLayout

    ActionBarDrawerToggle mDrawerToggle;                  // Declaring Action Bar Drawer Toggle
    private PullToRefreshView mPullToRefreshView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setUpNavigationDrawer();

        // Fetch Facebook user info if the session is active
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            makeMeRequest();
            getFriends();
        }

        mPullToRefreshView = (PullToRefreshView) findViewById(R.id.pull_to_refresh);
        mPullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshView.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Deal");
        query.whereGreaterThanOrEqualTo("deal_end_date", new Date());
        query.whereEqualTo("community_name", ParseUser.getCurrentUser().getString("university_name"));
        query.orderByAscending("deal_start_date");
        query.include("user");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> dealList, ParseException e) {
                if (e == null) {
                    dealList.addAll(dealList);
                    dealList.addAll(dealList);
                    dealList.addAll(dealList);
                    StickyListHeadersListView listview = (StickyListHeadersListView) findViewById(R.id.lv_deal);
                    DealAdapter adapter = new DealAdapter(MainActivity.this, dealList);                      // use custom adapter
                    listview.setAdapter(adapter);
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });

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
                        .load("https://graph.facebook.com/" + userProfile.getString("fb_id") + "/picture?type=normal&height=300&width=300")
                        .transform(new CircleTransform())
                        .into((ImageView) findViewById(R.id.circleView));
                }

                if (userProfile.has("name")) {
                    TextView username = (TextView) findViewById(R.id.name);
                    username.setText(userProfile.getString("name"));
                }

            } catch (JSONException e) {
                Log.d(BarliftApplication.TAG, "Error parsing saved user data.");
            }
        }
    }

    private void setUpNavigationDrawer(){
        /* Assinging the toolbar object ot the view
        and setting the the Action bar to our toolbar
        */
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View

        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size

        mAdapter = new NavAdapter(TITLES,ICONS,"", this);       // Creating the Adapter of MyAdapter class(which we are going to see in a bit)
        // And passing the titles,icons,header view name, header view email,
        // and header view profile picture

        mRecyclerView.setAdapter(mAdapter);                              // Setting the adapter to RecyclerView

        mLayoutManager = new LinearLayoutManager(this);                 // Creating a layout Manager

        mRecyclerView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager


        Drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);        // Drawer object Assigned to the view
        mDrawerToggle = new ActionBarDrawerToggle(this,Drawer,toolbar,R.string.openDrawer,R.string.closeDrawer){

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // code here will execute once the drawer is opened( As I dont want anything happened whe drawer is
                // open I am not going to put anything here)
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Code here will execute once drawer is closed
            }



        }; // Drawer Toggle Object Made
        Drawer.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        mDrawerToggle.syncState();
    }

    public void logout() {
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
