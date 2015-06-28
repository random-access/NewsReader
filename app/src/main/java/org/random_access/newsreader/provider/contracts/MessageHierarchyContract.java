package org.random_access.newsreader.provider.contracts;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import org.random_access.newsreader.provider.NNTPProvider;

/**
 * <b>Project:</b> FlashCards Manager for Android <br>
 * <b>Date:</b> 28.06.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class MessageHierarchyContract {

    private static final String TAG = MessageHierarchyContract.class.getSimpleName();

    public static final String TABLE_NAME = "_TBL_MESSAGE_HIERARCHY";

    public static final Uri CONTENT_URI = Uri.parse("content://" + NNTPProvider.AUTHORITY + "/" + TABLE_NAME);

    // prevent instantiation
    private MessageHierarchyContract(){}

    /**
     * Table name: _TBL_MESSAGES
     * <br>
     * Columns:
     * <ul>
     *      <li>_ID: int, PK, AI -> inherited from BaseColumns</li>
     *      <li>_MSG_DB_ID: int NN-> message id string -> references _TBL_MESSAGES._ID</li>
     *      <li>_IN_REPLY_TO: text NN-> message subject -> references _TBL_MESSAGES._ID</li>
     * </ul>
     */
    public static abstract class MessageHierarchyEntry implements BaseColumns {

        public static final String COL_MSG_DB_ID = "_MSG_ID";
        public static final String COL_IN_REPLY_TO = "_IN_REPLY_TO";

        public static final String COL_ID_FULLNAME = TABLE_NAME + "." + _ID;
        public static final String COL_MSG_DB_ID_FULLNAME = TABLE_NAME + "." + COL_MSG_DB_ID;
        public static final String COL_IN_REPLY_TO_FULLNAME = TABLE_NAME + "." + COL_IN_REPLY_TO;
    }

    private static final String DATABASE_CREATE = "create table if not exists "
            + TABLE_NAME
            + "("
            + MessageHierarchyEntry._ID + " integer primary key autoincrement, "
            + MessageHierarchyEntry.COL_MSG_DB_ID + " integer not null, "
            + MessageHierarchyEntry.COL_IN_REPLY_TO + " integer not null, "
            + "foreign key (" + MessageHierarchyEntry.COL_MSG_DB_ID + ") references "
            +  MessageContract.TABLE_NAME + " (" + MessageContract.MessageEntry._ID + "), "
            + "foreign key (" + MessageHierarchyEntry.COL_IN_REPLY_TO + ") references "
            + MessageContract.TABLE_NAME + " (" + MessageContract.MessageEntry._ID + ")"
            + ");";

    public static void onCreate (SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
        Log.d(TAG, DATABASE_CREATE);
    }

    @SuppressWarnings("EmptyMethod")
    public static void onUpdate(SQLiteDatabase db, int oldVersion, int newVersion) {
        // add upgrade procedure if necessary
    }
}
