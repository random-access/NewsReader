package org.random_access.newsreader.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.random_access.newsreader.queries.MessageQueries;

/**
 * <b>Project:</b> NewsReader for Android <br>
 * <b>Date:</b> 22.09.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class NotificationDismissReceiver extends BroadcastReceiver{

    private static final String TAG = NotificationDismissReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        new MessageQueries(context).markAllMessagesNonFresh();
        Log.i(TAG, "User dismissed fresh messages notification.");
    }
}
