package com.barliftapp.barlift.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.afollestad.materialdialogs.Theme;
import com.barliftapp.barlift.fragment.DealFragment;
import com.barliftapp.barlift.fragment.FirstFragment;
import com.barliftapp.barlift.fragment.SecondFragment;
import com.barliftapp.barlift.util.BarliftApplication;
import com.barliftapp.barlift.util.CircleTransform;
import com.barliftapp.barlift.R;
import com.facebook.Session;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class DealActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";

    ViewPager viewPager;
    CirclePageIndicator mIndicator;

    private ImageView userProfilePictureView;
    private ImageView dealImageView;
    private TextView userNameView;
    private TextView timeView;
    private TextView barNameView;
    private TextView barAddressView;
    private TextView interestedView;
    private String barDesc;
    private String dealTitle;
    private String barName;
    private ProgressBar spinnerProg;
    private String dealId = "";
    private String barAddress = "";
    private String fbId = "";
    private String barLat = "";
    private String barLng = "";
    private boolean isGoing = false;

    ParseUser currentUser;
    MixpanelAPI mMixpanel;

    private Toolbar toolbar;                              // Declaring the Toolbar Object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        timeView = (TextView) findViewById(R.id.tv_hours);
        barAddressView = (TextView) findViewById(R.id.tv_address);
        interestedView = (TextView) findViewById(R.id.tv_interested);
        dealImageView = (ImageView) findViewById(R.id.iv_dealback);

        mMixpanel = MixpanelAPI.getInstance(this, BarliftApplication.MIXPANEL_TOKEN);
        currentUser = ParseUser.getCurrentUser();

        Intent intent = getIntent();
        String dealId = intent.getStringExtra("dealId");

        // Fetch Facebook user info if the session is active
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            loadDeal(dealId);
        }

    }

    private void loadDeal(final String dealId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Deal");
        query.whereEqualTo("objectId", dealId);
        query.include("venue");
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object == null) {
                    Log.d("score", "The getFirst request failed.");
                } else {
                    Log.d("score", "Retrieved the object.");
                    updateViewsWithInfo(object);
                    getInterestedFriends(object);
                    getNumberNudges(object);
                    startDealPager(object);
                    getHours(object);
                }
            }
        });
    }

    private void getHours(ParseObject deal){
        if (deal != null && deal.getNumber("start_utc") != null && deal.getNumber("end_utc") != null) {
            Calendar c = Calendar.getInstance();
            Calendar current = Calendar.getInstance();
            c.setTime(new Date((long) deal.getNumber("start_utc")));
            Calendar end = Calendar.getInstance();
            end.setTime(new Date((long) deal.getNumber("end_utc")));
            String headerText = "";
            if (c.get(Calendar.DAY_OF_MONTH) == current.get(Calendar.DAY_OF_MONTH)) {
                headerText = "TODAY";
            } else if (c.get(Calendar.DAY_OF_MONTH) == current.get(Calendar.DAY_OF_MONTH) + 1) {
                headerText = "TOMORROW";
            } else {
                //            headerText = format.format(c.get(Calendar.DAY_OF_WEEK));
                headerText = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US).toUpperCase();
            }
            String timeText = headerText + " | " + c.get(Calendar.HOUR) + " " + c.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.US) + " - ";
            timeText += end.get(Calendar.HOUR) + " " + end.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.US);
            timeView.setText(timeText);
        }else{
            timeView.setText("");
        }
    }

    private void startDealPager(ParseObject deal){
        // Locate the ViewPager in viewpager_main.xml

            viewPager = (ViewPager) findViewById(R.id.deal_pager);
            ArrayList<Object> temp = new ArrayList<>();
            temp.add(deal.getString("name").replace("\\n", "\n"));
        if (deal.getList("add_deals") != null) {
            temp.addAll(deal.getList("add_deals"));
        }
            viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(), temp));

            // ViewPager Indicator
            mIndicator = (CirclePageIndicator) findViewById(R.id.deal_indicator);
            mIndicator.setSnap(true);
            mIndicator.setViewPager(viewPager);

    }

    private void getNumberNudges(final ParseObject deal) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("dealID", dealId);
        ParseCloud.callFunctionInBackground("getNumberNudges", params, new FunctionCallback<Integer>() {
            public void done(Integer result, ParseException e) {
                if (e == null){
                    TextView numberNudges = (TextView) findViewById(R.id.tv_viralityScore);
//                    int adjustedNudges = result +
                    if (deal != null && deal.getNumber("start_utc") != null) {
                        Calendar c = Calendar.getInstance();
                        c.setTime(new Date((long) deal.getNumber("start_utc")));
                        result += c.get(Calendar.HOUR_OF_DAY);
                    }
                    numberNudges.setText(result + " nudges sent");
                }
            }
        });
    }

    private void getInterestedFriends(ParseObject object) {
        ParseUser currentUser = ParseUser.getCurrentUser();

        HashMap<String, Object> params = new HashMap<>();
        params.put("deal_objectId", object.getObjectId());
        params.put("user_objectId", currentUser.getObjectId());
        ParseCloud.callFunctionInBackground("getWhosGoing", params, new FunctionCallback<ArrayList<Object>>() {
            public void done(ArrayList<Object> result, ParseException e) {
                if (e == null) {
                    ArrayList<Object> detail = (ArrayList<Object>) result.get(0);
                    ToggleButton interested = (ToggleButton) findViewById(R.id.toggle_interested);
                    isGoing = (boolean) result.get(1);
                    interested.setChecked(isGoing);
                    int[] imageviews = {R.id.iv_friend1, R.id.iv_friend2, R.id.iv_friend3, R.id.iv_friend4, R.id.iv_friend5, R.id.iv_friend6};
                    int limit = (detail.size() < 6) ? detail.size() : 6;
                    for (int x = 0; x < limit; x++) {
                        HashMap<String, String> friend = (HashMap<String, String>) detail.get(x);
                        Picasso.with(DealActivity.this)
                                .load("https://graph.facebook.com/" + friend.get("fb_id") + "/picture?type=normal&height=100&width=100")
                                .transform(new CircleTransform())
                                .into((ImageView) findViewById(imageviews[x]));
                    }
                } else
                    Log.d("TAG", e.getMessage());
            }
        });
    }

    private void updateViewsWithInfo(ParseObject deal) {
        dealId = deal.getObjectId();
        if (deal.getParseObject("venue") != null && deal.getParseObject("venue").getString("address") != null) {
            barAddress = deal.getParseObject("venue").getString("address").replace("\\n", "\n");
            barAddressView.setText(barAddress);
        }
        if (deal.getString("description") != null) {
            barDesc = deal.getString("description");
            interestedView.setText("Who's Interested (" + deal.getNumber("num_accepted") + " going):");
        }
        if (deal.getString("name") != null){
            dealTitle = deal.getString("name").replace("\\n", "\n");
//            dealView.setText(dealTitle);
        }
        if (deal.getParseObject("venue") != null && deal.getParseObject("venue").getString("bar_name") != null) {
            barName = deal.getParseObject("venue").getString("bar_name");
            setTitle(barName);
        }
        Picasso.with(this)
                .load(deal.getString("image_url"))
                .into(dealImageView);
    }

    public void onGoingClick(View v){
        ParseUser currentUser = ParseUser.getCurrentUser();

        if (isGoing) {
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("deal_objectId", dealId);
            params.put("user_objectId", currentUser.getObjectId());
            Handler handler = new Handler();
            handler.postDelayed(new Runnable(){
                @Override
                public void run(){
//                    menuButton.setImageResource(R.drawable.interested1);
                }
            }, 500);
            ParseCloud.callFunctionInBackground("notGoing", params, new FunctionCallback<Integer>() {
                public void done(Integer result, ParseException e) {
                    if (e == null){
                        isGoing = false;
                    }
                }
            });
        }else{
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("deal_objectId", dealId);
            params.put("user_objectId", currentUser.getObjectId());
            ParseCloud.callFunctionInBackground("imGoing", params, new FunctionCallback<Integer>() {
                public void done(Integer result, ParseException e) {
                    if (e == null){
                        isGoing = true;
                    }
                }
            });
        }
    }

    public void onFriendsClick(View v){
        Intent goingIntent = new Intent(this, FriendActivity.class);
        goingIntent.putExtra("dealId", dealId);
        startActivity(goingIntent);
    }

    public void onNudgeClick(View v){
        Intent nudgeIntent = new Intent(this, FriendActivity.class);
        nudgeIntent.putExtra("nudge", true);
        nudgeIntent.putExtra("nudgeDeal", dealId);
        startActivity(nudgeIntent);
    }

    public void onDealShareClick(View v){
        JSONObject props = new JSONObject();
        try {
            props.put("Fb_id", currentUser.getString("fb_id"));
            props.put("University", currentUser.get("university_name"));
            props.put("DealID", dealId);
            props.put("Time", new Date());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mMixpanel.track("Deal Share Click", props);
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = dealTitle + " at " + barName + ". Let's go!";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, barName + " tonight?");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share deal via"));
    }

    public void onAddressClick(View v){
        String map = "http://maps.google.co.in/maps?q=" + barAddress;
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
        startActivity(i);
    }

    public void onDetailClick(View v){
        new MaterialDialog.Builder(this)
                .title("Deal Details")
                .theme(Theme.LIGHT)
                .content(barDesc)
                .positiveText("Cool")
                .show();
    }

    public void onViralityClick(View v){
        new MaterialDialog.Builder(this)
                .title("Virality")
                .theme(Theme.LIGHT)
                .content("This shows the number of nudges sent for this deal. Higher number means it's being shared more.")
                .positiveText("Cool")
                .show();
    }

    public void onUberClick(View v){
        PackageManager pm = getPackageManager();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_deal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        private List<Object> deals;

        public MyPagerAdapter(FragmentManager fm, List<Object> deals) {
            super(fm);
            this.deals = deals;
        }

        @Override
        public Fragment getItem(int pos) {
            return DealFragment.newInstance((String) deals.get(pos));
        }

        @Override
        public int getCount() {
            return deals.size();
        }
    }

    @Override
    protected void onDestroy() {
        mMixpanel.flush();
        super.onDestroy();
    }

