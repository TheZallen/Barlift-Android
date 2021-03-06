package com.barliftapp.barlift.util;

import android.app.Application;
import android.util.Log;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParsePush;
import com.parse.SaveCallback;

public class BarliftApplication extends Application{

    static final String TAG = "Barlift";
    public static final String MIXPANEL_TOKEN = "c8ecf107a7f4ff594d74841c9147c330";

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this,
                "5DZi1FrdZcwBKXIxMplWsqYu3cEEumlmFDB1kKnC",
                "tzrpMCtTU3FWlZAZUHFXBHObk4i9WW5AxAYKHx3E"
        );
        ParseFacebookUtils.initialize("1549337231980066");

        ParsePush.subscribeInBackground("global", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });
    }
}
