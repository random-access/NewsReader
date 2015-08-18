package org.random_access.newsreader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 26.06.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class NetworkStateHelper {

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static boolean hasWifiConnection(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return  netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

}