//    private void loadNudgePics() {
//        ParseUser currentUser = ParseUser.getCurrentUser();
//
//        HashMap<String, Object> params = new HashMap<String, Object>();
//        params.put("user_objectId", userId);
//        ParseCloud.callFunctionInBackground("loadNudges", params, new FunctionCallback<Integer>() {
//            public void done(Integer result, ParseException e) {
//                if (e == null) {
//                    TextView tv_count = (TextView) findViewById(R.id.tv_count);
//                    tv_count.setText("" + result);
//                }
//            }
//        });
//
//        final ArrayList<Object> arrayList = (ArrayList<Object>) currentUser.getList("friends");
//
//
//        if (arrayList != null){
//            Collections.sort(arrayList, new Comparator<Object>() {
//                @Override public int compare(Object p1, Object p2) {
//                    HashMap<String, String> hash1 = (HashMap<String, String>) p1;
//                    HashMap<String, String> hash2 = (HashMap<String, String>) p2;
//                    return hash1.get("name").compareTo(hash2.get("name"));
//                }
//
//            });
//            ListView listView = (ListView) findViewById(R.id.listView);
//            listView.setAdapter(new UserAdapter(DealActivity.this, arrayList));
//            YoYo.with(Techniques.Pulse)
//                    .duration(1200)
//                    .delay(1000)
//                    .playOn(findViewById(R.id.tv_nudge));
//
//            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                public void onItemClick(AdapterView<?> parent, View v, final int position, long id) {
//                    Log.d("TAG", "item clicked: " + position);
//                    final TextView tv_count = (TextView) findViewById(R.id.tv_count);
//                    final MaterialDialog mMaterialDialog = new MaterialDialog(DealActivity.this);
//                    if (tv_count.getText().equals("0")) {
//                        mMaterialDialog
//                                .setTitle("Out of Nudges.")
//                                .setMessage("You do not have any nudges left. Nudge someone tomorrow! :(")
//                                .setPositiveButton("Ok", new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        mMaterialDialog.dismiss();
//                                    }
//                                });
//                        mMaterialDialog.show();
//                    } else {
//                        mMaterialDialog
//                                .setTitle("Nudge " + ((HashMap<String, String>) arrayList.get(position)).get("name") + "?")
//                                .setMessage("You only have " + tv_count.getText() + " nudges left. Are you sure?")
//                                .setPositiveButton("Yes", new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        mMaterialDialog.dismiss();
//                                        HashMap<String, Object> params = new HashMap<String, Object>();
//                                        params.put("receipient", ((HashMap<String, String>) arrayList.get(position)).get("fb_id"));
//                                        ParseCloud.callFunctionInBackground("nudge", params, new FunctionCallback<Integer>() {
//                                            public void done(Integer result, ParseException e) {
//                                                if (e == null) {
//                                                    Log.d("APP", "nudge sent");
//                                                    tv_count.setText("" + result);
//                                                }
//                                            }
//                                        });
//                                    }
//                                })
//                                .setNegativeButton("No", new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        mMaterialDialog.dismiss();
//                                    }
//                                });
//
//                        mMaterialDialog.show();
//                    }
//                }
//            });
//        }
//    }
//
//
//    private void animateYoYo(Techniques tech, int duration, int delay, int id){
//        YoYo.with(tech)
//                .duration(duration)
//                .delay(delay)
//                .playOn(findViewById(id));
//    }

