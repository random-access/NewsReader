package org.random_access.newsreader.queries;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.random_access.newsreader.provider.contracts.SettingsContract;

import java.io.IOException;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.06.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class SettingsQueries {

    private static final String TAG = SettingsQueries.class.getSimpleName();

    private final Context context;

    private static final String[] PROJECTION_SETTINGS = new String[] {SettingsContract.SettingsEntry._ID,
            SettingsContract.SettingsEntry.COL_NAME, SettingsContract.SettingsEntry.COL_EMAIL, SettingsContract.SettingsEntry.COL_SIGNATURE,
    SettingsContract.SettingsEntry.COL_MSG_KEEP_DAYS, SettingsContract.SettingsEntry.COL_MSG_KEEP_NO};

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
        long settingsId = new ServerQueries(context).getServerSettingsId(serverId);
        return context.getContentResolver().query(Uri.parse(SettingsContract.CONTENT_URI + "/" + settingsId), PROJECTION_SETTINGS, null, null, null);
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
        values.put(SettingsContract.SettingsEntry.COL_MSG_KEEP_DAYS, msgKeepDays);
        values.put(SettingsContract.SettingsEntry.COL_MSG_KEEP_NO, -1); // TODO handle this
        return context.getContentResolver().insert(SettingsContract.CONTENT_URI, values);
    }

    /*
     * Helper method for getting the nummber of messages to keep in memory
     * @param serverId database _ID field identifying a Server entry
     * @return int - number of messages to keep
     * @throws IOException

    public int getNumberOfMessagesToKeep(long serverId) throws IOException{
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
    }*/

    /*
     * Helper method for getting the number of days to keep message headers in memory
     * @param serverId database _ID field identifying a Server entry
     * @return number of days to keep messages
     * @throws IOException

    public int getNumberOfDaysForKeepingMessages(long serverId) throws IOException{
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
    */

    /**
     * Deletes settings with a given ID
     * @param settingsId database _ID field identifying a Settings entry
     */
    public void deleteSettingsWitId(long settingsId) {
        int delCount =  context.getContentResolver().delete(SettingsContract.CONTENT_URI, SettingsContract.SettingsEntry._ID + " = ?", new String[]{settingsId + ""});
        Log.i(TAG, delCount + " rows deleted");
    }


}
