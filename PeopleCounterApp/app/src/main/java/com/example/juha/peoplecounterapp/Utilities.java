package com.example.juha.peoplecounterapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class Utilities {

    public static boolean isInternetConnection(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

}
