package com.barliftapp.barlift;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.facebook.Session;
import com.facebook.widget.ProfilePictureView;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;


public class ProfileActivity extends Activity implements AdapterView.OnItemSelectedListener {

    private ImageView userProfilePictureImageView;
    private ImageView profileImageView;
    private TextView userNameView;
    private String teamSelection = "";
    private String nightSelection = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        userProfilePictureImageView = (ImageView) findViewById(R.id.userProfileImageView);
        profileImageView = (ImageView) findViewById(R.id.iv_profileback);
        userNameView = (TextView) findViewById(R.id.tv_userNameProf);

        Picasso.with(this)
                .load(R.drawable.cover)
                .into(profileImageView);

        // Fetch Facebook user info if the session is active
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            updateViewsWithProfileInfo();
        }

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.teams_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        Spinner spinner1 = (Spinner) findViewById(R.id.spinner2);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
                R.array.nights_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner1.setAdapter(adapter1);
        spinner1.setOnItemSelectedListener(this);

        final ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser.get("dm_team") != null){

            int spinnerPosition = adapter.getPosition(currentUser.get("dm_team").toString());
            int spinnerPosition1 = adapter1.getPosition(currentUser.get("num_nights").toString());

            //set the default according to value
            spinner.setSelection(spinnerPosition);
            spinner1.setSelection(spinnerPosition1);
        }

        final Button shareButton = (Button) findViewById(R.id.btn_save);
        shareButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveUserData(currentUser);
            }
        });
    }

    private void saveUserData(ParseUser currentUser){
        Spinner mySpinner = (Spinner) findViewById(R.id.spinner);
        Spinner mySpinner1 = (Spinner) findViewById(R.id.spinner2);
        currentUser.put("dm_team", mySpinner.getSelectedItem().toString());
        currentUser.put("num_nights", mySpinner1.getSelectedItem().toString());
        currentUser.saveInBackground();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        this.finish();
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
                            .into(userProfilePictureImageView);
                }

                if (userProfile.has("name")) {
                    userNameView.setText(userProfile.getString("name"));
                } else {
                    userNameView.setText("");
                }

            } catch (JSONException e) {
                Log.d(BarliftApplication.TAG, "Error parsing saved user data.");
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

}
