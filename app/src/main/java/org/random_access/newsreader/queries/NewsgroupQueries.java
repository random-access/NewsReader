package org.random_access.newsreader.queries;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.random_access.newsreader.provider.contracts.MessageContract;
import org.random_access.newsreader.provider.contracts.NewsgroupContract;

/**
 * Project: FlashCards Manager for Android
 * Date: 18.05.15
 * Author: Monika Schrenk
 * E-Mail: software@random-access.org
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


    public Cursor getNewsgroupForId(long newsgroupId) {
        return context.getContentResolver().query(Uri.parse(NewsgroupContract.CONTENT_URI + "/" + newsgroupId), PROJECTION_NEWSGROUP, null, null, null);
    }

    public Cursor getNewsgroupsOfServer(long serverId) {
        return context.getContentResolver().query(NewsgroupContract.CONTENT_URI, PROJECTION_NEWSGROUP,
                NewsgroupContract.NewsgroupEntry.COL_FK_SERV_ID + " = ?", new String[] {serverId + ""}, null);
    }

    public Uri addNewsgroup(String title, long serverId) {
        ContentValues values = new ContentValues();
        values.put(NewsgroupContract.NewsgroupEntry.COL_NAME, title);
        values.put(NewsgroupContract.NewsgroupEntry.COL_FK_SERV_ID, serverId);
        return context.getContentResolver().insert(NewsgroupContract.CONTENT_URI, values);
    }

    public int deleteNewsgroup (long newsgroupId) {
        context.getContentResolver().delete(MessageContract.CONTENT_URI, MessageContract.MessageEntry.COL_FK_N_ID + " = ?", new String[]{newsgroupId + ""});
        return context.getContentResolver().delete(NewsgroupContract.CONTENT_URI, NewsgroupContract.NewsgroupEntry._ID + " = ?", new String[]{newsgroupId + ""});
    }

    public int getNewsgroupCountFromServer(long serverId) {
        return QueryHelper.count(context, NewsgroupContract.CONTENT_URI,
                NewsgroupContract.NewsgroupEntry.COL_FK_SERV_ID + " = ? ", new String[]{serverId + ""});
    }

}
