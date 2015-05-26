package org.random_access.newsreader.queries;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.random_access.newsreader.provider.contracts.MessageContract;
import org.random_access.newsreader.provider.contracts.NewsgroupContract;
import org.random_access.newsreader.provider.contracts.ServerContract;
import org.random_access.newsreader.provider.contracts.SettingsContract;

import java.io.IOException;

/**
 * <b>Project:</b> FlashCards Manager for Android <br>
 * <b>Date:</b> 18.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class ServerQueries {

    private Context context;

    private static final String[] PROJECTION_SERVER = new String[] {ServerContract.ServerEntry._ID,
            ServerContract.ServerEntry.COL_SERVERNAME, ServerContract.ServerEntry.COL_SERVERPORT,
            ServerContract.ServerEntry.COL_ENCRYPTION, ServerContract.ServerEntry.COL_USER, ServerContract.ServerEntry.COL_PASSWORD};

    public static final int COL_ID = 0;
    public static final int COL_NAME = 1;
    public static final int COL_PORT = 2;
    public static final int COL_ENCRYPTION = 3;
    public static final int COL_USER = 4;
    public static final int COL_PASSWORD = 5;


    public ServerQueries(Context context) {
        this.context = context;
    }

    /**
     * Get all servers from the database
     * @return cursor pointing in front of the result "table"
     */
    public Cursor getAllServers () {
        return context.getContentResolver().query(ServerContract.CONTENT_URI, PROJECTION_SERVER, null, null, null);
    }

    /**
     *
     * @param serverId _ID field of server
     * @return cursor pointing in front of the result "table"
     */
    public Cursor getServerWithId(long serverId) {
        return context.getContentResolver().query(Uri.parse(ServerContract.CONTENT_URI + "/" + serverId), PROJECTION_SERVER, null, null, null);
    }

    /**
     * Adds a server entry to the database
     * @param serverTitle the title, custom string from user input
     * @param serverName server URL e.g. news.example.org
     * @param serverPort port for NNTP connection (default: 119)
     * @param encryption 0 for no encryption, 1 for encryption (NOT YET IMPLEMENTED)
     * @param user username to authenticate
     * @param password password to authenticate
     * @param settingsId _ID field of corresponding settings table entry
     * @return the URI to the created database entry, containing _ID for further use
     */
    public Uri addServer(String serverTitle, String serverName, int serverPort, boolean encryption, String user, String password, long settingsId) {
        ContentValues values = new ContentValues();
        values.put(ServerContract.ServerEntry.COL_TITLE, serverTitle);
        values.put(ServerContract.ServerEntry.COL_SERVERNAME, serverName);
        values.put(ServerContract.ServerEntry.COL_SERVERPORT, serverPort);
        values.put(ServerContract.ServerEntry.COL_ENCRYPTION, 0); // TODO handle encrypted connections
        values.put(ServerContract.ServerEntry.COL_USER, user);
        values.put(ServerContract.ServerEntry.COL_PASSWORD, password);
        values.put(ServerContract.ServerEntry.COL_FK_SET_ID, settingsId);
        return context.getContentResolver().insert(ServerContract.CONTENT_URI, values);
    }

    /**
     * Deletes a newsgroup with a given _ID field, deletes as well:
     * <ul>
     *     <li>all newsgroups</li>
     *     <li>all message headers</li>
     *     <li>the settings entry</li>
     * </ul>
     * that were synced from that server
     * @param serverId _ID field of server
     * @return number of server deletions (should be 1 if deletion was correct & successful)
     */
    public int deleteServerWithId(long serverId) {
        Cursor newsgroupCursor = context.getContentResolver().query(NewsgroupContract.CONTENT_URI, new String[]{NewsgroupContract.NewsgroupEntry._ID},
                NewsgroupContract.NewsgroupEntry.COL_FK_SERV_ID + " = ?", new String[]{serverId + ""}, NewsgroupContract.NewsgroupEntry._ID + " ASC");
        if (newsgroupCursor.moveToFirst()) {
            while (!newsgroupCursor.isAfterLast()) {
                long newsgroupId = newsgroupCursor.getLong(0);
                context.getContentResolver().delete(MessageContract.CONTENT_URI, MessageContract.MessageEntry.COL_FK_N_ID + " = ?", new String[] {newsgroupId + ""});
                newsgroupCursor.moveToNext();
            }
        }
        newsgroupCursor.close();

        long settingsId = 0;
        Cursor serverCursor = context.getContentResolver().query(ServerContract.CONTENT_URI, new String[]{ServerContract.ServerEntry.COL_FK_SET_ID},
                ServerContract.ServerEntry._ID + " = ?", new String[]{serverId + ""}, null);
        if (!serverCursor.moveToFirst()) {
            // we just have 1 setting entry for 1 project
            settingsId = serverCursor.getLong(0);
        }

        context.getContentResolver().delete(NewsgroupContract.CONTENT_URI, NewsgroupContract.NewsgroupEntry.COL_FK_SERV_ID + " = ?", new String[]{serverId + ""});

        int noOfServerRows =  context.getContentResolver().delete(ServerContract.CONTENT_URI, ServerContract.ServerEntry._ID + " = ?", new String[] {serverId + ""});

        context.getContentResolver().delete(SettingsContract.CONTENT_URI, SettingsContract.SettingsEntry._ID + "= ?", new String[] {settingsId + ""});

        return noOfServerRows;
    }


}
