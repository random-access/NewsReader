package org.random_access.newsreader.queries;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.random_access.newsreader.provider.contracts.MessageContract;
import org.random_access.newsreader.provider.contracts.MessageHierarchyContract;
import org.random_access.newsreader.provider.contracts.NewsgroupContract;
import org.random_access.newsreader.provider.contracts.ServerContract;
import org.random_access.newsreader.provider.contracts.SettingsContract;

import java.io.IOException;
import java.util.ArrayList;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.06.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class ServerQueries {

    private final Context context;

    private static final String[] PROJECTION_SERVER = new String[] {ServerContract.ServerEntry._ID,
            ServerContract.ServerEntry.COL_SERVERNAME, ServerContract.ServerEntry.COL_SERVERPORT,
            ServerContract.ServerEntry.COL_ENCRYPTION, ServerContract.ServerEntry.COL_AUTH, ServerContract.ServerEntry.COL_USER, ServerContract.ServerEntry.COL_PASSWORD,
            ServerContract.ServerEntry.COL_FK_SET_ID};

    public static final int COL_ID = 0;
    public static final int COL_NAME = 1;
    public static final int COL_PORT = 2;
    public static final int COL_ENCRYPTION = 3;
    public static final int COL_AUTH = 4;
    public static final int COL_USER = 5;
    public static final int COL_PASSWORD = 6;
    public static final int COL_SETTINGS_ID = 7;


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
    public Cursor getServerWithId(long serverId){
        return context.getContentResolver().query(Uri.parse(ServerContract.CONTENT_URI + "/" + serverId), PROJECTION_SERVER, null, null, null);
    }

    /**
     * Adds a server entry to the database
     * @param serverTitle the title, custom string from user input
     * @param serverName server URL e.g. news.example.org
     * @param serverPort port for NNTP connection (default: 119)
     * @param encryption 0 for no encryption, 1 for encryption (NOT YET IMPLEMENTED)
     * @param auth 0 for no authentication, 1 for authentication
     * @param user username to authenticate
     * @param password password to authenticate
     * @param settingsId _ID field of corresponding settings table entry
     * @return the URI to the created database entry, containing _ID for further use
     */
    @SuppressWarnings("SameParameterValue")
    public Uri addServer(String serverTitle, String serverName, int serverPort, boolean encryption, boolean auth, String user, String password, long settingsId) {
        ContentValues values = new ContentValues();
        values.put(ServerContract.ServerEntry.COL_TITLE, serverTitle);
        values.put(ServerContract.ServerEntry.COL_SERVERNAME, serverName);
        values.put(ServerContract.ServerEntry.COL_SERVERPORT, serverPort);
        values.put(ServerContract.ServerEntry.COL_ENCRYPTION, 0); // TODO handle encrypted connections
        values.put(ServerContract.ServerEntry.COL_AUTH, auth ? 1 : 0);
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
        // delete all newsgroups
        new NewsgroupQueries(context).deleteNewsgroupsFromServer(serverId);
        long settingsId = getServerSettingsId(serverId);

        // delete all server entries
        int noOfServerRows =  context.getContentResolver().delete(ServerContract.CONTENT_URI, ServerContract.ServerEntry._ID + " = ?", new String[] {serverId + ""});

        // delete all settings entries
        new SettingsQueries(context).deleteSettingsWitId(settingsId);
        return noOfServerRows;
    }

    public long getServerSettingsId(long serverId) {
        long settingsId = 0;
        Cursor serverCursor = context.getContentResolver().query(ServerContract.CONTENT_URI, PROJECTION_SERVER,
                ServerContract.ServerEntry._ID + " = ?", new String[]{serverId + ""}, null);
        if (serverCursor.moveToFirst()) {
            // we just have 1 setting entry for 1 project
            settingsId = serverCursor.getLong(COL_SETTINGS_ID);
        }
        serverCursor.close();
        return settingsId;
    }



    /**
     * Helper method to get the name of a newsgroup for a given ID
     * @param serverId database _ID field identifying a Newsgroup entry
     * @return String containing the name of the given newsgroup, e.g. formatted like this: "section1.section2.*.sectionl"
     * @throws IOException if there is no newsgroup matching the ID
     */
    public boolean hasServerAuth(long serverId) throws IOException {
        Cursor c = getServerWithId(serverId);
        if (!c.moveToFirst()) {
            throw new IOException("No newsgroup with the given ID found");
        }
        boolean auth = c.getInt(ServerQueries.COL_AUTH) == 1;
        c.close();
        return auth;
    }

}
