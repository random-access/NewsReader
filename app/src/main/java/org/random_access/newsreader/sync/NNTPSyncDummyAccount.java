package org.random_access.newsreader.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

/**
 * Project: FlashCards Manager for Android
 * Date: 19.05.15
 * Author: Monika Schrenk
 * E-Mail: software@random-access.org
 */
public class NNTPSyncDummyAccount {

    private static final String TAG = NNTPSyncDummyAccount.class.getSimpleName();

    public static final String AUTHORITY = "org.random_access.newsreader.provider";

    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "org.random_access.newsreader.datasync";
    // The account name
    public static final String ACCOUNT = "dummyaccount";
    // Instance fields

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account createSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        Context.ACCOUNT_SERVICE);
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
        if (accounts.length > 0) {
            Log.d(TAG, "****** SUCCESS ***** Account found! - " + accounts[0].name);
            return accounts[0];
        }
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, new Bundle())) {
            ContentResolver.setIsSyncable(newAccount, AUTHORITY, 1);
            // ContentResolver.setMasterSyncAutomatically(true);
            ContentResolver.setSyncAutomatically(newAccount, AUTHORITY, true);
            Log.d(TAG, "****** SUCCESS ***** New Account created!");
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            return newAccount;
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
            Log.d(TAG, "Error creating account");
            // TODO do something else here
            return newAccount;
        }
    }

}
