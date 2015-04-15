package com.barliftapp.barlift;

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
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.Session;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.yalantis.phoenix.PullToRefreshView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import me.drakeet.materialdialog.MaterialDialog;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class FriendActivity extends ActionBarActivity {

    private Toolbar toolbar;

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

        // Fetch Facebook user info if the session is active
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            getUsers(dealId);
        }

    }

    private void getUsers(String dealId) {
        ParseUser currentUser = ParseUser.getCurrentUser();

        if (dealId == null) {
            updateListView((ArrayList<Object>) currentUser.getList("friends"), true);
        }else{
            setTitle("People Interested");
            HashMap<String, Object> params = new HashMap<>();
            params.put("deal_objectId", dealId);
            params.put("user_objectId", currentUser.getObjectId());
            ParseCloud.callFunctionInBackground("getFriends", params, new FunctionCallback<ArrayList<Object>>() {
                public void done(ArrayList<Object> result, ParseException e) {
                    if (e == null){
                        updateListView(result, false);
                    }else
                        Log.d("TAG", e.getMessage());
                }
            });
        }
    }

    private void updateListView(final ArrayList<Object> arrayList, final boolean hash){
        if (arrayList != null) {
            Collections.sort(arrayList, new Comparator<Object>() {
                @Override
                public int compare(Object p1, Object p2) {
                    if (hash) {
                        HashMap<String, String> hash1 = (HashMap<String, String>) p1;
                        HashMap<String, String> hash2 = (HashMap<String, String>) p2;
                        return hash1.get("name").compareTo(hash2.get("name"));
                    }else {
                        ArrayList<String> deet1 = (ArrayList<String>) p1;
                        ArrayList<String> deet2 = (ArrayList<String>) p2;
                        return deet1.get(0).compareTo(deet2.get(0));
                    }
                }

            });
            StickyListHeadersListView listView = (StickyListHeadersListView) findViewById(R.id.lv_friend);
            listView.setAdapter(new UserAdapter(this, arrayList, hash));

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, final int position, long id) {
                    Log.d("TAG", "item clicked: " + position);
                    Intent userIntent = new Intent(FriendActivity.this, ProfileActivity.class);
                    if (hash){
                        HashMap<String, String> deets = (HashMap<String, String>) arrayList.get(position);
                        userIntent.putExtra("userId", deets.get("fb_id"));
                    }else{
                        ArrayList<String> friend_deets = (ArrayList<String>) arrayList.get(position);
                        userIntent.putExtra("userId", friend_deets.get(1));
                    }

                    startActivity(userIntent);
                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friend, menu);
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
}
