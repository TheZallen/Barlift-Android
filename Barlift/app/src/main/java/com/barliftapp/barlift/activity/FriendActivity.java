package com.barliftapp.barlift.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.barliftapp.barlift.R;
import com.barliftapp.barlift.adapter.UserAdapter;
import com.facebook.Session;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class FriendActivity extends ActionBarActivity {

    private Toolbar toolbar;
    private Boolean nudge;
    private String nudgeDeal;
    private UserAdapter mUserAdapter = null;
    private ArrayList<Object> mArrayListResult = new ArrayList<>();
    private int tempNudgeCount = 0;
    private boolean oneAndDone = true;
    public static HashMap<String, String> pendingNudges = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String dealId = intent.getStringExtra("dealId");
        nudgeDeal = intent.getStringExtra("nudgeDeal");
        nudge = intent.getBooleanExtra("nudge", false);

        // Fetch Facebook user info if the session is active
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            getUsers(dealId);
        }

    }

    private void getUsers(final String dealId) {
        final ParseUser currentUser = ParseUser.getCurrentUser();

        if (dealId == null) {
            mArrayListResult = (ArrayList<Object>) currentUser.getList("friends");
            if (mArrayListResult != null) {
                Collections.sort(mArrayListResult, new Comparator<Object>() {
                    @Override
                    public int compare(Object p1, Object p2) {
                        HashMap<String, String> hash1 = (HashMap<String, String>) p1;
                        HashMap<String, String> hash2 = (HashMap<String, String>) p2;
                        return hash1.get("name").compareTo(hash2.get("name"));
                    }

                });
            }
            updateListView(true);
        }else{
            setTitle("People Interested");
            HashMap<String, Object> params = new HashMap<>();
            params.put("deal_objectId", dealId);
            params.put("user_objectId", currentUser.getObjectId());
            ParseCloud.callFunctionInBackground("getInterestedFriends", params, new FunctionCallback<ArrayList<Object>>() {
                public void done(ArrayList<Object> result, ParseException e) {
                    if (e == null){
                        if (result != null) {
                            Collections.sort(result, new Comparator<Object>() {
                                @Override
                                public int compare(Object p1, Object p2) {
                                    ArrayList<String> deet1 = (ArrayList<String>) p1;
                                    ArrayList<String> deet2 = (ArrayList<String>) p2;
                                    return deet1.get(0).compareTo(deet2.get(0));
                                }

                            });
                        }
                        for (int x = 0; x < result.size(); x++){
                            ArrayList<String> deet = (ArrayList<String>) result.get(x);
                            deet.add(2, "1");
                            mArrayListResult.add(deet);
                        }
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("deal_objectId", dealId);
                        params.put("user_objectId", currentUser.getObjectId());
                        ParseCloud.callFunctionInBackground("getInterestedOthers", params, new FunctionCallback<ArrayList<Object>>() {
                            public void done(ArrayList<Object> resultOther, ParseException e) {
                                if (e == null){
                                    if (resultOther != null) {
                                        Collections.sort(resultOther, new Comparator<Object>() {
                                            @Override
                                            public int compare(Object p1, Object p2) {
                                                ArrayList<String> deet1 = (ArrayList<String>) p1;
                                                ArrayList<String> deet2 = (ArrayList<String>) p2;
                                                return deet1.get(0).compareTo(deet2.get(0));
                                            }

                                        });
                                    }
                                    for (int x = 0; x < resultOther.size(); x++){
                                        ArrayList<String> deet = (ArrayList<String>) resultOther.get(x);
                                        deet.add(2, "2");
                                        mArrayListResult.add(deet);
                                    }
                                    updateListView(false);
                                }else
                                    Log.d("TAG", e.getMessage());
                            }
                        });
                    }else
                        Log.d("TAG", e.getMessage());
                }
            });
        }
    }

    private void updateListView(final boolean hash){
        if (mArrayListResult != null) {
            final StickyListHeadersListView listView = (StickyListHeadersListView) findViewById(R.id.lv_friend);
            mUserAdapter = new UserAdapter(this, mArrayListResult, hash, nudge);
            listView.setAdapter(mUserAdapter);
//            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

            final TextView numberSelected = (TextView) findViewById(R.id.numbernudges);

            if (nudge){
                setTitle("Nudge your Friends");
                numberSelected.setVisibility(View.VISIBLE);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v, final int position, long id) {
                        Log.d("TAG", "item clicked: " + position);
                        HashMap<String, String> deets = (HashMap<String, String>) mArrayListResult.get(position);
                        if (pendingNudges.containsKey(deets.get("fb_id"))){
                            pendingNudges.remove(deets.get("fb_id"));
                        }else{
                            pendingNudges.put(deets.get("fb_id"), deets.get("name"));
                        }
                        numberSelected.setText(pendingNudges.size() + ((pendingNudges.size() == 1) ? " Friend" : " Friends") + " Selected");
                        invalidateOptionsMenu();
                        mUserAdapter.notifyDataSetChanged();
                    }
                });
            }else {
                numberSelected.setVisibility(View.GONE);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v, final int position, long id) {
                        Log.d("TAG", "item clicked: " + position);
                        Intent userIntent = new Intent(FriendActivity.this, ProfileActivity.class);
                        if (hash) {
                            HashMap<String, String> deets = (HashMap<String, String>) mArrayListResult.get(position);
                            userIntent.putExtra("userId", deets.get("fb_id"));
                        } else {
                            ArrayList<String> friend_deets = (ArrayList<String>) mArrayListResult.get(position);
                            userIntent.putExtra("userId", friend_deets.get(1));
                        }

                        startActivity(userIntent);
                    }
                });
            }
        }
    }

    private void sendNudges(){
        Iterator it = pendingNudges.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("fb", pair.getKey().toString());
            params.put("deal_objectId", nudgeDeal);
            Log.d("APP", pair.getKey().toString());
            Log.d("Deal", nudgeDeal);
            params.put("backMsg", "");
            params.put("reply", "");
            ParseCloud.callFunctionInBackground("nudge_v2", params, new FunctionCallback<Object>() {
                public void done(Object result, ParseException e) {
                    if (e == null) {
                        Log.d("APP", "nudge sent");
                        if (oneAndDone) {
                            Toast.makeText(FriendActivity.this, "Nudge(s) have been sent", Toast.LENGTH_SHORT).show();
                            finish();
                            oneAndDone = false;
                        }
                    }else{
                        Log.d("ERROR", e.getMessage());
                    }
                }
            });
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    public void onWhatNudgeClick(View v){
        new MaterialDialog.Builder(this)
                .title("What is a Nudge?")
                .theme(Theme.LIGHT)
                .content("A nudge sends a subtle push notification to your friends letting them know you want to see them out tonight. All you need to do is tap their photo or name, hit the send button in the top right corner, and they will get your notification.")
                .positiveText("Cool")
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friend, menu);

        if (nudge){
            if (pendingNudges.size() > 0) {
                menu.add(0, R.id.sendbuttonId, Menu.NONE, "Send")
                        .setIcon(R.drawable.ic_action_send_now)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        }
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
            case R.id.sendbuttonId:
                new MaterialDialog.Builder(this)
                        .title("Nudge?")
                        .theme(Theme.LIGHT)
                        .content("This will send a push to " + pendingNudges.size() + " of your friends. Are you sure?")
                        .positiveText("Nudge")
                        .negativeText("Cancel")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                sendNudges();
                            }
                        })
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tempNudgeCount = pendingNudges.size();
        pendingNudges.clear();
        mUserAdapter.notifyDataSetChanged();
    }
}
