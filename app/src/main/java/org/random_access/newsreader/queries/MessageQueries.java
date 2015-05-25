package org.random_access.newsreader.queries;

import android.content.Context;
import android.database.Cursor;

import org.random_access.newsreader.provider.contracts.MessageContract;

/**
 * Project: FlashCards Manager for Android
 * Date: 18.05.15
 * Author: Monika Schrenk
 * E-Mail: software@random-access.org
 */
public class MessageQueries {

    private Context context;

    private static final String[] PROJECTION_MESSAGE = new String[] {MessageContract.MessageEntry._ID, MessageContract.MessageEntry.COL_MSG_ID,
            MessageContract.MessageEntry.COL_FK_N_ID, MessageContract.MessageEntry.COL_DATE };

    public static final int COL_ID = 1;
    public static final int COL_MSG_ID = 2;
    public static final int COL_FK_N_ID = 3;
    public static final int COL_DATE = 4;

    public MessageQueries(Context context) {
        this.context = context;
    }


    public Cursor getCursorToYoungestMessage(long newsgroupId) {
        return context.getContentResolver().query
                (MessageContract.CONTENT_URI, PROJECTION_MESSAGE, MessageContract.MessageEntry.COL_FK_N_ID + " = ?", new String[] {newsgroupId + ""},
                        MessageContract.MessageEntry.COL_DATE + " DESC");
    }
}
