package com.barliftapp.barlift.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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

public class NudgeActivity extends ActionBarActivity {

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
        setContentView(R.layout.activity_nudge);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        setTitle("Nudges Received");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Fetch Facebook user info if the session is active
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            getFeed();
        }

    }

    private void getFeed() {
        final ParseUser currentUser = ParseUser.getCurrentUser();

        HashMap<String, Object> params = new HashMap<>();
        ParseCloud.callFunctionInBackground("getMyNudges", params, new FunctionCallback<ArrayList<Object>>() {
            public void done(ArrayList<Object> result, ParseException e) {
                if (e == null) {

                } else
                    Log.d("TAG", e.getMessage());
            }
        });
    }

    private void updateListView(final boolean hash) {
        if (mArrayListResult != null) {
            final StickyListHeadersListView listView = (StickyListHeadersListView) findViewById(R.id.lv_friend);
            mUserAdapter = new UserAdapter(this, mArrayListResult, hash, nudge);
            listView.setAdapter(mUserAdapter);
//            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

            final TextView numberSelected = (TextView) findViewById(R.id.numbernudges);

            if (nudge) {
                setTitle("Nudge your Friends");
                numberSelected.setVisibility(View.VISIBLE);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v, final int position, long id) {
                        Log.d("TAG", "item clicked: " + position);
                        HashMap<String, String> deets = (HashMap<String, String>) mArrayListResult.get(position);
                        if (pendingNudges.containsKey(deets.get("fb_id"))) {
                            pendingNudges.remove(deets.get("fb_id"));
                        } else {
                            pendingNudges.put(deets.get("fb_id"), deets.get("name"));
                        }
                        numberSelected.setText(pendingNudges.size() + ((pendingNudges.size() == 1) ? " Friend" : " Friends") + " Selected");
                        invalidateOptionsMenu();
                        mUserAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nudge, menu);

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
