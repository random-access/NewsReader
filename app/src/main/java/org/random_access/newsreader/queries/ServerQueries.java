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
 * Project: FlashCards Manager for Android
 * Date: 17.05.15
 * Author: Monika Schrenk
 * E-Mail: software@random-access.org
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

    public Cursor getAllServers () {
        return context.getContentResolver().query(ServerContract.CONTENT_URI, PROJECTION_SERVER, null, null, null);
    }

    public Cursor getServerWithId(long serverId) throws IOException {
        return context.getContentResolver().query(Uri.parse(ServerContract.CONTENT_URI + "/" + serverId), PROJECTION_SERVER, null, null, null);
    }

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
