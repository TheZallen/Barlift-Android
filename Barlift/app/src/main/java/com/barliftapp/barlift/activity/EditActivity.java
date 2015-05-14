package com.barliftapp.barlift.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.barliftapp.barlift.R;
import com.parse.ConfigCallback;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;

public class EditActivity extends ActionBarActivity {

    private Toolbar toolbar;
    private String[] mPrefs;
    private String[] mDays = {"EVERYDAY","M","TU","W","TH","F","SAT","SUN"};
    ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        currentUser = ParseUser.getCurrentUser();

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        setTitle("Edit Profile");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ParseConfig.getInBackground(new ConfigCallback() {
            @Override
            public void done(ParseConfig config, ParseException e) {
                if (e == null) {
                    Log.d("TAG", "Yay! Config was fetched from the server.");
                } else {
                    Log.e("TAG", "Failed to fetch. Using Cached Config.");
                    config = ParseConfig.getCurrentConfig();
                }
                mPrefs = config.getList("deal_types").toArray(new String[config.getList("deal_types").size()]);
            }
        });


    }

    public void onEditClick(View v) {
        Intent onboard = new Intent(this, OnBoardActivity.class);
        startActivity(onboard);
    }

    public void onPrefClick(View v) {
        new MaterialDialog.Builder(EditActivity.this)
                .title("Choose Deal Preferences")
                .items(mPrefs)
                .theme(Theme.LIGHT)
                .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                        /**
                         * If you use alwaysCallMultiChoiceCallback(), which is discussed below,
                         * returning false here won't allow the newly selected check box to actually be selected.
                         * See the limited multi choice dialog example in the sample project for details.
                         **/
                        ArrayList<String> dealTypes = new ArrayList<String>();
                        for (Integer temp : which) {
                            dealTypes.add(mPrefs[temp]);
                        }
                        currentUser.put("deal_types", dealTypes);
                        currentUser.saveInBackground();
                        return true;
                    }
                })
                .positiveText("Save")
                .show();
    }

    public void onGoingClick(View v) {
        new MaterialDialog.Builder(EditActivity.this)
                .title("Choose Days you usually go out")
                .items(mDays)
                .theme(Theme.LIGHT)
                .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                        /**
                         * If you use alwaysCallMultiChoiceCallback(), which is discussed below,
                         * returning false here won't allow the newly selected check box to actually be selected.
                         * See the limited multi choice dialog example in the sample project for details.
                         **/
                        ArrayList<String> days = new ArrayList<String>();
                        for (Integer temp : which){
                            days.add(mDays[temp]);
                        }
                        currentUser.put("selected_days", days);
                        currentUser.saveInBackground();
                        return true;
                    }
                })
                .positiveText("Save")
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
