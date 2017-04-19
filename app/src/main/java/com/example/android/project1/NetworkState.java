package com.example.android.project1;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by abspk on 18/11/2016.
 */

public class NetworkState {
    private Context context;

    public NetworkState(Context c) {
        this.context = c;
    }

    //this help was taken from StackOverflow
    public boolean isNetworkWorking() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }
}
