package com.barliftapp.barlift.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.barliftapp.barlift.util.BlurTransform;
import com.barliftapp.barlift.util.CircleTransform;
import com.barliftapp.barlift.adapter.DealAdapter;
import com.barliftapp.barlift.adapter.NavAdapter;
import com.barliftapp.barlift.R;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.ConfigCallback;
import com.parse.FindCallback;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class MainActivity extends ActionBarActivity {

    static final String TAG = "BarliftMain";

    private String userId = "";
    private String fbId = "";

    //First We Declare Titles And Icons For Our Navigation Drawer List View
    //This Icons And Titles Are holded in an Array as you can see

    String TITLES[] = {"PROFILE","FRIENDS","SHARE","NUDGES","LOCATION","LOG OUT"};
    int ICONS[] = {R.drawable.ic_action_person,R.drawable.ic_action_group,R.drawable.ic_action_share,R.drawable.ic_action_storage,R.drawable.ic_action_place,R.drawable.ic_action_warning};

    public static String[] mCommunities = {"Northwestern","NU"};

    //Similarly we Create a String Resource for the name and email in the header view
    //And we also create a int resource for profile picture in the header view

    private Toolbar toolbar;                              // Declaring the Toolbar Object

    RecyclerView mRecyclerView;                           // Declaring RecyclerView
    RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    DrawerLayout Drawer;                                  // Declaring DrawerLayout

    ActionBarDrawerToggle mDrawerToggle;                  // Declaring Action Bar Drawer Toggle

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

        refreshDeals();

        getLocations();

        final SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.pull_to_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshDeals();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.dblue, R.color.orange, R.color.green);
    }

    private void getLocations() {
        ParseConfig.getInBackground(new ConfigCallback() {
            @Override
            public void done(ParseConfig config, ParseException e) {
                if (e == null) {
                    Log.d("TAG", "Yay! Config was fetched from the server.");
                } else {
                    Log.e("TAG", "Failed to fetch. Using Cached Config.");
                    config = ParseConfig.getCurrentConfig();
                }
                mCommunities = config.getList("communities").toArray(new String[config.getList("communities").size()]);
            }
        });
    }

    public class DealComparator implements Comparator<ParseObject> {
        public int compare(ParseObject a, ParseObject b) {
            Calendar c = Calendar.getInstance();
            Calendar d = Calendar.getInstance();
            c.setTime(a.getDate("deal_start_date"));
            d.setTime(b.getDate("deal_start_date"));
            if (c.get(Calendar.DAY_OF_YEAR) == d.get(Calendar.DAY_OF_YEAR)){
                if (a.getBoolean("main")){
                    return -1;
                }else if (b.getBoolean("main")){
                    return 1;
                }else{
                    return 0;
                }
            }
            return 0;
        }
    }

    private void refreshDeals(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Deal");
        query.whereGreaterThanOrEqualTo("deal_end_date", new Date());
        query.whereEqualTo("community_name", ParseUser.getCurrentUser().getString("community_name"));
        query.orderByAscending("deal_start_date");
        query.include("venue");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(final List<ParseObject> dealList, ParseException e) {
                if (e == null) {
                    Collections.sort(dealList, new DealComparator());

                    StickyListHeadersListView listview = (StickyListHeadersListView) findViewById(R.id.lv_deal);
                    DealAdapter adapter = new DealAdapter(MainActivity.this, dealList);                      // use custom adapter
                    listview.setAdapter(adapter);
                    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, final int position, long id) {
                            Intent dealIntent = new Intent(MainActivity.this, DealActivity.class);
                            dealIntent.putExtra("dealId", dealList.get(position).getObjectId());
                            startActivity(dealIntent);
                        }
                    });
                    adapter.notifyDataSetChanged();
                    TextView nodeals = (TextView) findViewById(R.id.nodealstext);
                    if (dealList.isEmpty()){
                        nodeals.setVisibility(View.VISIBLE);
                    }else{
                        nodeals.setVisibility(View.GONE);
                    }
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
                            install.addAllUnique("channels", Arrays.asList(currentUser.getString("university_name")));
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
                                currentUser.put("full_name", user.getName());
                                currentUser.put("deals_redeemed", currentUser.get("deals_redeemed") != null ? currentUser.get("deals_redeemed") : 0);
                                currentUser.saveInBackground();

                                // Show the user info
                                updateViewsWithProfileInfo();

                            } catch (JSONException e) {
                                Log.d(TAG, "Error parsing returned user data. " + e);
                            }

                        } else if (response.getError() != null) {
                            if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY) ||
                                    (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
                                Log.d(TAG, "The facebook session was invalidated." + response.getError());
                                logout();
                            } else {
                                Log.d(TAG,
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
                                    Log.d(TAG, "Error parsing returned user data. " + e);
                                }

                            } else if (response.getError() != null) {
                                if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY) ||
                                        (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
                                    Log.d(TAG, "The facebook session was invalidated." + response.getError());
                                    logout();
                                } else {
                                    Log.d(TAG,
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
                        .into((ImageView) findViewById(R.id.navProfPic));
                    Picasso.with(this)
                        .load("https://graph.facebook.com/" + userProfile.getString("fb_id") + "/picture?type=normal&height=200&width=200")
                        .transform(new BlurTransform(this))
                        .into((ImageView) findViewById(R.id.navCoverPhoto));
                }

                if (userProfile.has("name")) {
                    TextView username = (TextView) findViewById(R.id.name);
                    username.setText(userProfile.getString("name"));
                }

            } catch (JSONException e) {
                Log.d(TAG, "Error parsing saved user data.");
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
        if (id == R.id.action_location) {
            int x;
            for (x = 0; x < mCommunities.length; x++){
                if (mCommunities[x].equals(ParseUser.getCurrentUser().getString("community_name"))) {
                    break;
                }
            }
            new MaterialDialog.Builder(this)
                    .title("Choose Location")
                    .items(mCommunities)
                    .theme(Theme.LIGHT)
                    .itemsCallbackSingleChoice(x, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            ParseUser.getCurrentUser().put("community_name", mCommunities[which]);
                            ParseUser.getCurrentUser().saveInBackground();
                                refreshDeals();
                                return true;
                            }
                        })
                        .positiveText("Select")
                        .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
