package org.random_access.newsreader.queries;

import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.util.Log;

import org.random_access.newsreader.provider.contracts.MessageContract;
import org.random_access.newsreader.utils.ListUtils;

import java.util.ArrayList;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.06.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
@SuppressWarnings("WeakerAccess")
public class MessageQueries {

    private static final String TAG = MessageQueries.class.getSimpleName();
    private final Context context;

    private static final String[] PROJECTION_MESSAGE = new String[] {MessageContract.MessageEntry._ID, MessageContract.MessageEntry.COL_MSG_ID,
            MessageContract.MessageEntry.COL_FROM_EMAIL, MessageContract.MessageEntry.COL_FROM_NAME, MessageContract.MessageEntry.COL_SUBJECT,
            MessageContract.MessageEntry.COL_CHARSET, MessageContract.MessageEntry.COL_DATE,MessageContract.MessageEntry.COL_NEW,
            MessageContract.MessageEntry.COL_FK_N_ID, MessageContract.MessageEntry.COL_HEADER, MessageContract.MessageEntry.COL_BODY,
            MessageContract.MessageEntry.COL_LEFT_VALUE, MessageContract.MessageEntry.COL_RIGHT_VALUE, MessageContract.MessageEntry.COL_LEVEL,
            MessageContract.MessageEntry.COL_ROOT_MSG, MessageContract.MessageEntry.COL_REFERENCES, MessageContract.MessageEntry.COL_FRESH};

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
    public static final int COL_FRESH = 16;

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
     * @return a CursorLoader with all children of a given message in the correct order to display
     */
    public CursorLoader getRootMessageWithChildren(long messageId) {
        return new CursorLoader(context, MessageContract.CONTENT_URI, PROJECTION_MESSAGE, MessageContract.MessageEntry.COL_ROOT_MSG + " = ? OR "
                + MessageContract.MessageEntry._ID + " = ? ", new String[]{messageId + "", messageId + ""}, MessageContract.MessageEntry.COL_LEFT_VALUE + " ASC");
    }

    /**
     * Get the _ID field of a message with a given messageId
     * @param messageId the message id value (identifying a message on the news server)
     * @return _ID field of message, -1 if message is not existing
     */
    public long getIdFromMessageId(String messageId, long newsgroupId) {
        long id = -1;
        Cursor c = context.getContentResolver().query(MessageContract.CONTENT_URI, PROJECTION_MESSAGE, MessageContract.MessageEntry.COL_MSG_ID + " = ? AND "
                        + MessageContract.MessageEntry.COL_FK_N_ID + " = ?", new String[] {messageId, newsgroupId + ""}, null);
        if (c.moveToFirst()) {
            id = c.getLong(COL_ID);
        }
        c.close();
        return  id;
    }

    /**
     * Get a cursor with the message identified by its _ID field
     * @param id _ID field of a message (identifying a message in the database)
     * @return a cursor pointing at the given message or a cursor with an empty result if this message is not existing
     */
    public Cursor getMessageWithId(long id) {
        return context.getContentResolver().query(MessageContract.CONTENT_URI, PROJECTION_MESSAGE, MessageContract.MessageEntry._ID + " = ?", new String[]{id + ""},
                null);
    }

    /**
     * Find out if a message with a given message id is in the database
     * @param messageId the message id value (identifying a message on the news server
     * @return true if message is in database, otherwise false
     */
    public boolean isMessageInDatabase(String messageId, long newsgroupId) {
        Cursor c = context.getContentResolver().query(MessageContract.CONTENT_URI, PROJECTION_MESSAGE, MessageContract.MessageEntry.COL_MSG_ID + " = ? AND "
                + MessageContract.MessageEntry.COL_FK_N_ID + " = ?", new String[]{messageId + "", newsgroupId + ""}, null);
        boolean result = c.moveToFirst();
        c.close();
        return result;
    }

