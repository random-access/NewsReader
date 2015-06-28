package org.random_access.newsreader.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import org.apache.http.auth.AUTH;
import org.random_access.newsreader.provider.contracts.MessageContract;
import org.random_access.newsreader.provider.contracts.MessageHierarchyContract;
import org.random_access.newsreader.provider.contracts.NewsgroupContract;
import org.random_access.newsreader.provider.contracts.ServerContract;
import org.random_access.newsreader.provider.contracts.SettingsContract;

import java.util.Arrays;
import java.util.HashMap;


/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.07.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class NNTPProvider extends ContentProvider {

    public static final String AUTHORITY = "org.random_access.newsreader.provider";

    private static final String MIME_BASETYPE_TABLE = "vnd.android.cursor.dir";
    private static final String MIME_BASETYPE_ROW = "vnd.android.cursor.item";

    private NewsDBOpenHelper newsDBOpenHelper;

    // ID's for URI matcher
    // TABLES starting with 1000
    private static final int SETTINGS_TABLE = 1000;
    private static final int SERVER_TABLE = 1001;
    private static final int NEWSGROUP_TABLE = 1002;
    private static final int MESSAGE_TABLE = 1003;
    private static final int MESSAGE_HIERARCHY_TABLE = 1004;

    // ROWS starting with 2000
    private static final int SETTINGS_ROW = 2000;
    private static final int SERVER_ROW = 2001;
    private static final int NEWSGROUP_ROW = 2002;
    private static final int MESSAGE_ROW = 2003;
    private static final int MESSAGE_HIERARCHY_ROW = 2004;

    private static final HashMap<String, String> PROJECTION_MAP_SETTINGS;
    private static final HashMap<String, String> PROJECTION_MAP_SERVER;
    private static final HashMap<String, String> PROJECTION_MAP_NEWSGROUP;
    private static final HashMap<String, String> PROJECTION_MAP_MESSAGE;
    private static final HashMap<String, String> PROJECTION_MAP_MESSAGE_HIERARCHY;

    static {
        PROJECTION_MAP_SETTINGS = new HashMap<>();
        PROJECTION_MAP_SETTINGS.put(SettingsContract.SettingsEntry._ID, SettingsContract.SettingsEntry.COL_ID_FULLNAME);
        PROJECTION_MAP_SETTINGS.put(SettingsContract.SettingsEntry.COL_NAME, SettingsContract.SettingsEntry.COL_NAME_FULLNAME);
        PROJECTION_MAP_SETTINGS.put(SettingsContract.SettingsEntry.COL_EMAIL, SettingsContract.SettingsEntry.COL_EMAIL_FULLNAME);
        PROJECTION_MAP_SETTINGS.put(SettingsContract.SettingsEntry.COL_SIGNATURE, SettingsContract.SettingsEntry.COL_SIGNATURE_FULLNAME);
        PROJECTION_MAP_SETTINGS.put(SettingsContract.SettingsEntry.COL_MSG_KEEP_NO, SettingsContract.SettingsEntry.COL_MSG_KEEP_NO_FULLNAME);

        PROJECTION_MAP_SERVER = new HashMap<>();
        PROJECTION_MAP_SERVER.put(ServerContract.ServerEntry._ID, ServerContract.ServerEntry.COL_ID_FULLNAME);
        PROJECTION_MAP_SERVER.put(ServerContract.ServerEntry.COL_SERVERNAME, ServerContract.ServerEntry.COL_SERVERNAME_FULLNAME);
        PROJECTION_MAP_SERVER.put(ServerContract.ServerEntry.COL_TITLE, ServerContract.ServerEntry.COL_TITLE_FULLNAME);
        PROJECTION_MAP_SERVER.put(ServerContract.ServerEntry.COL_ENCRYPTION, ServerContract.ServerEntry.COL_ENCRYPTION_FULLNAME);
        PROJECTION_MAP_SERVER.put(ServerContract.ServerEntry.COL_SERVERPORT, ServerContract.ServerEntry.COL_SERVERPORT_FULLNAME);
        PROJECTION_MAP_SERVER.put(ServerContract.ServerEntry.COL_AUTH, ServerContract.ServerEntry.COL_AUTH_FULLNAME);
        PROJECTION_MAP_SERVER.put(ServerContract.ServerEntry.COL_USER, ServerContract.ServerEntry.COL_USER_FULLNAME);
        PROJECTION_MAP_SERVER.put(ServerContract.ServerEntry.COL_PASSWORD, ServerContract.ServerEntry.COL_PASSWORD_FULLNAME);
        PROJECTION_MAP_SERVER.put(ServerContract.ServerEntry.COL_FK_SET_ID, ServerContract.ServerEntry.COL_FK_SET_ID_FULLNAME);

        PROJECTION_MAP_NEWSGROUP = new HashMap<>();
        PROJECTION_MAP_NEWSGROUP.put(NewsgroupContract.NewsgroupEntry._ID, NewsgroupContract.NewsgroupEntry.COL_ID_FULLNAME);
        PROJECTION_MAP_NEWSGROUP.put(NewsgroupContract.NewsgroupEntry.COL_NAME, NewsgroupContract.NewsgroupEntry.COL_NAME_FULLNAME);
        PROJECTION_MAP_NEWSGROUP.put(NewsgroupContract.NewsgroupEntry.COL_TITLE, NewsgroupContract.NewsgroupEntry.COL_TITLE_FULLNAME);
        PROJECTION_MAP_NEWSGROUP.put(NewsgroupContract.NewsgroupEntry.COL_FK_SERV_ID, NewsgroupContract.NewsgroupEntry.COL_FK_SERV_ID_FULLNAME);
        PROJECTION_MAP_NEWSGROUP.put(NewsgroupContract.NewsgroupEntry.COL_LAST_SYNC_DATE, NewsgroupContract.NewsgroupEntry.COL_LAST_SYNC_DATE_FULLNAME);

        PROJECTION_MAP_MESSAGE = new HashMap<>();
        PROJECTION_MAP_MESSAGE.put(MessageContract.MessageEntry._ID, MessageContract.MessageEntry.COL_ID_FULLNAME);
        PROJECTION_MAP_MESSAGE.put(MessageContract.MessageEntry.COL_MSG_ID, MessageContract.MessageEntry.COL_MSG_ID_FULLNAME);
        PROJECTION_MAP_MESSAGE.put(MessageContract.MessageEntry.COL_FROM_NAME, MessageContract.MessageEntry.COL_FROM_NAME_FULLNAME);
        PROJECTION_MAP_MESSAGE.put(MessageContract.MessageEntry.COL_FROM_EMAIL, MessageContract.MessageEntry.COL_FROM_EMAIL_FULLNAME);
        PROJECTION_MAP_MESSAGE.put(MessageContract.MessageEntry.COL_CHARSET, MessageContract.MessageEntry.COL_CHARSET_FULLNAME);
        PROJECTION_MAP_MESSAGE.put(MessageContract.MessageEntry.COL_SUBJECT, MessageContract.MessageEntry.COL_SUBJECT_FULLNAME);
        PROJECTION_MAP_MESSAGE.put(MessageContract.MessageEntry.COL_DATE, MessageContract.MessageEntry.COL_DATE_FULLNAME);
        PROJECTION_MAP_MESSAGE.put(MessageContract.MessageEntry.COL_NEW, MessageContract.MessageEntry.COL_NEW_FULLNAME);
        PROJECTION_MAP_MESSAGE.put(MessageContract.MessageEntry.COL_FK_N_ID, MessageContract.MessageEntry.COL_FK_N_ID_FULLNAME);
        PROJECTION_MAP_MESSAGE.put(MessageContract.MessageEntry.COL_HEADER, MessageContract.MessageEntry.COL_HEADER_FULLNAME);
        PROJECTION_MAP_MESSAGE.put(MessageContract.MessageEntry.COL_BODY, MessageContract.MessageEntry.COLL_BODY_FULLNAME);

        PROJECTION_MAP_MESSAGE_HIERARCHY = new HashMap<>();
        PROJECTION_MAP_MESSAGE_HIERARCHY.put(MessageHierarchyContract.MessageHierarchyEntry._ID, MessageHierarchyContract.MessageHierarchyEntry.COL_ID_FULLNAME);
        PROJECTION_MAP_MESSAGE_HIERARCHY.put(MessageHierarchyContract.MessageHierarchyEntry.COL_MSG_DB_ID, MessageHierarchyContract.MessageHierarchyEntry.COL_MSG_DB_ID_FULLNAME);
        PROJECTION_MAP_MESSAGE_HIERARCHY.put(MessageHierarchyContract.MessageHierarchyEntry.COL_IN_REPLY_TO, MessageHierarchyContract.MessageHierarchyEntry.COL_IN_REPLY_TO_FULLNAME);
    }

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, SettingsContract.TABLE_NAME, SETTINGS_TABLE);
        uriMatcher.addURI(AUTHORITY, ServerContract.TABLE_NAME, SERVER_TABLE);
        uriMatcher.addURI(AUTHORITY, NewsgroupContract.TABLE_NAME, NEWSGROUP_TABLE);
        uriMatcher.addURI(AUTHORITY, MessageContract.TABLE_NAME, MESSAGE_TABLE);
        uriMatcher.addURI(AUTHORITY, MessageHierarchyContract.TABLE_NAME, MESSAGE_HIERARCHY_TABLE);

        uriMatcher.addURI(AUTHORITY, SettingsContract.TABLE_NAME + "/#", SETTINGS_ROW);
        uriMatcher.addURI(AUTHORITY, ServerContract.TABLE_NAME + "/#", SERVER_ROW);
        uriMatcher.addURI(AUTHORITY, NewsgroupContract.TABLE_NAME + "/#", NEWSGROUP_ROW);
        uriMatcher.addURI(AUTHORITY, MessageContract.TABLE_NAME + "/#", MESSAGE_ROW);
        uriMatcher.addURI(AUTHORITY, MessageHierarchyContract.TABLE_NAME + "/#", MESSAGE_HIERARCHY_ROW);
    }

    @Override
    public boolean onCreate() {
        newsDBOpenHelper = new NewsDBOpenHelper(getContext());
        return false;
    }

    @Override
    public String getType(Uri uri) {
        int uriCode = uriMatcher.match(uri);
        switch(uriCode) {
            case SETTINGS_ROW:
                return MIME_BASETYPE_ROW + "/" + SettingsContract.TABLE_NAME;
            case SETTINGS_TABLE:
                return MIME_BASETYPE_TABLE + "/" + SettingsContract.TABLE_NAME;
            case SERVER_ROW:
                return MIME_BASETYPE_ROW + "/" + ServerContract.TABLE_NAME;
            case SERVER_TABLE:
                return MIME_BASETYPE_TABLE + "/" + ServerContract.TABLE_NAME;
            case NEWSGROUP_ROW:
                return MIME_BASETYPE_ROW + "/" + NewsgroupContract.TABLE_NAME;
            case NEWSGROUP_TABLE:
                return MIME_BASETYPE_TABLE + "/" + NewsgroupContract.TABLE_NAME;
            case MESSAGE_ROW:
                return MIME_BASETYPE_ROW + "/" + MessageContract.TABLE_NAME;
            case MESSAGE_TABLE:
                return MIME_BASETYPE_TABLE + "/" + MessageContract.TABLE_NAME;
            case MESSAGE_HIERARCHY_ROW:
                return MIME_BASETYPE_ROW + "/" + MessageHierarchyContract.TABLE_NAME;
            case MESSAGE_HIERARCHY_TABLE:
                return MIME_BASETYPE_TABLE + "/" + MessageHierarchyContract.TABLE_NAME;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uriCode);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        int uriCode = uriMatcher.match(uri);
        String tableName = getTableName(uriCode);
        HashMap<String,String> pMap = getProjections(uriCode);
        queryBuilder.setTables(tableName);
        queryBuilder.setProjectionMap(pMap);
        checkColumnProjection(projection);
        String itemId = getTableIdColumn(uriCode);
        if (itemId != null) {
            queryBuilder.appendWhere(itemId + "="
                    + uri.getLastPathSegment()); // TODO this is not safe!
        }
        SQLiteDatabase db = newsDBOpenHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        // notify listeners
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase sqlDB = newsDBOpenHelper.getWritableDatabase();
        int uriCode = uriMatcher.match(uri);
        String tableName = getTableName(uriCode);
        long id = sqlDB.insert(tableName, null, values);
        // notify observers
        getContext().getContentResolver().notifyChange(uri, null);
        return  Uri.parse(tableName + "/" + id);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase sqlDB = newsDBOpenHelper.getWritableDatabase();
        int numberOfUpdates;
        int uriCode = uriMatcher.match(uri);
        String tableName = getTableName(uriCode);
        String itemId = getTableIdColumn(uriCode);
        if (itemId == null) {
            numberOfUpdates = sqlDB.update(tableName, values, selection, selectionArgs);
        } else {
            String id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)) {
                numberOfUpdates = sqlDB.update(tableName, values, itemId + " = ? ", new String[]{id});
            } else {
                if (selectionArgs == null || selectionArgs.length == 0) {
                    numberOfUpdates = sqlDB.update(tableName, values, itemId + " = ?", new String[]{id});
                } else {
                    numberOfUpdates = sqlDB.update(tableName, values, selection + " AND " + itemId + " = ?",
                            buildSelectionArgs(selectionArgs, id));
                }
            }
        }
        //notify observers
        getContext().getContentResolver().notifyChange(uri, null);
        return numberOfUpdates;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase sqlDB = newsDBOpenHelper.getWritableDatabase();
        int numberOfDeletions;
        int uriCode = uriMatcher.match(uri);
        String tableName = getTableName(uriCode);
        String itemId = getTableIdColumn(uriCode);
        if (itemId == null ) {
            numberOfDeletions = sqlDB.delete(tableName, selection, selectionArgs);
        } else {
            String id = uri.getLastPathSegment();
            if (selectionArgs == null || selectionArgs.length == 0) {
                numberOfDeletions = sqlDB.delete(tableName, itemId + " = ?", new String[]{id});
            } else {
                numberOfDeletions = sqlDB.delete(tableName, selection + " AND " + itemId + " = ?",
                        buildSelectionArgs(selectionArgs, id));
            }
        }
        // notify potential observers
        getContext().getContentResolver().notifyChange(uri,null);
        return numberOfDeletions;
    }

    private String[] buildSelectionArgs(String[] originalArgs, String id) {
        String[] newArgs = new String[originalArgs.length + 1];
        Arrays.copyOfRange(originalArgs, 0, originalArgs.length);
        newArgs[newArgs.length-1] = id;
        return newArgs;
    }


    /**
     * Helper method to get the name of the table associated with the matched uriCode, if a valid Uri was given
     * @param uriCode code returned from URI matcher
     * @return name of the table associated with the matched uriCode
     * @throws IllegalArgumentException if we didn't get a request with a valid Uri
     */
    private String getTableName(int uriCode) {
        switch(uriCode) {
            case SETTINGS_TABLE:
            case SETTINGS_ROW:
                return SettingsContract.TABLE_NAME;
            case SERVER_TABLE:
            case SERVER_ROW:
                return ServerContract.TABLE_NAME;
            case NEWSGROUP_TABLE:
            case NEWSGROUP_ROW:
                return NewsgroupContract.TABLE_NAME;
            case MESSAGE_TABLE:
            case MESSAGE_ROW:
                return MessageContract.TABLE_NAME;
            case MESSAGE_HIERARCHY_TABLE:
            case MESSAGE_HIERARCHY_ROW:
                return MessageHierarchyContract.TABLE_NAME;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uriCode);
        }
    }

    /**
     * Helper method to get the name of the ID column of the table associated with the matched uriCode,
     * if the Uri specifies this
     * @param uriCode code returned from Uri matcher
     * @return column name for table associated with the given uriCode (currently always "_ID"), else null
     */
    private String getTableIdColumn(int uriCode) {
        switch(uriCode){
            case SETTINGS_ROW:
                return SettingsContract.SettingsEntry._ID;
            case SERVER_ROW:
                return ServerContract.ServerEntry._ID;
            case NEWSGROUP_ROW:
                return NewsgroupContract.NewsgroupEntry._ID;
            case MESSAGE_ROW:
                return MessageContract.MessageEntry._ID;
            case MESSAGE_HIERARCHY_ROW:
                return MessageHierarchyContract.MessageHierarchyEntry._ID;
            default:
                return null;
        }
    }

    /**
     * Helper method to get the projection map associated with the matched uriCode, if a valid Uri was given
     * @param uriCode code returned from URI matcher
     * @return projection map that maps all column requests to TableName.column
     * @throws IllegalArgumentException if we didn't get a request with a valid Uri
     */
    private HashMap<String,String> getProjections(int uriCode) {
        switch (uriCode) {
            case SETTINGS_TABLE:
            case SETTINGS_ROW:
                return PROJECTION_MAP_SETTINGS;
            case SERVER_TABLE:
            case SERVER_ROW:
                return PROJECTION_MAP_SERVER;
            case NEWSGROUP_TABLE:
            case NEWSGROUP_ROW:
                return PROJECTION_MAP_NEWSGROUP;
            case MESSAGE_TABLE:
            case MESSAGE_ROW:
                return PROJECTION_MAP_MESSAGE;
            case MESSAGE_HIERARCHY_TABLE:
            case MESSAGE_HIERARCHY_ROW:
                return PROJECTION_MAP_MESSAGE_HIERARCHY;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uriCode);
        }
    }


    @SuppressWarnings("EmptyMethod")
    private void checkColumnProjection(String[] projection) {
        // TODO check if requested columns in selection are valid
    }
}