//    private void refreshDeal() {
//        HashMap<String, Object> params = new HashMap<String, Object>();
//        params.put("location", "Northwestern");
//        ParseCloud.callFunctionInBackground("getCurrentDeal", params, new FunctionCallback<ArrayList<Object>>() {
//            public void done(ArrayList<Object> result, ParseException e) {
//                spinnerProg.setVisibility(View.INVISIBLE);
//                if (e == null && result.size() > 0){
//                    ParseObject deal = (ParseObject)result.get(0);
//                    dealView.setText(deal.getString("name"));
//                    barNameView.setText(deal.getParseObject("user").getString("bar_name"));
//                    barAddressView.setText(deal.getParseObject("user").getString("address"));
////                    barNameView.setText("Celtic Knot");
////                    barAddressView.setText("626 Church St, Evanston, IL 60201");
//                    barLng = "" + deal.getParseObject("user").getParseGeoPoint("location").getLatitude();
//                    barLat = "" + deal.getParseObject("user").getParseGeoPoint("location").getLongitude();
//                    barDesc = deal.getString("description");
//                    dealId = deal.getObjectId();
//                    if (userId != "")
//                        getWhosGoing();
//                }else{
//                    dealView.setText("No Current Deal");
//                    barNameView.setText("Check back later");
//                    barAddressView.setText("We usually release a deal for the night in the afternoon");
//                }
//            }
//        });
//    }

