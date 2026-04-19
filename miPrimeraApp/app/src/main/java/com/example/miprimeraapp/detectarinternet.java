package com.example.miprimeraapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class detectarinternet {
    Context context;

    public detectarinternet(Context context) {
        this.context = context;
    }

    public boolean hayConexionInternet() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
        if (info == null) return false;

        for (NetworkInfo networkInfo : info) {
            if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        }
        return false;
    }
}
