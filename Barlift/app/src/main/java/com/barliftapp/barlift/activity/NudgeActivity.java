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
import android.widget.ListView;

import com.barliftapp.barlift.R;
import com.barliftapp.barlift.adapter.NudgeAdapter;
import com.facebook.Session;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;

public class NudgeActivity extends ActionBarActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nudge);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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
        HashMap<String, Object> params = new HashMap<>();
        ParseCloud.callFunctionInBackground("getMyNudges", params, new FunctionCallback<ArrayList<Object>>() {
            public void done(final ArrayList<Object> result, ParseException e) {
                if (e == null) {
                    ListView listView = (ListView) findViewById(R.id.lv_nudge);
                    listView.setAdapter(new NudgeAdapter(NudgeActivity.this, result));
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, final int position, long id) {
                            Log.d("TAG", "item clicked: "+position);
                            ParseObject nudge = (ParseObject) result.get(position);
                            Intent dealIntent = new Intent(NudgeActivity.this, DealActivity.class);
                            dealIntent.putExtra("dealId", nudge.getParseObject("deal").getObjectId());
                            startActivity(dealIntent);
                        }
                    });
                } else
                    Log.d("TAG", e.getMessage());
            }
        });
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
