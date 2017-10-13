package com.abhisek.indiezsearch;

import android.app.Application;
import android.util.Log;

import com.amitshekhar.DebugDB;

/**
 * Created by bapu on 10/8/2017.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("address", DebugDB.getAddressLog());



    }

}
