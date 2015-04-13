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

        // Fetch Facebook user info if the session is active
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            getUsers();
        }


    }

    private void getUsers() {
        ParseUser currentUser = ParseUser.getCurrentUser();

        final ArrayList<Object> arrayList = (ArrayList<Object>) currentUser.getList("friends");

        if (arrayList != null) {
            Collections.sort(arrayList, new Comparator<Object>() {
                @Override
                public int compare(Object p1, Object p2) {
                    HashMap<String, String> hash1 = (HashMap<String, String>) p1;
                    HashMap<String, String> hash2 = (HashMap<String, String>) p2;
                    return hash1.get("name").compareTo(hash2.get("name"));
                }

            });
            StickyListHeadersListView listView = (StickyListHeadersListView) findViewById(R.id.lv_friend);
            listView.setAdapter(new UserAdapter(this, arrayList));

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, final int position, long id) {
                    Log.d("TAG", "item clicked: " + position);
                    Intent userIntent = new Intent(FriendActivity.this, ProfileActivity.class);
                    HashMap<String, String> deets = (HashMap<String, String>) arrayList.get(position);
                    userIntent.putExtra("userId", deets.get("fb_id"));
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
