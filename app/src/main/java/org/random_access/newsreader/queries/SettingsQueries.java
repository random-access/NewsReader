package org.random_access.newsreader.queries;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.random_access.newsreader.provider.contracts.SettingsContract;

/**
 * Project: FlashCards Manager for Android
 * Date: 18.05.15
 * Author: Monika Schrenk
 * E-Mail: software@random-access.org
 */
public class SettingsQueries {

    private Context context;

    private static final String[] PROJECTION_SETTINGS = new String[] {SettingsContract.SettingsEntry._ID,
            SettingsContract.SettingsEntry.COL_NAME, SettingsContract.SettingsEntry.COL_EMAIL, SettingsContract.SettingsEntry.COL_SIGNATURE,
    SettingsContract.SettingsEntry.COL_MSG_KEEP_DATE, SettingsContract.SettingsEntry.COL_MSG_KEEP_NO};

    public static final int COL_ID = 0;
    public static final int COL_NAME = 1;
    public static final int COL_EMAIL = 2;
    public static final int COL_SIGNATURE = 3;
    public static final int COL_MSG_KEEP_DAYS = 4;
    public static final int COL_MSG_KEEP_NO = 5;

    public SettingsQueries(Context context) {
        this.context = context;
    }

    public Cursor getSettingsForServer(long serverId) {
        return context.getContentResolver().query(Uri.parse(SettingsContract.CONTENT_URI + "/" + serverId), PROJECTION_SETTINGS, null, null, null);
    }

    public Uri addSettingsEntry(String name, String email, String signature, int msgKeepDays){
        ContentValues values = new ContentValues();
        values.put(SettingsContract.SettingsEntry.COL_NAME, name);
        values.put(SettingsContract.SettingsEntry.COL_EMAIL, email);
        values.put(SettingsContract.SettingsEntry.COL_SIGNATURE, signature);
        values.put(SettingsContract.SettingsEntry.COL_MSG_KEEP_DATE, msgKeepDays);
        values.put(SettingsContract.SettingsEntry.COL_MSG_KEEP_NO, 0); // TODO handle this
        return context.getContentResolver().insert(SettingsContract.CONTENT_URI, values);
    }
}
