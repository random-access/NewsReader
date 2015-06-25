package org.random_access.newsreader.provider.contracts;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import org.random_access.newsreader.provider.NNTPProvider;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.07.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class NewsgroupContract {

    private static final String TAG = ServerContract.class.getSimpleName();

    public static final String TABLE_NAME = "_TBL_NEWSGROUPS";
    public static final Uri CONTENT_URI = Uri.parse("content://" + NNTPProvider.AUTHORITY + "/" + TABLE_NAME);

    // prevent instantiation
    private NewsgroupContract(){}

    /**
     * Table name: _TBL_NEWSGROUPS
     * <br>
     * Columns:
     * <ul>
     *      <li>_ID: integer, PK, AI -> inherited from BaseColumns</li>
     *      <li>_NAME: text NN -> newsgroup name</li>
     *      <li>_FK_SERV_ID: int -> references _TBL_SERVER._ID</li>
     * </ul>
     */
    public static abstract class NewsgroupEntry implements BaseColumns {
        
        public static final String COL_NAME = "_NAME";
        public static final String COL_TITLE = "_TITLE";
        public static final String COL_LAST_SYNC_DATE = "_LAST_SYNC_DATE";
        public static final String COL_FK_SERV_ID= "_FK_SERV_ID";

        public static final String COL_ID_FULLNAME = TABLE_NAME + "." + _ID;
        public static final String COL_NAME_FULLNAME = TABLE_NAME + "." + COL_NAME;
        public static final String COL_TITLE_FULLNAME = TABLE_NAME + "." + COL_TITLE;
        public static final String COL_LAST_SYNC_DATE_FULLNAME = TABLE_NAME + "." + COL_LAST_SYNC_DATE;
        public static final String COL_FK_SERV_ID_FULLNAME = TABLE_NAME + "." + COL_FK_SERV_ID;
    }

    private static final String DATABASE_CREATE = "create table if not exists "
            + TABLE_NAME
            + "("
            + NewsgroupEntry._ID + " integer primary key autoincrement, "
            + NewsgroupEntry.COL_NAME + " text not null, "
            + NewsgroupEntry.COL_TITLE + " text, "
            + NewsgroupEntry.COL_LAST_SYNC_DATE + " integer not null, "
            + NewsgroupEntry.COL_FK_SERV_ID + " integer not null, "
            + "foreign key (" + NewsgroupEntry.COL_FK_SERV_ID + ") references "
            +  ServerContract.TABLE_NAME+ " (" + ServerContract.ServerEntry._ID + ")"
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