//    private void getWhosGoing(){
//        HashMap<String, Object> params = new HashMap<String, Object>();
//        params.put("deal_objectId", dealId);
//        params.put("user_objectId", userId);
//        ParseCloud.callFunctionInBackground("getFriends", params, new FunctionCallback<ArrayList<Object>>() {
//            public void done(ArrayList<Object> result, ParseException e) {
//                if (e == null){
//                    for (int x = 0; x < result.size(); x++){
//                        ArrayList<String> friend_detail = (ArrayList<String>)result.get(x);
//                        if (friend_detail.get(1).equals(fbId)) {
//                            //result.remove(x);
//                            ImageView menuButton = (ImageView) findViewById(R.id.btn_going);
//                            menuButton.setImageResource(R.drawable.interested2);
//                            isGoing = true;
//                            break;
//                        }
//                    }
//                    if (!isGoing)
//                        animateYoYo(Techniques.Flash, 1000, 1500, R.id.btn_going);
//                    animateYoYo(Techniques.Shake, 1000, 3000, R.id.nudgeButton);
//                    FriendGridView gridview = (FriendGridView) findViewById(R.id.gridView);
//                    gridview.setAdapter(new FriendAdapter(DealActivity.this, result));
//                    animateYoYo(Techniques.Pulse, 1000, 3000, R.id.tv_going);
//
//                }else
//                    Log.d(TAG, e.getMessage());
//            }
//        });
//    }