    /**
     * Counts the new messages in a given newsgroup
     * @param serverId _ID field of a newsgroup (identifying a newsgroup in the database)
     * @return number of new messages in the newsgroup
     */
    public int getNewMessagesOnServerCount(long serverId) {
        int freshCount = 0;
        Cursor c = new NewsgroupQueries(context).getNewsgroupsOfServer(serverId);
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                freshCount += getNewMessagesCount(c.getLong(NewsgroupQueries.COL_ID));
                c.moveToNext();
            }
        }
        c.close();
        return freshCount;
    }


    /**
     * Counts the new messages in a given newsgroup
     * @param serverId _ID field of a newsgroup (identifying a newsgroup in the database)
     * @return number of new messages in the newsgroup
     */
    public int getFreshMessagesOnServerCount(long serverId) {
        int freshCount = 0;
        Cursor c = new NewsgroupQueries(context).getNewsgroupsOfServer(serverId);
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                freshCount += getFreshMessagesCount(c.getLong(NewsgroupQueries.COL_ID));
                c.moveToNext();
            }
        }
        c.close();
        return freshCount;
    }


    /**
     * Counts the new messages in a given newsgroup
     * @param newsgroupId _ID field of a newsgroup (identifying a newsgroup in the database)
     * @return number of new messages in the newsgroup
     */
    public int getNewMessagesCount(long newsgroupId) {
        return QueryHelper.count(context, MessageContract.CONTENT_URI, MessageContract.MessageEntry.COL_FK_N_ID + " = ? AND " + MessageContract.MessageEntry.COL_NEW + " = ?",
                new String[]{newsgroupId + "", "1"});
    }

    /**
     * Counts the fresh messages in a given newsgroup
     * @param newsgroupId _ID field of a newsgroup (identifying a newsgroup in the database)
     * @return number of new messages in the newsgroup
     */
    public int getFreshMessagesCount(long newsgroupId) {
        return QueryHelper.count(context, MessageContract.CONTENT_URI, MessageContract.MessageEntry.COL_FK_N_ID + " = ? AND " + MessageContract.MessageEntry.COL_FRESH + " = ?",
                new String[]{newsgroupId + "", "1"});
    }

    /**
     * Mark all messages of a given newsgroup as not fresh
     * @param newsgroupId _ID field of a newsgroup (identifying a newsgroup in the database)
     */
    public void markAllMessagesNonFresh(long newsgroupId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessageContract.MessageEntry.COL_FRESH, 0);
        context.getContentResolver().update(MessageContract.CONTENT_URI, contentValues, MessageContract.MessageEntry.COL_FK_N_ID + " = ? ", new String[] {newsgroupId + ""});
    }

    /**
     * Mark all messages of a given newsgroup as not fresh
     * @param serverId _ID field of a newsgroup (identifying a newsgroup in the database)
     */
    public void markAllMessagesOnServerNonFresh(long serverId) {
        ArrayList<Long> serverIds = new ArrayList<>();
        Cursor c = new NewsgroupQueries(context).getNewsgroupsOfServer(serverId);
        if (c.moveToFirst()) {
            while(!c.isAfterLast()) {
                serverIds.add(c.getLong(NewsgroupQueries.COL_ID));
                c.moveToNext();
            }
        }
        c.close();

        ContentValues contentValues = new ContentValues();
        contentValues.put(MessageContract.MessageEntry.COL_FRESH, 0);
        int updatedRows = context.getContentResolver().update(MessageContract.CONTENT_URI, contentValues,
                MessageContract.MessageEntry.COL_FK_N_ID + " in (" + QueryHelper.makePlaceholderArray(serverIds.size()) + ")",
                new ListUtils<Long>().convertArrayListToStringArray(serverIds));
        Log.d(TAG, "Successfully updated " + updatedRows + " rows (JOIN!!)");
    }

    /**
     * Mark all messages as not fresh
     */
    public void markAllMessagesNonFresh() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessageContract.MessageEntry.COL_FRESH, 0);
        context.getContentResolver().update(MessageContract.CONTENT_URI, contentValues, null, null);
    }

    /**
     * Marks either a single message (if it has no children) or the root and all its child messages (if this is a root message)
     * as read if isNew is true or as unread if isNew is false
     * @param id _ID field of a message (identifying a message in the database)
     * @param isNew input true to mark messages as unread, false to mark messages as read
     */
    public void setMessageNewStatusThroughTheHierarchy(long id, boolean isNew) {
        int value = isNew ? 1 : 0;
        // update the NEW value of the given message
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessageContract.MessageEntry.COL_NEW, value);
        context.getContentResolver().update(MessageContract.CONTENT_URI, contentValues, MessageContract.MessageEntry._ID + " = ? ",
                new String[] {id + ""});
        if (isRootMessage(id)) { // all children get the same read status as their root message
            context.getContentResolver().update(MessageContract.CONTENT_URI, contentValues, MessageContract.MessageEntry.COL_ROOT_MSG + " = ? ",
                    new String[]{id + ""});
        }
        if (!isNew) {
            // if message is marked as read, set the FRESH value also to 0, if message is marked as unread again do
            // nothing because a message is marked as fresh only when initially fetched from server
            setMessagesUnfreshThroughTheHierarchy(id);
        }
    }

    /**
     * Marks either a single message (if it has no children) or the root and all its child messages (if this is a root message)
     * as non-fresh
     * @param id _ID field of a message (identifying a message in the database)
     */
    public void setMessagesUnfreshThroughTheHierarchy(long id) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessageContract.MessageEntry.COL_FRESH, 0);
        context.getContentResolver().update(MessageContract.CONTENT_URI, contentValues, MessageContract.MessageEntry._ID + " = ? ",
                new String[]{id + ""});
        if (isRootMessage(id)) { // all children get the same fresh status as their root message
            context.getContentResolver().update(MessageContract.CONTENT_URI, contentValues, MessageContract.MessageEntry.COL_ROOT_MSG + " = ? ",
                    new String[]{id + ""});
        }
    }

    /**
     * sets the messages NEW value, correcting values of all affected rows
     * @param id _ID field of a message (identifying a message in the database)
     * @param isNew true if message is new
     */
    public void setMessageNewStatus(long id, boolean isNew) {
        int value = 1; // assume new message
        boolean isRootMessage = isRootMessage(id); // test if this message is a root message
        if (!isNew)  { // set value to -1 if this is a root message and has any new children in order to display root message as new in hierarchial overview
            if (isRootMessage && hasNewChildren(id)){
                value = -1;
            } else {
                value = 0;
            }
        }
        // update the NEW value of the given message
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessageContract.MessageEntry.COL_NEW, value);
        context.getContentResolver().update(MessageContract.CONTENT_URI, contentValues, MessageContract.MessageEntry._ID + " = ? ",
                new String[]{id + ""});

        // if this is no root message, we might have to change the root messages read status
        if (!isRootMessage) {
            adjustRootMessagesNewValue(id);
        }
        // if this message is marked as read it should not be marked as fresh anymore. remarking it as new has no effects
        // on the fresh status because a message is just fresh immediately after fetching it from the server
        if (!isNew) {
            setMessageFreshStatus(id, false);
        }
    }

    /**
     * sets the messages FRESH value, correcting values of all affected rows
     * @param id _ID field of a message (identifying a message in the database)
     * @param isFresh true if message is fresh
     */
    public boolean setMessageFreshStatus (long id, boolean isFresh) {
        int value = 1; // assume fresh message
        boolean isRootMessage = isRootMessage(id); // test if this message is a root message
        if (!isFresh) { // set value to -1 if this is a root message and has any fresh children in order to display root message as fresh in hierarchial overview
            if (isRootMessage && hasFreshChildren(id)) {
                value = -1;
            } else {
                value = 0;
            }
        }
        // update the FRESH value of the given message
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessageContract.MessageEntry.COL_FRESH, value);
        context.getContentResolver().update(MessageContract.CONTENT_URI, contentValues, MessageContract.MessageEntry._ID + " = ? ",
                new String[] {id + ""});

        // if this is no root message, we might have to change the root messages fresh status
        if (!isRootMessage) {
            adjustRootMessagesFreshValue(id);
        }
        return true;
    }

    /**
     * Test if root message with the given rootId has unread children
     * @param rootId ID of the given message
     * @return true if message has unread children, otherwise false
     */
    private boolean hasNewChildren(long rootId) {
        boolean hasUnreadChildren = false;
        Cursor c = context.getContentResolver().query(MessageContract.CONTENT_URI, new String[]{MessageContract.MessageEntry._ID, MessageContract.MessageEntry.COL_NEW}, MessageContract.MessageEntry.COL_ROOT_MSG + " = ? ",
                new String[]{rootId + ""}, null);
        if (c.moveToFirst()) {
            while(!c.isAfterLast() && !hasUnreadChildren) {
                hasUnreadChildren = c.getInt(1) == 1;
                c.moveToNext();
            }
        }
        c.close();
        return hasUnreadChildren;
    }

    /**
     * Test if root message with the given rootId has fresh children
     * @param rootId ID of the given message
     * @return true if message has non fresh children, otherwise false
     */
     private boolean hasFreshChildren(long rootId) {
        boolean hasFreshChildren = false;
        Cursor c = context.getContentResolver().query(MessageContract.CONTENT_URI, new String[]{MessageContract.MessageEntry._ID, MessageContract.MessageEntry.COL_FRESH}, MessageContract.MessageEntry.COL_ROOT_MSG + " = ? ",
                new String[]{rootId + ""}, null);
        if (c.moveToFirst()) {
            while (!c.isAfterLast() && !hasFreshChildren) {
                hasFreshChildren = c.getInt(1) == 1;
                c.moveToNext();
            }
        }
        c.close();
        return hasFreshChildren;
    }

    /**
     * Readjusts the read status of a given messages' root message
     * @param messageId ID of the given message
     */
    private void adjustRootMessagesNewValue(long messageId) {
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
        setMessageNewStatus(rootMessageId, rootMessageStatus);
    }

    /**
     * Readjusts the fresh status of a given messages' root message
     * @param messageId ID of the given message
     */
    private void adjustRootMessagesFreshValue(long messageId) {
        long rootMessageId = -1;
        boolean freshMessageStatus = false;
        Cursor c = getMessageWithId(messageId);
        if (c.moveToFirst()) {
            rootMessageId = c.getLong(COL_ROOT_MESSAGE);
        }
        c.close();
        Cursor c1 = getMessageWithId(rootMessageId);
        if (c1.moveToFirst()) {
            freshMessageStatus = c1.getInt(COL_FRESH) == 1;
        }
        c1.close();
        setMessageFreshStatus(rootMessageId, freshMessageStatus);
    }


    public boolean addMessage(String messageId, String fromEmail, String fromName, String subject, String charset, long date, int isNew,
                              long newsgroupId, String header, String body, long parentId, long rootId, int level, String references, int isFresh) {
        // calculate missing values
        Point currentNodeLeftRightValues = calculateLeftRightValues(parentId, rootId);

        // insert message
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessageContract.MessageEntry.COL_MSG_ID, messageId);
        contentValues.put(MessageContract.MessageEntry.COL_FROM_EMAIL, fromEmail);
        contentValues.put(MessageContract.MessageEntry.COL_FROM_NAME, fromName);
        contentValues.put(MessageContract.MessageEntry.COL_SUBJECT, subject == null ? "" : subject);
        contentValues.put(MessageContract.MessageEntry.COL_CHARSET, charset);
        contentValues.put(MessageContract.MessageEntry.COL_DATE, date);
        contentValues.put(MessageContract.MessageEntry.COL_NEW, isNew);
        contentValues.put(MessageContract.MessageEntry.COL_FK_N_ID, newsgroupId);
        contentValues.put(MessageContract.MessageEntry.COL_HEADER, header);
        contentValues.put(MessageContract.MessageEntry.COL_BODY, body);
        contentValues.put(MessageContract.MessageEntry.COL_PARENT_MSG, parentId);
        contentValues.put(MessageContract.MessageEntry.COL_ROOT_MSG, rootId);
        contentValues.put(MessageContract.MessageEntry.COL_LEVEL, level);
        contentValues.put(MessageContract.MessageEntry.COL_REFERENCES, references);
        contentValues.put(MessageContract.MessageEntry.COL_FRESH, isFresh);
        contentValues.put(MessageContract.MessageEntry.COL_LEFT_VALUE, currentNodeLeftRightValues.x);
        contentValues.put(MessageContract.MessageEntry.COL_RIGHT_VALUE, currentNodeLeftRightValues.y);

        // update other message values
        adjustLeftRightValues(currentNodeLeftRightValues, newsgroupId, rootId);
        Uri message = context.getContentResolver().insert(MessageContract.CONTENT_URI, contentValues);
        Long msgId = Long.parseLong(message.getLastPathSegment());
        setMessageNewStatus(msgId, isNew == 1);
        setMessageFreshStatus(msgId, isFresh == 1);
        return true;
    }

    /**
     * Gets the left and right value of a given message already stored in the database
     * @param messageId ID of a message
     * @return Point containing the left value as x coordinate and the right value as y coordinate, (-1, -1) if message is not in database
     */
    private Point getLeftRightValue(long messageId) {
        int rValue = -1;
        int lValue = -1;
        Cursor c = context.getContentResolver().query(MessageContract.CONTENT_URI, new String[]{
            MessageContract.MessageEntry.COL_RIGHT_VALUE, MessageContract.MessageEntry.COL_LEFT_VALUE}, MessageContract.MessageEntry._ID + " = ? ", new String[]{messageId + ""}, null);
        if (c.moveToFirst()) {
            rValue = c.getInt(0);
            lValue = c.getInt(1);
        }
        c.close();
        return new Point(lValue, rValue);
    }

    /**
     * Calculates the left and right values of the node to be inserted
     * @param parentMsg ID of the parent message of the current node
     * @return a Point containing the current nodes' left value as x coordinate and right value as y coordinate
     */
    private Point calculateLeftRightValues(long parentMsg, long rootMsg) {
        Point rootLeftRightValues = getLeftRightValue(rootMsg);
        Point parentLeftRightValues = getLeftRightValue(parentMsg);
        long idOfYoungestSibling = getYoungestSibling(parentMsg);
        int lValue;
        int rValue;
        if (parentLeftRightValues.x == -1) {
            lValue = 1;
            rValue = 2;
        } else {
            Point youngestSiblingLeftRightValues = getLeftRightValue(idOfYoungestSibling);
            lValue = idOfYoungestSibling == -1 ? // the node has no younger siblings.
                    parentLeftRightValues.x + 1 : youngestSiblingLeftRightValues.y + 1;
            rValue = lValue + 1;
        }
        return new Point(lValue, rValue);
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
     * @param rootId ID of root message
     */
    private void adjustLeftRightValues(Point currentNodeLeftRightValues, long newsgroupId, long rootId) {
        Point rootLeftRightValue = getLeftRightValue(rootId);
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
        int delCount = context.getContentResolver().delete(MessageContract.CONTENT_URI, MessageContract.MessageEntry.COL_FK_N_ID + " = ? ", new String[]{newsgroupId + ""});
        Log.i(TAG, delCount + " rows deleted");
    }


    /**
     * Deletes all messages with COL_FK_N_ID = newsgroupId older than timeLimit
     * @param newsgroupId ID of a newsgroup
     * @param timeLimit date before which all messages in the given newsgroup should be deleted
     */
    public  void deleteOldMessages(long newsgroupId, long timeLimit) {
        Cursor c = context.getContentResolver().query(MessageContract.CONTENT_URI, PROJECTION_MESSAGE, MessageContract.MessageEntry.COL_FK_N_ID + " = ? AND "
                + MessageContract.MessageEntry.COL_DATE + " < ? ", new String[]{newsgroupId + "", timeLimit + ""}, MessageContract.MessageEntry.COL_DATE + " ASC");
        if (c.moveToFirst()) {
            while(!c.isAfterLast()) {
                deleteRootMessage(c.getLong(COL_ID));
                c.moveToNext();
            }
        }
        c.close();
    }

    private void deleteRootMessage(long rootId) {
        // get all children of message with id = rootId
        Cursor c = context.getContentResolver().query(MessageContract.CONTENT_URI, PROJECTION_MESSAGE,
                MessageContract.MessageEntry.COL_ROOT_MSG + " = ? AND " + MessageContract.MessageEntry.COL_PARENT_MSG + " = ? ", new String[]{rootId + "", rootId + ""}, null);
        if (c.moveToFirst()) {
            while(!c.isAfterLast()) {
                long msgId = c.getLong(COL_ID);
                int left = c.getInt(COL_LEFT_VALUE);
                int right = c.getInt(COL_RIGHT_VALUE);
                // get all messages in the subtree of message with ID msgId
                Cursor c1 = context.getContentResolver().query(MessageContract.CONTENT_URI, PROJECTION_MESSAGE, MessageContract.MessageEntry.COL_ROOT_MSG + " = ? AND "
                     + MessageContract.MessageEntry.COL_LEFT_VALUE + " > ? AND " + MessageContract.MessageEntry.COL_RIGHT_VALUE + " < ? ", new String[] {rootId + "", left + "", right + ""}, null);
                if (c1.moveToFirst()) {
                    while(!c1.isAfterLast()) {
                        adjustLeftRightValuesWhileDeleting(c1.getLong(COL_ID), c1.getInt(COL_LEFT_VALUE), c1.getInt(COL_RIGHT_VALUE), c1.getInt(COL_LEVEL), left - 1, msgId);
                        c1.moveToNext();
                    }
                }
                c1.close();
                adjustLeftRightValuesWhileDeleting(c.getLong(COL_ID), c.getInt(COL_LEFT_VALUE), c.getInt(COL_RIGHT_VALUE), c.getInt(COL_LEVEL), left - 1, -1);
                c.moveToNext();
            }
        }
        c.close();
        context.getContentResolver().delete(MessageContract.CONTENT_URI, MessageContract.MessageEntry._ID + " = ? ", new String[]{rootId + ""});
        Log.d(TAG, "Deleted message with id: " + rootId);
    }

    /**
     * Adjusts the values of a message responsible for the order of this message in the hierarchy
     * @param id database _ID value
     * @param left current database _LEFT value
     * @param right current database _RIGHT value
     * @param level current database _LEVEL value
     * @param diff how much to subtract from left and right
     * @param rootId id of new message root
     */
    private void adjustLeftRightValuesWhileDeleting(long id, int left, int right, int level, int diff, long rootId) {
        ContentValues cv = new ContentValues();
        if (rootId == -1) {
            cv.put(MessageContract.MessageEntry.COL_PARENT_MSG, -1);
        }
        cv.put(MessageContract.MessageEntry.COL_ROOT_MSG, rootId);
        cv.put(MessageContract.MessageEntry.COL_LEFT_VALUE, left-diff);
        cv.put(MessageContract.MessageEntry.COL_RIGHT_VALUE, right-diff);
        cv.put(MessageContract.MessageEntry.COL_LEVEL, level-1);
        context.getContentResolver().update(MessageContract.CONTENT_URI, cv, MessageContract.MessageEntry._ID + " = ? ", new String[]{id + ""});
        Log.d(TAG, "Adjust values: ID = " + id + ", root = " + rootId + ", left = " + (left-diff) + ", right = " + (right-diff) + ", level = " + (level-1));
    }


}
