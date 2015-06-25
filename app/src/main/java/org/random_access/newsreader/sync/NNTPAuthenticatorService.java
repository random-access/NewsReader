package org.random_access.newsreader.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.07.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class NNTPAuthenticatorService extends Service {

    private NNTPServerAuthenticator mAuthenticator;

    public void onCreate() {
        mAuthenticator = new NNTPServerAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