//    private void updateViewsWithProfileInfo() {
//        ParseUser currentUser = ParseUser.getCurrentUser();
//        if (currentUser.has("profile")) {
//            JSONObject userProfile = currentUser.getJSONObject("profile");
//            try {
//
//                if (userProfile.has("fb_id")) {
//                    Picasso.with(this)
//                            .load("https://graph.facebook.com/" + userProfile.getString("fb_id") + "/picture?type=normal&height=200&width=200")
//                            .transform(new CircleTransform())
//                            .into(userProfilePictureView);
//                }
//
//                if (userProfile.has("name")) {
//                    userNameView.setText(userProfile.getString("name"));
//                } else {
//                    userNameView.setText("");
//                }
//
//            } catch (JSONException e) {
//                Log.d(BarliftApplication.TAG, "Error parsing saved user data.");
//            }
//        }
//    }
//
//    public void onDealClick(View v) {
//        final MaterialDialog mMaterialDialog = new MaterialDialog(DealActivity.this);
//        mMaterialDialog
//                .setTitle("Deal Details")
//                .setMessage(barDesc)
//                .setPositiveButton("Cool", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        mMaterialDialog.dismiss();
//                    }
//                });
//
//        mMaterialDialog.show();
//    }
//    public void onBarClick(View v) {
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:<"+barLat+">,<"+barLng+">?q=<"+barNameView.getText()+">"));
//        startActivity(intent);
//    }
//    public void onWhatClick(View v) {
//        final MaterialDialog mMaterialDialog = new MaterialDialog(DealActivity.this);
//        mMaterialDialog
//                .setTitle("What is a Nudge?")
//                .setMessage("A nudge sends a subtle push notification to your friends letting them know you want to see them out tonight. All you need to do is tap their photo or name, and they will get your notification. You have 5 nudges per day - so use them wisely!")
//                .setPositiveButton("Cool", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        mMaterialDialog.dismiss();
//                    }
//                });
//
//        mMaterialDialog.show();
//    }
//
//    public void onPurchaseClick(View v){
//        final MaterialDialog mMaterialDialog = new MaterialDialog(DealActivity.this);
//        mMaterialDialog
//                .setTitle("Purchase Drinks")
//                .setMessage("Hey there, we haven't added this feature yet but would you be interested in purchasing drinks through the app in the future?")
//                .setPositiveButton("Yes", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        mMaterialDialog.dismiss();
//                        ParseUser currentUser = ParseUser.getCurrentUser();
//                        currentUser.put("pay_interest", true);
//                        currentUser.saveInBackground();
//                    }
//                })
//                .setNegativeButton("No", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        mMaterialDialog.dismiss();
//                        ParseUser currentUser = ParseUser.getCurrentUser();
//                        currentUser.put("pay_interest", false);
//                        currentUser.saveInBackground();
//                    }
//                });
//
//        mMaterialDialog.show();
//    }

//    public void onRSVPClick(View v){
//        if (isGoing)
//            notGoing();
//        else
//            rsvpClicked();
//    }
}
