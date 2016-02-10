package org.random_access.newsreader.security;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * <b>Project:</b> NewsReader for Android <br>
 * <b>Date:</b> 10.02.16 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class CryptUtils {

    private static final String TAG = CryptUtils.class.getSimpleName();

    private static final String ENCRYPTION_KEY_ALIAS = "newsreader-crypt-key";

    private IKeyStoreHandler keyStoreHandler;

    private static CryptUtils instance;

    private CryptUtils () {
        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyStoreHandler = new AndroidMKeyStoreHandler();
            } else {
                keyStoreHandler = new LegacyKeyStoreHandler(null); // TODO !!!!!!!!!!!
            }
            if (!keyStoreHandler.hasKeyWithAlias(ENCRYPTION_KEY_ALIAS)) {
                keyStoreHandler.createKeyPair(ENCRYPTION_KEY_ALIAS);
                Log.d(TAG, "created new RSA key pair for password storage.");
            }
            Log.d(TAG, "Successfully initialized key store.");
        }
        catch(KeyStoreHandlerException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public static synchronized CryptUtils getInstance() {
        if (instance == null) {
            instance = new CryptUtils();
        }
        return  instance;
    }

    public String encrypt (String plaintext) throws KeyStoreHandlerException {
        PublicKey publicKey = keyStoreHandler.getPublicKey(ENCRYPTION_KEY_ALIAS);
        return keyStoreHandler.encryptString(plaintext, publicKey);
    }

    public String decrypt (String ciphertext) throws KeyStoreHandlerException {
        PrivateKey privateKey = keyStoreHandler.getPrivateKey(ENCRYPTION_KEY_ALIAS);
        return keyStoreHandler.decryptString(ciphertext, privateKey);
    }
}
