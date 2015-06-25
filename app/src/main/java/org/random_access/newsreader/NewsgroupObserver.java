package org.random_access.newsreader;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.random_access.newsreader.queries.NewsgroupQueries;
import org.random_access.newsreader.sync.NNTPSyncAdapter;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.07.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class NewsgroupObserver extends ContentObserver {

    private static final String TAG = NewsgroupObserver.class.getSimpleName();

    public NewsgroupObserver() {
        super(null);
    }

    @Override
    public void onChange(boolean selfChange) {
        onChange(selfChange, null);
    }


    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Bundle extras = new Bundle();
        extras.putString(NNTPSyncAdapter.SYNC_REQUEST_ORIGIN, TAG);
        extras.putBoolean(NNTPSyncAdapter.SYNC_REQUEST_TAG, true);
        ContentResolver.requestSync(ShowServerActivity.ACCOUNT, ShowServerActivity.AUTHORITY, extras);
    }
}
