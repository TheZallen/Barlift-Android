package com.barliftapp.barlift;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;

public class BarliftApplication extends Application{

    static final String TAG = "Barlift";

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this,
                "5DZi1FrdZcwBKXIxMplWsqYu3cEEumlmFDB1kKnC",
                "tzrpMCtTU3FWlZAZUHFXBHObk4i9WW5AxAYKHx3E"
        );
        ParseFacebookUtils.initialize("1549337231980066");
    }
}
