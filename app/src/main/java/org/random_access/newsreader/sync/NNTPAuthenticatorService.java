package org.random_access.newsreader.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * <b>Project:</b> FlashCards Manager for Android <br>
 * <b>Date:</b> 18.05.15 <br>
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
