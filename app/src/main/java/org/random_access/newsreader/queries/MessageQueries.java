package org.random_access.newsreader.queries;

import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.util.Log;

import org.random_access.newsreader.provider.contracts.MessageContract;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.06.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class MessageQueries {

    private static final String TAG = MessageQueries.class.getSimpleName();
    private final Context context;

    private static final String[] PROJECTION_MESSAGE = new String[] {MessageContract.MessageEntry._ID, MessageContract.MessageEntry.COL_MSG_ID,
            MessageContract.MessageEntry.COL_FROM_EMAIL, MessageContract.MessageEntry.COL_FROM_NAME, MessageContract.MessageEntry.COL_SUBJECT,
            MessageContract.MessageEntry.COL_CHARSET, MessageContract.MessageEntry.COL_DATE,MessageContract.MessageEntry.COL_NEW,
            MessageContract.MessageEntry.COL_FK_N_ID, MessageContract.MessageEntry.COL_HEADER, MessageContract.MessageEntry.COL_BODY,
            MessageContract.MessageEntry.COL_LEFT_VALUE, MessageContract.MessageEntry.COL_RIGHT_VALUE, MessageContract.MessageEntry.COL_LEVEL,
            MessageContract.MessageEntry.COL_ROOT_MSG, MessageContract.MessageEntry.COL_REFERENCES};

    public static final int COL_ID = 0;
    public static final int COL_MSG_ID = 1;
    public static final int COL_FROM_EMAIL = 2;
    public static final int COL_FROM_NAME = 3;
    public static final int COL_SUBJECT = 4;
    public static final int COL_CHARSET = 5;
    public static final int COL_DATE = 6;
    public static final int COL_NEW = 7;
    public static final int COL_FK_N_ID = 8;
    public static final int COL_HEADER = 9;
    public static final int COL_BODY = 10;
    public static final int COL_LEFT_VALUE = 11;
    public static final int COL_RIGHT_VALUE = 12;
    public static final int COL_LEVEL = 13;
    public static final int COL_ROOT_MESSAGE = 14;
    public static final int COL_REFERENCES = 15;

    public MessageQueries(Context context) {
        this.context = context;
    }

    public CursorLoader getAllMessagesInCursorLoader(long newsgroupId) {
        return new CursorLoader(context, MessageContract.CONTENT_URI, PROJECTION_MESSAGE, MessageContract.MessageEntry.COL_FK_N_ID + " = ?", new String[]{newsgroupId + ""},
                MessageContract.MessageEntry.COL_DATE + " DESC");
    }

    public CursorLoader getRootMessagesInCursorLoader(long newsgroupId) {
        return new CursorLoader(context, MessageContract.CONTENT_URI, PROJECTION_MESSAGE, MessageContract.MessageEntry.COL_FK_N_ID + " = ? AND "
                + MessageContract.MessageEntry.COL_LEVEL + " = ? ", new String[]{newsgroupId + "", "0"}, MessageContract.MessageEntry.COL_DATE + " DESC");
    }

    /**
     * Get all children of a message with messageId sorted by their LEFT value (correct order to display them in a list)
     * @param messageId ID of the given message
     * @return a cursor with all children of a given message in the correct order to display
     */
    public CursorLoader getRootMessageWithChildren(long messageId) {
        return new CursorLoader(context, MessageContract.CONTENT_URI, PROJECTION_MESSAGE, MessageContract.MessageEntry.COL_ROOT_MSG + " = ? OR "
                + MessageContract.MessageEntry._ID + " = ? ", new String[]{messageId + "", messageId + ""}, MessageContract.MessageEntry.COL_LEFT_VALUE + " ASC");
    }



    public Cursor getMessagesOfNewsgroup(long newsgroupId) {
        return context.getContentResolver().query(MessageContract.CONTENT_URI, PROJECTION_MESSAGE, MessageContract.MessageEntry.COL_FK_N_ID + " = ?", new String[] {newsgroupId + ""},
                MessageContract.MessageEntry.COL_DATE + " DESC");
    }

    public long getIdFromMessageId(String messageId) {
        long id = -1;
        Cursor c = context.getContentResolver().query(MessageContract.CONTENT_URI, PROJECTION_MESSAGE, MessageContract.MessageEntry.COL_MSG_ID + " = ?",
                new String[] {messageId}, null);
        if (c.moveToFirst()) {
            id = c.getLong(COL_ID);
        }
        c.close();
        return  id;
    }

    public Cursor getMessageWithId(long messageId) {
        return context.getContentResolver().query(MessageContract.CONTENT_URI, PROJECTION_MESSAGE, MessageContract.MessageEntry._ID + " = ?", new String[]{messageId + ""},
                null);
    }

    public boolean isMessageInDatabase(String messageId) {
        Cursor c = context.getContentResolver().query(MessageContract.CONTENT_URI, PROJECTION_MESSAGE, MessageContract.MessageEntry.COL_MSG_ID + " = ?", new String[]{messageId + ""}, null);
        boolean result = c.moveToFirst();
        c.close();
        return result;
    }

    public String getMessageIdFromId(long id) {
        String messageId = null;
        Cursor c = getMessageWithId(id);
        if (c.moveToFirst()) {
            messageId = c.getString(COL_MSG_ID);
        }
        c.close();
        return messageId;
    }

    public int getNewMessagesCount(long newsgroupId) {
        return QueryHelper.count(context, MessageContract.CONTENT_URI, MessageContract.MessageEntry.COL_FK_N_ID + " = ? AND " + MessageContract.MessageEntry.COL_NEW + " = ?",
                new String[]{newsgroupId + "", "1"});
    }


    public boolean setMessageReadStatusThroughTheHierarchy (long messageId, boolean isNew) {
        int value = isNew ? 1 : 0;
        // update the NEW value of the given message
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessageContract.MessageEntry.COL_NEW, value);
        context.getContentResolver().update(MessageContract.CONTENT_URI, contentValues, MessageContract.MessageEntry._ID + " = ? ",
                new String[] {messageId + ""});
        if (isRootMessage(messageId)) { // all children get the sam status as their root message
            context.getContentResolver().update(MessageContract.CONTENT_URI, contentValues, MessageContract.MessageEntry.COL_ROOT_MSG + " = ? ",
                    new String[]{messageId + ""});
        }
        return true;
    }

    public boolean setMessageReadStatus(long messageId, boolean isNew) {
        int value = 1; // assume new message
        boolean isRootMessage = isRootMessage(messageId); // test if this message is a root message
        if (!isNew)  {
            if (isRootMessage && hasUnreadChildren(messageId)){ // not new & root message & unreadChildren
                value = -1;
            } else { // not new & no root message or no unread children -> 0
                value = 0;
            }
        }
        // update the NEW value of the given message
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessageContract.MessageEntry.COL_NEW, value);
        context.getContentResolver().update(MessageContract.CONTENT_URI, contentValues, MessageContract.MessageEntry._ID + " = ? ",
                new String[] {messageId + ""});

        if (!isRootMessage) {
            adjustRootMessagesReadValue(messageId);
        }

        return true;
    }

    public boolean hasUnreadChildren (long messageId) {
        boolean hasUnreadChildren = false;
        Cursor c = context.getContentResolver().query(MessageContract.CONTENT_URI, new String[]{MessageContract.MessageEntry._ID, MessageContract.MessageEntry.COL_NEW}, MessageContract.MessageEntry.COL_ROOT_MSG + " = ? ",
                new String[]{messageId + ""}, null);
        if (c.moveToFirst()) {
            while(!c.isAfterLast() && !hasUnreadChildren) {
                hasUnreadChildren = c.getInt(1) == 1;
                c.moveToNext();
            }
            c.close();
        }
        return hasUnreadChildren;
    }

    /**
     * Readjusts the status of a given messages' root message
     * @param messageId ID of the given message
     */
    public void adjustRootMessagesReadValue(long messageId) {
        long rootMessageId = -1;
        boolean rootMessageStatus = false;
        Cursor c = getMessageWithId(messageId);
        if (c.moveToFirst()) {
            rootMessageId = c.getLong(COL_ROOT_MESSAGE);
        }
        c.close();
        Cursor c1 = getMessageWithId(rootMessageId);
        if (c1.moveToFirst()) {
            rootMessageStatus = c1.getInt(COL_NEW) == 1;
        }
        c1.close();
        setMessageReadStatus(rootMessageId, rootMessageStatus);
    }


    public boolean addMessage(String messageId, String fromEmail, String fromName, String subject, String charset, long date, int isNew,
                              long newsgroupId, String header, String body, long parentMsg, long rootMsg, int level, String references) {
        // insert message
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessageContract.MessageEntry.COL_MSG_ID, messageId);
        contentValues.put(MessageContract.MessageEntry.COL_FROM_EMAIL, fromEmail);
        contentValues.put(MessageContract.MessageEntry.COL_FROM_NAME, fromName);
        contentValues.put(MessageContract.MessageEntry.COL_SUBJECT, subject);
        contentValues.put(MessageContract.MessageEntry.COL_CHARSET, charset);
        contentValues.put(MessageContract.MessageEntry.COL_DATE, date);
        contentValues.put(MessageContract.MessageEntry.COL_NEW, isNew);
        contentValues.put(MessageContract.MessageEntry.COL_FK_N_ID, newsgroupId);
        contentValues.put(MessageContract.MessageEntry.COL_HEADER, header);
        contentValues.put(MessageContract.MessageEntry.COL_BODY, body);
        contentValues.put(MessageContract.MessageEntry.COL_PARENT_MSG, parentMsg);
        contentValues.put(MessageContract.MessageEntry.COL_ROOT_MSG, rootMsg);
        contentValues.put(MessageContract.MessageEntry.COL_LEVEL, level);
        contentValues.put(MessageContract.MessageEntry.COL_REFERENCES, references);
        Point rootLeftRightValues = getLeftRightValue(rootMsg);
        Point parentLeftRightValues = getLeftRightValue(parentMsg);
        Point currentNodeLeftRightValues = calculateLeftRightValues(parentMsg, parentLeftRightValues);
        contentValues.put(MessageContract.MessageEntry.COL_LEFT_VALUE, currentNodeLeftRightValues.x);
        contentValues.put(MessageContract.MessageEntry.COL_RIGHT_VALUE, currentNodeLeftRightValues.y);
        adjustLeftRightValues(currentNodeLeftRightValues, newsgroupId, rootMsg, rootLeftRightValues);
        Uri message = context.getContentResolver().insert(MessageContract.CONTENT_URI, contentValues);
        Long msgId = Long.parseLong(message.getLastPathSegment());
        setMessageReadStatus(msgId, isNew == 1);
        return true;
    }

    /**
     * Gets the left and right value of a given message already stored in the database
     * @param messageId ID of a message
     * @return Point containing the left value as x coordinate and the right value as y coordinate
     */
    private Point getLeftRightValue(long messageId) {
        int rightValue = -1;
        int leftValue = -1;
        Cursor c = context.getContentResolver().query(MessageContract.CONTENT_URI, new String[]{
            MessageContract.MessageEntry.COL_RIGHT_VALUE, MessageContract.MessageEntry.COL_LEFT_VALUE}, MessageContract.MessageEntry._ID + " = ? ", new String[]{messageId + ""}, null);
        if (c.moveToFirst()) {
            rightValue = c.getInt(0);
            leftValue = c.getInt(1);
        }
        c.close();
        return new Point(leftValue, rightValue);
    }

    /**
     * Calculates the left and right values of the node to be inserted
     * @param parentMsg ID of the parent message of the current node
     * @param parentLeftRightValues Point containing the parent messages' left and right value as x and y coordinate
     * @return a Point containing the current nodes' left value as x coordinate and right value as y coordinate
     */
    private Point calculateLeftRightValues(long parentMsg, Point parentLeftRightValues) {
        long idOfYoungestSibling = getYoungestSibling(parentMsg);
        int leftValue;
        int rightValue;
        if (parentLeftRightValues.x == -1) {
            leftValue = 1;
            rightValue = 2;
        } else {
            Point youngestSiblingLeftRightValues = getLeftRightValue(idOfYoungestSibling);
            leftValue = idOfYoungestSibling == -1 ? // the node has no younger siblings.
                    parentLeftRightValues.x + 1 : youngestSiblingLeftRightValues.y + 1;
            rightValue = leftValue + 1;
        }
        return new Point(leftValue, rightValue);
    }

    /**
     * Gets the ID of the node which has the given parent and the youngest date if existing, else returns -1;
     * @param parentMsg ID of a message
     * @return id of youngest sibling or -1 if there are no siblings
     */
    private long getYoungestSibling(long parentMsg) {
        long youngestSibling = -1;
        if (parentMsg == -1) {
            return youngestSibling;
        }
        Cursor c = context.getContentResolver().query(MessageContract.CONTENT_URI, new String[]{MessageContract.MessageEntry._ID}, MessageContract.MessageEntry.COL_PARENT_MSG + " = ? ",
                new String[]{parentMsg + ""}, MessageContract.MessageEntry.COL_DATE + " DESC");
        if (c.moveToFirst()) {
            youngestSibling = c.getLong(0);
        }
        c.close();
        return youngestSibling;
    }

    /**
     * Set all nodes' left and right values + 2 which are in the same subtree, whose left values are greater or equal than the current node's left value
     * and whose right values are smaller or equal to the root's right value.
     * @param currentNodeLeftRightValues Point containing the left value of the current node as x, the right value of the current node as y coordinate
     * @param newsgroupId ID of the current node's newsgroup
     * @param rootLeftRightValue Point containing the left value of the current node's root as x, the right value of the current node's root as y coordinate
     */
    private void adjustLeftRightValues(Point currentNodeLeftRightValues, long newsgroupId, long rootId, Point rootLeftRightValue) {
        if (rootLeftRightValue.x == -1 && rootLeftRightValue.y == -1) {
            return;
        }
        Cursor c = context.getContentResolver().query(MessageContract.CONTENT_URI, PROJECTION_MESSAGE, MessageContract.MessageEntry.COL_FK_N_ID + " = ? AND (" +
                        MessageContract.MessageEntry.COL_ROOT_MSG + " = ? OR " + MessageContract.MessageEntry._ID + " = ?)", new String[] {newsgroupId + "", rootId + "", rootId + ""}, MessageContract.MessageEntry.COL_DATE + " DESC");
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                int left = c.getInt(COL_LEFT_VALUE);
                int right = c.getInt(COL_RIGHT_VALUE);
                ContentValues cv = new ContentValues();
                if (left >= currentNodeLeftRightValues.x)
                    cv.put(MessageContract.MessageEntry.COL_LEFT_VALUE, left + 2);
                if (right >= currentNodeLeftRightValues.x)
                    cv.put(MessageContract.MessageEntry.COL_RIGHT_VALUE, right + 2);
                if (cv.containsKey(MessageContract.MessageEntry.COL_LEFT_VALUE) || cv.containsKey(MessageContract.MessageEntry.COL_RIGHT_VALUE))
                    context.getContentResolver().update(MessageContract.CONTENT_URI, cv, MessageContract.MessageEntry._ID + " = ? ", new String[]{c.getInt(COL_ID) + ""});
                c.moveToNext();
            }
        }
        c.close();
    }

    /**
     * Tests if a message with messageId has any children
     * @param messageId ID of the given message
     * @return true if there are any messages with rootId = messageId, else false
     */
    public boolean hasMessageChildren (long messageId) {
        boolean hasChildren = false;
        Cursor c = context.getContentResolver().query(MessageContract.CONTENT_URI, new String[]{MessageContract.MessageEntry._ID}, MessageContract.MessageEntry.COL_ROOT_MSG + " = ? ",
                new String[]{messageId + ""}, null);
        if (c.moveToFirst()) {
            hasChildren = true;
        }
        c.close();
        return  hasChildren;
    }

    /**
     * Test if a message is the root of a conversation
     * @param messageId ID of the given message
     * @return true if the message's level = 0, else false
     */
    public boolean isRootMessage(long messageId) {
        boolean isRootMessage = false;
        Cursor c = getMessageWithId(messageId);
        if (c.moveToFirst() && c.getInt(COL_LEVEL) == 0) {
            isRootMessage = true;
        }
        c.close();
        return isRootMessage;
    }


    /**
     * Deletes all messages with COL_FK_N_ID = newsgroupId
     * @param newsgroupId ID of a newsgroup
     */
    public  void deleteMessagesFromNewsgroup(long newsgroupId) {
        int delCount = context.getContentResolver().delete(MessageContract.CONTENT_URI, MessageContract.MessageEntry.COL_FK_N_ID + " = ? ", new String[] {newsgroupId + ""});
        Log.i(TAG, delCount + " rows deleted");
    }

}
