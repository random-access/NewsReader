package org.random_access.newsreader.provider.contracts;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import org.random_access.newsreader.provider.NNTPProvider;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 18.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class SettingsContract {
    private static final String TAG = ServerContract.class.getSimpleName();

    public static final String TABLE_NAME = "_TBL_SETTINGS";
    public static final Uri CONTENT_URI = Uri.parse("content://" + NNTPProvider.AUTHORITY + "/" + TABLE_NAME);

    // prevent instantiation
    private SettingsContract(){}

    /**
     * Table name: _TBL_SETTINGS
     * <br>
     * Columns:
     * <ul>
     *      <li>_ID: int, PK, AI -> inherited from BaseColumns</li>
     *      <li>_FULLNAME: text -> name for message header</li>
     *      <li>_EMAIL: text -> email for message header</li>
     *      <li>_SIGNATURE: text -> optional signature for message header</li>
     *      <li>_MSG_KEEP_NO: integer NN -> keep the last [x] messages</li>
     *      <li>_MSG_KEEP_DATE: integer NN -> keep all messages since [x]></li>
     *      <li>_MSG_UPDATE_INTERVAL: integer NN -> update messages all [x] minutes (0 - at every app start)</li>
     * </ul>
     */
    public static abstract class SettingsEntry implements BaseColumns {

        public static final String COL_NAME = "_NAME";
        public static final String COL_EMAIL = "_EMAIL";
        public static final String COL_SIGNATURE = "_SIGNATURE";
        public static final String COL_MSG_KEEP_NO = "_MSG_KEEP_NO";
        public static final String COL_MSG_KEEP_DATE = "_MSG_KEEP_DATE";

        public static final String COL_ID_FULLNAME = TABLE_NAME + "." + _ID;
        public static final String COL_NAME_FULLNAME = TABLE_NAME + "." + "_NAME";
        public static final String COL_EMAIL_FULLNAME = TABLE_NAME + "." + "_EMAIL";
        public static final String COL_SIGNATURE_FULLNAME = TABLE_NAME + "." + "_SIGNATURE";
        public static final String COL_MSG_KEEP_NO_FULLNAME = TABLE_NAME + "." +"_MSG_KEEP_NO";
        public static final String COL_MSG_KEEP_DATE_FULLNAME = TABLE_NAME + "." +"_MSG_KEEP_DATE";
    }

    private static final String DATABASE_CREATE = "create table if not exists "
            + TABLE_NAME
            + "("
            + SettingsEntry._ID + " integer primary key autoincrement, "
            + SettingsEntry.COL_NAME + " text not null, "
            + SettingsEntry.COL_EMAIL + " text not null, "
            + SettingsEntry.COL_SIGNATURE + " text, "
            + SettingsEntry.COL_MSG_KEEP_NO + " integer not null, "
            + SettingsEntry.COL_MSG_KEEP_DATE + " integer not null"
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
