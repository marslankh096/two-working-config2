package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Controller {

    public static boolean isOnline(Context activity) {
        final NetworkInfo networkInfo = ((ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
