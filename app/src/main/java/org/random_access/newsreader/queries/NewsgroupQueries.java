package org.random_access.newsreader.queries;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.random_access.newsreader.provider.contracts.MessageContract;
import org.random_access.newsreader.provider.contracts.NewsgroupContract;

import java.io.IOException;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 18.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class NewsgroupQueries {

    private Context context;

    private static final String[] PROJECTION_NEWSGROUP = new String[] {NewsgroupContract.NewsgroupEntry._ID,
            NewsgroupContract.NewsgroupEntry.COL_NAME, NewsgroupContract.NewsgroupEntry.COL_TITLE , NewsgroupContract.NewsgroupEntry.COL_FK_SERV_ID};

    public static final int COL_ID = 0;
    public static final int COL_NAME = 1;
    public static final int COL_TITLE = 2;
    public static final int COL_SERVERID = 3;

    public NewsgroupQueries(Context context) {
        this.context = context;
    }

    /**
     * Get newsgroup identified by its _ID field
     * @param newsgroupId the _ID field from database
     * @return a cursor pointing in front of the result
     */
    public Cursor getNewsgroupForId(long newsgroupId) {
        return context.getContentResolver().query(Uri.parse(NewsgroupContract.CONTENT_URI + "/" + newsgroupId), PROJECTION_NEWSGROUP, null, null, null);
    }

    /**
     * Get all newsgroups that are stored in database from a given server
     * @param serverId the server _ID field from database
     * @return a cursor pointing in front of the result
     */
    public Cursor getNewsgroupsOfServer(long serverId) {
        return context.getContentResolver().query(NewsgroupContract.CONTENT_URI, PROJECTION_NEWSGROUP,
                NewsgroupContract.NewsgroupEntry.COL_FK_SERV_ID + " = ?", new String[] {serverId + ""}, null);
    }

    /**
     * Adds a newsgroup entry to the database
     * @param title name of newsgroup on the server
     * @param serverId _ID field of server the newsgroup is from
     * @return the URI to the created database entry, containing _ID for further use
     */
    public Uri addNewsgroup(String title, long serverId) {
        ContentValues values = new ContentValues();
        values.put(NewsgroupContract.NewsgroupEntry.COL_NAME, title);
        values.put(NewsgroupContract.NewsgroupEntry.COL_FK_SERV_ID, serverId);
        return context.getContentResolver().insert(NewsgroupContract.CONTENT_URI, values);
    }

    /**
     * Deletes a newsgroup with a given _ID field, deletes all messages from that group as well
     * @param newsgroupId _ID field of newsgroup
     * @return number of deleted entries.
     */
    public int deleteNewsgroup(long newsgroupId) {
        context.getContentResolver().delete(MessageContract.CONTENT_URI, MessageContract.MessageEntry.COL_FK_N_ID + " = ?", new String[]{newsgroupId + ""});
        return context.getContentResolver().delete(NewsgroupContract.CONTENT_URI, NewsgroupContract.NewsgroupEntry._ID + " = ?", new String[]{newsgroupId + ""});
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
     * @param context the Sync context
     * @param newsGroupId database _ID field identifying a Newsgroup entry
     * @return String containing the name of the given newsgroup, e.g. formatted like this: "section1.section2.*.sectionl"
     * @throws IOException if there is no newsgroup matching the ID
     */
    private String getNewsgroupName(Context context, long newsGroupId) throws IOException {
        NewsgroupQueries nQueries = new NewsgroupQueries(context);
        Cursor c = nQueries.getNewsgroupForId(newsGroupId);
        if (!c.moveToFirst()) {
            throw new IOException("No newsgroup with the given ID found");
        }
        String newsgroupName = c.getString(NewsgroupQueries.COL_NAME);
        c.close();
        return newsgroupName;
    }

}
