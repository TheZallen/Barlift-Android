package com.barliftapp.barlift;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.parse.GetCallback;
import com.parse.ParseCloud;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;

public class DealActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";

    private ImageView userProfilePictureView;
    private ImageView dealImageView;
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

    private Toolbar toolbar;                              // Declaring the Toolbar Object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dealView = (TextView) findViewById(R.id.tv_deal_text);
//        dealImageView = (ImageView) findViewById(R.id.iv_dealback);

        Intent intent = getIntent();
        String dealId = intent.getStringExtra("dealId");

//        Picasso.with(this)
//                .load(R.drawable.coverphoto)
//                .into(dealImageView);


        // Fetch Facebook user info if the session is active
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            loadDeal(dealId);
        }

    }

    private void loadDeal(final String dealId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Deal");
        query.whereEqualTo("objectId", dealId);
        query.include("user");
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

    private void updateViewsWithInfo(ParseObject object) {
        dealId = object.getObjectId();
        dealView.setText(object.getString("name"));
        setTitle(object.getParseObject("user").getString("bar_name"));
    }

    public void onGoingClick(View v){
        Intent goingIntent = new Intent(this, FriendActivity.class);
        goingIntent.putExtra("dealId", dealId);
        startActivity(goingIntent);
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
//    private void rsvpClicked() {
//        HashMap<String, Object> params = new HashMap<String, Object>();
//        params.put("deal_objectId", dealId);
//        params.put("user_objectId", userId);
//        ImageView menuButton = (ImageView) findViewById(R.id.btn_going);
//        menuButton.setImageResource(R.drawable.interested2);
//        ParseCloud.callFunctionInBackground("imGoing", params, new FunctionCallback<Integer>() {
//            public void done(Integer result, ParseException e) {
//                if (e == null){
//                    isGoing = true;
//                }
//            }
//        });
//    }
//
//    private void notGoing() {
//        HashMap<String, Object> params = new HashMap<String, Object>();
//        params.put("deal_objectId", dealId);
//        params.put("user_objectId", userId);
//
//        final ImageView menuButton = (ImageView) findViewById(R.id.btn_going);
//        menuButton.setImageResource(R.drawable.interested3);
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable(){
//            @Override
//            public void run(){
//                menuButton.setImageResource(R.drawable.interested1);
//            }
//        }, 500);
//        ParseCloud.callFunctionInBackground("notGoing", params, new FunctionCallback<Integer>() {
//            public void done(Integer result, ParseException e) {
//                if (e == null){
//                    isGoing = false;
//                }
//            }
//        });
//    }
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
