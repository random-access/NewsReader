package org.random_access.newsreader.queries;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.random_access.newsreader.provider.contracts.MessageContract;
import org.random_access.newsreader.provider.contracts.NewsgroupContract;

import java.io.IOException;
import java.util.ArrayList;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.06.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class NewsgroupQueries {

    private static final String TAG = NewsgroupQueries.class.getSimpleName();

    private final Context context;

    private static final String[] PROJECTION_NEWSGROUP = new String[] {NewsgroupContract.NewsgroupEntry._ID,
            NewsgroupContract.NewsgroupEntry.COL_NAME, NewsgroupContract.NewsgroupEntry.COL_TITLE , NewsgroupContract.NewsgroupEntry.COL_FK_SERV_ID,
            NewsgroupContract.NewsgroupEntry.COL_LAST_SYNC_DATE, NewsgroupContract.NewsgroupEntry.COL_MSG_LOAD_INTERVAL, NewsgroupContract.NewsgroupEntry.COL_MSG_KEEP_INTERVAL};

    public static final int COL_ID = 0;
    public static final int COL_NAME = 1;
    public static final int COL_TITLE = 2;
    public static final int COL_SERVERID = 3;
    public static final int COL_LAST_SYNC_DATE = 4;
    public static final int COL_MSG_LOAD_INTERVAL = 5;
    public static final int COL_MSG_KEEP_INTERVAL = 6;

    public NewsgroupQueries(Context context) {
        this.context = context;
    }

    /**
     * Get newsgroup identified by its _ID field
     * @param newsgroupId the _ID field from database
     * @return a cursor pointing in front of the result
     */
    private Cursor getNewsgroupForId(long newsgroupId) {
        return context.getContentResolver().query(Uri.parse(NewsgroupContract.CONTENT_URI + "/" + newsgroupId), PROJECTION_NEWSGROUP, null, null, null);
    }

    /**
     * Get all newsgroups that are stored in database from a given server
     * @param serverId the server _ID field from database
     * @return a cursor pointing in front of the result
     */
    public Cursor getNewsgroupsOfServer(long serverId) {
        return context.getContentResolver().query(NewsgroupContract.CONTENT_URI, PROJECTION_NEWSGROUP,
                NewsgroupContract.NewsgroupEntry.COL_FK_SERV_ID + " = ?", new String[]{serverId + ""}, null);
    }

    /**
     * Adds a newsgroup entry to the database
     * @param title name of newsgroup on the server
     * @param serverId _ID field of server the newsgroup is from
     * @return the URI to the created database entry, containing _ID for further use
     */
    public Uri addNewsgroup(String title, long serverId, long defaultMsgLoadInterval) {
        ContentValues values = new ContentValues();
        values.put(NewsgroupContract.NewsgroupEntry.COL_NAME, title);
        values.put(NewsgroupContract.NewsgroupEntry.COL_FK_SERV_ID, serverId);
        values.put(NewsgroupContract.NewsgroupEntry.COL_LAST_SYNC_DATE, -1); // not yet synced
        values.put(NewsgroupContract.NewsgroupEntry.COL_MSG_LOAD_INTERVAL, defaultMsgLoadInterval);
        values.put(NewsgroupContract.NewsgroupEntry.COL_MSG_KEEP_INTERVAL, -1); // default: keep all messages
        return context.getContentResolver().insert(NewsgroupContract.CONTENT_URI, values);
    }

    /**
     * Deletes a newsgroup with a given _ID field, deletes all messages from that group as well
     * @param newsgroupId _ID field of newsgroup
     * @return number of deleted entries.
     */
    public int deleteNewsgroup(long newsgroupId) {
        new MessageQueries(context).deleteMessagesFromNewsgroup(newsgroupId);
        return context.getContentResolver().delete(Uri.parse(NewsgroupContract.CONTENT_URI + "/" + newsgroupId), null, null);
    }

    /**
     * Deletes all newsgroups from server with id serverId
     * @param serverId id of a given server
     * @return number of newsgroups deleted
     */
    public void deleteNewsgroupsFromServer(long serverId) {
        MessageQueries messageQueries = new MessageQueries(context);
        Cursor cursor = getNewsgroupsOfServer(serverId);
        if (cursor.moveToFirst()) {
            while(!cursor.isAfterLast()) {
                messageQueries.deleteMessagesFromNewsgroup(cursor.getLong(COL_ID));
                cursor.moveToNext();
            }
        }
        cursor.close();
        int delCount =  context.getContentResolver().delete(NewsgroupContract.CONTENT_URI, NewsgroupContract.NewsgroupEntry.COL_FK_SERV_ID + " = ? ", new String[]{serverId + ""});
        Log.i(TAG, delCount + " rows deleted");
    }

    /**
     * Gets the number of newsgroups currently in sync from a given server
     * @param serverId _ID of server the newsgroups are from
     * @return number of newsgroups
     */
    public int getNewsgroupCountFromServer(long serverId) {
        return QueryHelper.count(context, NewsgroupContract.CONTENT_URI,
                NewsgroupContract.NewsgroupEntry.COL_FK_SERV_ID + " = ? ", new String[]{serverId + ""});
    }


    /**
     * Helper method to get the name of a newsgroup for a given ID
     * @param newsGroupId database _ID field identifying a Newsgroup entry
     * @return String containing the name of the given newsgroup, e.g. formatted like this: "section1.section2.*.sectionl"
     */
    public String getNewsgroupName(long newsGroupId) {
        NewsgroupQueries nQueries = new NewsgroupQueries(context);
        Cursor c = nQueries.getNewsgroupForId(newsGroupId);
        String newsgroupName = "";
        if (c.moveToFirst()) {
            newsgroupName = c.getString(NewsgroupQueries.COL_NAME);
        }
        c.close();
        return newsgroupName;
    }

    /**
     * Helper method for getting date of last sync
     * @param newsGroupId database _ID field identifying a newsgroup entry
     * @return long - date of last sync
     * @throws IOException
     */
    public long getLastSyncDate(long newsGroupId) throws IOException{
        Cursor c = getNewsgroupForId(newsGroupId);
        if (c.getCount() > 0 ) {
            c.moveToFirst();
            long i = c.getLong(COL_LAST_SYNC_DATE);
            c.close();
            return i;
        } else {
            c.close();
            throw new IOException("No settings for newsgroup with ID " + newsGroupId + " found!");
        }
    }

    /**
     * Helper method for settting date of last sync
     * @param newsGroupId database _ID field identifying a newsgroup entry
     * @param date date of last sync
     * @return true if update was committed, otherwise false
     */
    public boolean setLastSyncDate(long newsGroupId, long date) {
        ContentValues values = new ContentValues();
        values.put(NewsgroupContract.NewsgroupEntry.COL_LAST_SYNC_DATE, date);
        return context.getContentResolver().update(NewsgroupContract.CONTENT_URI, values, NewsgroupContract.NewsgroupEntry._ID + " = ?", new String[]{newsGroupId + ""}) > 0;
    }

    /**
     * Returns the column indicating how many days a message in the given newsgroup should be kept on this device
     * @param newsgroupId database _ID field identifying a newsgroup entry
     * @return number of days to keep messages, default (if no valid settings are found): 30
     */
    public int getMsgKeepDays(long newsgroupId){
        Cursor c = getNewsgroupForId(newsgroupId);
        if (c.moveToFirst()) {
            int i = c.getInt(COL_MSG_KEEP_INTERVAL);
            c.close();
            return i;
        } else {
            c.close();
            Log.d(TAG, "No settings for newsgroup with ID " + newsgroupId + " found!");
            return 30;
        }
    }

    /**
     * Stores how long messages should be kept into the database
     * @param newsgroupId database _ID field identifying a newsgroup entry
     * @param value length in days
     * @return true if value could be saved, otherwise false
     */
    public boolean setMsgKeepDays(long newsgroupId, int value) {
        ContentValues values = new ContentValues();
        values.put(NewsgroupContract.NewsgroupEntry.COL_MSG_KEEP_INTERVAL, value);
        return context.getContentResolver().update(NewsgroupContract.CONTENT_URI, values, NewsgroupContract.NewsgroupEntry._ID + " = ?", new String[] {newsgroupId + ""}) > 0;
    }

    /**
     *
     * Returns the column indicating which should be the max timespan to fetch messages.
     * @param newsgroupId database _ID field identifying a newsgroup entry
     * @return number of days to fetch messages, default (if no valid settings are found): 30
     */
    public int getMsgLoadDays(long newsgroupId){
        Cursor c = getNewsgroupForId(newsgroupId);
        if (c.moveToFirst()) {
            int i = c.getInt(COL_MSG_LOAD_INTERVAL);
            c.close();
            return i;
        } else {
            c.close();
            Log.d(TAG, "No settings for newsgroup with ID " + newsgroupId + " found!");
            return 30;
        }
    }

    /**
     * Stores tht max timespan to fetch messages into the database
     * @param newsgroupId database _ID field identifying a newsgroup entry
     * @param value timespan in days
     * @return true if value could be saved, otherwise false
     */
    public boolean setMsgLoadDays(long newsgroupId, int value) {
        ContentValues values = new ContentValues();
        values.put(NewsgroupContract.NewsgroupEntry.COL_MSG_LOAD_INTERVAL, value);
        return context.getContentResolver().update(NewsgroupContract.CONTENT_URI, values, NewsgroupContract.NewsgroupEntry._ID + " = ?", new String[] {newsgroupId + ""}) > 0;
    }

}
