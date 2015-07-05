package org.random_access.newsreader.queries;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.random_access.newsreader.provider.contracts.MessageHierarchyContract;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 28.06.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class MessageHierarchyQueries {

    private static final String TAG = MessageHierarchyQueries.class.getSimpleName();

    private Context context;

    private String[] MSG_HIERARCHY_PROJECTION = new String[] {MessageHierarchyContract.MessageHierarchyEntry._ID, MessageHierarchyContract.MessageHierarchyEntry.COL_MSG_DB_ID,
    MessageHierarchyContract.MessageHierarchyEntry.COL_IN_REPLY_TO};

    public static final int COL_ID = 0;
    public static final int COL_MSG_DB_ID = 1;
    public static final int COL_IN_REPLY_TO = 2;

    public MessageHierarchyQueries(Context context) {
        this.context = context;
    }

    public boolean hasMessageChildren(long messageId) {
        boolean hasChildren = false;

        Cursor cursor = context.getContentResolver().query(MessageHierarchyContract.CONTENT_URI, MSG_HIERARCHY_PROJECTION, MessageHierarchyContract.MessageHierarchyEntry.COL_IN_REPLY_TO + " = ? ",
                new String[] {messageId + ""}, null);
        if (cursor.moveToFirst()) {
            hasChildren = true;
        }
        cursor.close();
        Log.d(TAG, "Message " + messageId + " has children: " + hasChildren);
        return hasChildren;
    }

    public Cursor getChildrenOfId(long msgId) {
        return context.getContentResolver().query(MessageHierarchyContract.CONTENT_URI, MSG_HIERARCHY_PROJECTION,
                MessageHierarchyContract.MessageHierarchyEntry.COL_MSG_DB_ID + " = ?", new String[]{msgId + ""},
                null);
    }

    public String getChildrenListAsString() {
        Cursor c = context.getContentResolver().query(MessageHierarchyContract.CONTENT_URI, MSG_HIERARCHY_PROJECTION, null, null,
                MessageHierarchyContract.MessageHierarchyEntry.COL_MSG_DB_ID + " ASC");
        StringBuilder sb = new StringBuilder("(");
        long lastId = 0;
        if (c.moveToFirst()) {
            while(!c.isAfterLast()) {
                long currentId = c.getLong(COL_MSG_DB_ID);
                if (lastId != currentId) {
                    sb.append(currentId).append(",");
                    lastId = currentId;
                }
                c.moveToNext();
            }
        }
        c.close();
        if (sb.length() > 1) {
            sb.setCharAt(sb.lastIndexOf(","), ')');
        } else {
            sb.append(")");
        }
        return sb.toString();
    }

    /**
     * Deletes all message hierarchy entries from a given message id
     * @param messageId a given message id
     */
    public void deleteEntriesFromMessageIds(long messageId) {
        int delCount = context.getContentResolver().delete(MessageHierarchyContract.CONTENT_URI, MessageHierarchyContract.MessageHierarchyEntry.COL_MSG_DB_ID + " = ? ", new String[]{messageId + ""});
        Log.i(TAG, delCount + " rows deleted");
    }

}
