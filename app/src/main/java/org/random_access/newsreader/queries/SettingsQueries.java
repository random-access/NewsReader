package org.random_access.newsreader.queries;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.random_access.newsreader.provider.contracts.SettingsContract;

import java.io.IOException;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 18.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
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

    /**
     * Get a cursor to all settings entries with all columns on a given server
     * @param serverId server _ID field in database
     * @return a cursor pointing before the first entry of the "table"
     */
    public Cursor getSettingsForServer(long serverId) {
        return context.getContentResolver().query(Uri.parse(SettingsContract.CONTENT_URI + "/" + serverId), PROJECTION_SETTINGS, null, null, null);
    }

    /**
     * Add an entry to the settings table
     * @param name name under which the user wants to post news
     * @param email e-mail address under which the user wants to post news
     * @param signature individual signature that will be automatically added below each message the user posts
     * @param msgKeepDays number of days messages should be saved on the phone (NOT YET IMPLEMENTED)
     * @return the URI to the created database entry, containing _ID for further use
     */
    public Uri addSettingsEntry(String name, String email, String signature, int msgKeepDays){
        ContentValues values = new ContentValues();
        values.put(SettingsContract.SettingsEntry.COL_NAME, name);
        values.put(SettingsContract.SettingsEntry.COL_EMAIL, email);
        values.put(SettingsContract.SettingsEntry.COL_SIGNATURE, signature);
        values.put(SettingsContract.SettingsEntry.COL_MSG_KEEP_DATE, msgKeepDays);
        values.put(SettingsContract.SettingsEntry.COL_MSG_KEEP_NO, 0); // TODO handle this
        return context.getContentResolver().insert(SettingsContract.CONTENT_URI, values);
    }

    /**
     * Helper method for getting the nummber of messages to keep in memory
     * @param context the Sync context
     * @param serverId database _ID field identifying a Server entry
     * @return int - number of messages to keep
     * @throws IOException
     */
    public int getNumberOfMessagesToKeep(Context context, long serverId) throws IOException{
        SettingsQueries sQueries = new SettingsQueries(context);
        Cursor c = sQueries.getSettingsForServer(serverId);
        if (c.getCount() > 0 ) {
            c.moveToFirst();
            int i = c.getInt(SettingsQueries.COL_MSG_KEEP_NO);
            c.close();
            return i;
        } else {
            c.close();
            throw new IOException("No settings for server with ID " + serverId + " found!");
        }
    }

    /**
     * Helper method for getting the number of days to keep message headers in memory
     * @param context the Sync context
     * @param serverId database _ID field identifying a Server entry
     * @return int - number of days to keep messages
     * @throws IOException
     */
    public int getNumberOfDaysForKeepingMessages(Context context, long serverId) throws IOException{
        SettingsQueries sQueries = new SettingsQueries(context);
        Cursor c =  sQueries.getSettingsForServer(serverId);
        if (!c.moveToFirst()) {
            c.close();
            throw new IOException("No settings for the given newsgroup!");
        } else {
            int i =  c.getInt(SettingsQueries.COL_MSG_KEEP_DAYS);
            c.close();
            return i;
        }
    }
}
