package org.random_access.newsreader.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.random_access.newsreader.provider.contracts.*;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.06.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
class NewsDBOpenHelper extends SQLiteOpenHelper{


    private static final String TAG = NewsDBOpenHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "NEWSREADER.db";

    public NewsDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates all databases if not existing
     * @param db the sqlite database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        SettingsContract.onCreate(db);
        ServerContract.onCreate(db);
        NewsgroupContract.onCreate(db);
        MessageContract.onCreate(db);
        MessageHierarchyContract.onCreate(db);
        Log.d(TAG, "Finished onCreate in NewsDBOpenHelper");
    }

    /**
     * Starts update routines in all tables
     * @param db the sqlite database
     * @param oldVersion current version of database
     * @param newVersion new version of database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        SettingsContract.onUpdate(db, oldVersion, newVersion);
        ServerContract.onUpdate(db, oldVersion, newVersion);
        NewsgroupContract.onUpdate(db, oldVersion, newVersion);
        MessageContract.onUpdate(db, oldVersion, newVersion);
        MessageHierarchyContract.onUpdate(db, oldVersion, newVersion);
        Log.d(TAG, "Finished onUpdate in NewsDBOpenHelper");
    }
}
