package org.random_access.newsreader.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Project: FlashCards Manager for Android
 * Date: 17.05.15
 * Author: Monika Schrenk
 * E-Mail: software@random-access.org
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
