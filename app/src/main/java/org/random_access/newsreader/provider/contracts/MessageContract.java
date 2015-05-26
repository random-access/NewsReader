package org.random_access.newsreader.provider.contracts;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import org.random_access.newsreader.provider.NNTPProvider;

/**
 * Project: FlashCards Manager for Android
 * Date: 17.05.15
 * Author: Monika Schrenk
 * E-Mail: software@random-access.org
 */
public class MessageContract {
    private static final String TAG = ServerContract.class.getSimpleName();

    public static final String TABLE_NAME = "_TBL_MESSAGES";

    public static final Uri CONTENT_URI = Uri.parse("content://" + NNTPProvider.AUTHORITY + "/" + TABLE_NAME);

    // prevent instantiation
    private MessageContract(){}

    /**
     * Table name: _TBL_MESSAGES
     * <br>
     * Columns:
     * <ul>
     *      <li>_ID: int, PK, AI -> inherited from BaseColumns</li>
     *      <li>_MSG_ID: int NN-> message id string</li>
     *      <li>_SUBJECT: text NN-> message subject</li>
     *      <li>_DATE: integer jjjjmmtthhmmss NN -> message creation date & time</li>
     *      <li>_TIMEZONE: integer +/-zz00 NN -> time zone</li>
     *      <li>_NEW: integer {0,1} NN -> message read by user</li>
     *      <li>_IN_REPLY_TO: int -> references _TBL_MESSAGES._ID</li>
     *      <li>_FK_N_ID: int -> references _TBL_NEWSGROUPS._ID</li>
     * </ul>
     */
    public static abstract class MessageEntry implements BaseColumns {

        public static final String COL_MSG_ID = "_MSG_ID";
        public static final String COL_SUBJECT = "_SUBJECT";
        public static final String COL_CHARSET = "_CHARSET";
        public static final String COL_DATE = "_DATE";
        public static final String COL_TIMEZONE = "_TIMEZONE";

        public static final String COL_NEW = "_READ";
        public static final String COL_IN_REPLY_TO = "_IN_REPLY_TO";
        public static final String COL_FK_N_ID = "_FK_N_ID";

        public static final String COL_ID_FULLNAME = TABLE_NAME + "." + _ID;
        public static final String COL_MSG_ID_FULLNAME = TABLE_NAME + "." + COL_MSG_ID;
        public static final String COL_CHARSET_FULLNAME = TABLE_NAME + "." + COL_CHARSET;
        public static final String COL_SUBJECT_FULLNAME = TABLE_NAME + "." + COL_SUBJECT;
        public static final String COL_DATE_FULLNAME = TABLE_NAME + "." + COL_DATE;
        public static final String COL_TIMEZONE_FULLNAME = TABLE_NAME + "." + COL_TIMEZONE;
        public static final String COL_NEW_FULLNAME = TABLE_NAME + "." + COL_NEW;
        public static final String COL_IN_REPLY_TO_FULLNAME = TABLE_NAME + "." + COL_IN_REPLY_TO;
        public static final String COL_FK_N_ID_FULLNAME = TABLE_NAME + "." + COL_FK_N_ID;
    }

    private static final String DATABASE_CREATE = "create table if not exists "
            + TABLE_NAME
            + "("
            + MessageEntry._ID + " integer primary key autoincrement, "
            + MessageEntry.COL_MSG_ID + " text not null, "
            + MessageEntry.COL_CHARSET + " text not null, "
            + MessageEntry.COL_SUBJECT + " text not null, "
            + MessageEntry.COL_DATE + " integer not null, "
            + MessageEntry.COL_TIMEZONE + " string not null, "
            + MessageEntry.COL_NEW + " integer not null, "
            + MessageEntry.COL_IN_REPLY_TO + " integer, "
            + MessageEntry.COL_FK_N_ID + " integer, "
            + "foreign key (" + MessageEntry.COL_IN_REPLY_TO + ") references "
            +  MessageContract.TABLE_NAME + " (" + MessageEntry._ID + "), "
            + "foreign key (" + MessageEntry.COL_FK_N_ID + ") references "
            +  NewsgroupContract.TABLE_NAME + " (" + NewsgroupContract.NewsgroupEntry._ID + ")"
            + ");";

    public static void onCreate (SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
        Log.d(TAG, DATABASE_CREATE);
    }

    public static void onUpdate(SQLiteDatabase db, int oldVersion, int newVersion) {
        // add upgrade procedure if necessary
    }

}
