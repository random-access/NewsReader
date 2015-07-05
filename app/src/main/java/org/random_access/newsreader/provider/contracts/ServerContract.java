package org.random_access.newsreader.provider.contracts;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import org.random_access.newsreader.provider.NNTPProvider;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.06.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class ServerContract {

    private static final String TAG = ServerContract.class.getSimpleName();

    public static final String TABLE_NAME = "_TBL_SERVER";
    public static final Uri CONTENT_URI = Uri.parse("content://" + NNTPProvider.AUTHORITY + "/" + TABLE_NAME);

    // prevent instantiation
    private ServerContract(){}

    /**
     * Table name: _TBL_SERVER
     * <br>
     * Columns:
     * <ul>
     *      <li>_ID: int, PK, AI -> inherited from BaseColumns</li>
     *      <li>_SERVERNAME: text NN -> server ip / hostname</li>
     *      <li>_TITLE: text </li>
     *      <li>_ENCRYPTION: integer {0,1} -> SSL / no SSL</li>
     *      <li>_USER: text  NN-> username for newsserver</li>
     *      <li>_PASSWORD: text -> password for newsserver</li>
     *      <li>_FK_SET_ID: integer, references _TBL_SETTINGS._ID</li>
     * </ul>
     */
    public static abstract class ServerEntry implements BaseColumns {

        public static final String COL_TITLE = "_TITLE";
        public static final String COL_SERVERNAME = "_SERVERNAME";
        public static final String COL_SERVERPORT = "_SERVERPORT";
        public static final String COL_ENCRYPTION = "_ENCRYPTION";
        public static final String COL_AUTH = "_AUTH";
        public static final String COL_USER = "_USER";
        public static final String COL_PASSWORD = "_PASSWORD";
        public static final String COL_FK_SET_ID = "FK_SET_ID";

        public static final String COL_ID_FULLNAME = TABLE_NAME + "." + _ID;
        public static final String COL_TITLE_FULLNAME = TABLE_NAME + "." +"_TITLE";
        public static final String COL_SERVERNAME_FULLNAME = TABLE_NAME + "." +"_SERVERNAME";
        public static final String COL_SERVERPORT_FULLNAME = TABLE_NAME + "." + "_SERVERPORT";
        public static final String COL_ENCRYPTION_FULLNAME = TABLE_NAME + "." +"_ENCRYPTION";
        public static final String COL_AUTH_FULLNAME = TABLE_NAME + "." + "_AUTH";
        public static final String COL_USER_FULLNAME = TABLE_NAME + "." +"_USER";
        public static final String COL_PASSWORD_FULLNAME = TABLE_NAME + "." + "_PASSWORD";
        public static final String COL_FK_SET_ID_FULLNAME = TABLE_NAME + "." + "FK_SET_ID";
    }

    private static final String DATABASE_CREATE = "create table if not exists "
            + TABLE_NAME
            + "("
            + ServerEntry._ID + " integer primary key autoincrement, "
            + ServerEntry.COL_TITLE + " text, "
            + ServerEntry.COL_SERVERNAME + " text not null, "
            + ServerEntry.COL_SERVERPORT + " integer, "
            + ServerEntry.COL_ENCRYPTION + " integer, "
            + ServerEntry.COL_AUTH + " integer not null, "
            + ServerEntry.COL_USER + " text, "
            + ServerEntry.COL_PASSWORD + " text, "
            + ServerEntry.COL_FK_SET_ID + " integer not null, "
            + "foreign key (" + ServerEntry.COL_FK_SET_ID + ") references "
            +  SettingsContract.TABLE_NAME + " (" + SettingsContract.SettingsEntry._ID + ")"
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
