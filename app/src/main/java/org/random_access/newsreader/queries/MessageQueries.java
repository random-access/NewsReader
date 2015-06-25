package org.random_access.newsreader.queries;

import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.os.Message;

import org.random_access.newsreader.provider.contracts.MessageContract;

import java.util.ArrayList;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.07.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class MessageQueries {

    private final Context context;

    private static final String[] PROJECTION_MESSAGE = new String[] {MessageContract.MessageEntry._ID, MessageContract.MessageEntry.COL_MSG_ID, MessageContract.MessageEntry.COL_FROM_EMAIL,
            MessageContract.MessageEntry.COL_FROM_NAME, MessageContract.MessageEntry.COL_SUBJECT, MessageContract.MessageEntry.COL_CHARSET,
            MessageContract.MessageEntry.COL_DATE,MessageContract.MessageEntry.COL_NEW, MessageContract.MessageEntry.COL_IN_REPLY_TO,
            MessageContract.MessageEntry.COL_FK_N_ID, MessageContract.MessageEntry.COL_HEADER, MessageContract.MessageEntry.COL_BODY};

    public static final int COL_ID = 0;
    public static final int COL_MSG_ID = 1;
    public static final int COL_FROM_EMAIL = 2;
    public static final int COL_FROM_NAME = 3;
    public static final int COL_SUBJECT = 4;
    public static final int COL_CHARSET = 5;
    public static final int COL_DATE = 6;
    public static final int COL_NEW = 7;
    public static final int COL_IN_REPLY_TO = 8;
    public static final int COL_FK_N_ID = 9;
    public static final int COL_HEADER = 10;
    public static final int COL_BODY = 11;

    public MessageQueries(Context context) {
        this.context = context;
    }

    public CursorLoader getMessagesInCursorLoader(long newsgroupId) {
        return new CursorLoader(context, MessageContract.CONTENT_URI, PROJECTION_MESSAGE,  MessageContract.MessageEntry.COL_FK_N_ID + " = ?", new String[] {newsgroupId + ""},
                MessageContract.MessageEntry.COL_DATE + " DESC");
    }

    public Cursor getMessagesOfNewsgroup(long newsgroupId) {
        return context.getContentResolver().query(MessageContract.CONTENT_URI, PROJECTION_MESSAGE, MessageContract.MessageEntry.COL_FK_N_ID + " = ?", new String[] {newsgroupId + ""},
                MessageContract.MessageEntry.COL_DATE + " DESC");
    }

    public Cursor getMessageWithId(long messageId) {
        return context.getContentResolver().query(MessageContract.CONTENT_URI, PROJECTION_MESSAGE, MessageContract.MessageEntry._ID + " = ?", new String[] {messageId + ""},
                null);
    }

    public int getNewMessagesCount(long newsgroupId) {
        return QueryHelper.count(context, MessageContract.CONTENT_URI, MessageContract.MessageEntry.COL_FK_N_ID + " = ? AND " + MessageContract.MessageEntry.COL_NEW + " = ?",
                new String[]{newsgroupId + "", "1"});
    }

    public boolean setMessageRead(long messageId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessageContract.MessageEntry.COL_NEW, 0);
        return context.getContentResolver().update(MessageContract.CONTENT_URI, contentValues, MessageContract.MessageEntry._ID + " = ? ",
                new String[] {messageId + ""}) > 0;
    }


    public boolean addMessage(String messageId, String fromEmail, String fromName, String subject, String charset, long date, int isNew, long inReplyTo,
                              long newsgroupId, String header, String body) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessageContract.MessageEntry.COL_MSG_ID, messageId);
        contentValues.put(MessageContract.MessageEntry.COL_FROM_EMAIL, fromEmail);
        contentValues.put(MessageContract.MessageEntry.COL_FROM_NAME, fromName);
        contentValues.put(MessageContract.MessageEntry.COL_SUBJECT, subject);
        contentValues.put(MessageContract.MessageEntry.COL_CHARSET, charset);
        contentValues.put(MessageContract.MessageEntry.COL_DATE, date);
        contentValues.put(MessageContract.MessageEntry.COL_NEW, isNew);
        contentValues.put(MessageContract.MessageEntry.COL_IN_REPLY_TO, inReplyTo);
        contentValues.put(MessageContract.MessageEntry.COL_FK_N_ID, newsgroupId);
        contentValues.put(MessageContract.MessageEntry.COL_HEADER, header);
        contentValues.put(MessageContract.MessageEntry.COL_BODY, body);
        return context.getContentResolver().insert(MessageContract.CONTENT_URI, contentValues) != null;
    }

//    /**
//     * Returns an arraylist of the youngest message ID's, containing:
//     * <ul>
//     *      <li>0 entries if there are no messages at all</li>
//     *      <li>1 entry if there is 1 newest message</li>
//     *      <li>> 1 entries if there are messages posted at exactly the same date & time</li>
//     * </ul>
//     * This method also takes into consideration the time zone by adding its hours to the date-time-value.
//     * @param newsgroupId ID of newsgroup we want to look at
//     * @return ArrayList<String>, containing all the message ID's of the youngest message(-s) or being empty
//     */
//    public long getDateOfYoungestMessagesInNewsgroup (long newsgroupId) {
//        long result = 0;
//        Cursor c =  context.getContentResolver().query
//                (MessageContract.CONTENT_URI, PROJECTION_MESSAGE, MessageContract.MessageEntry.COL_FK_N_ID + " = ?", new String[] {newsgroupId + ""},
//                        MessageContract.MessageEntry.COL_DATE + " DESC");
//        if (c.moveToFirst()) {
//            result = c.getLong(COL_DATE) + convertTimeZoneInNumber(c.getString(COL_TIMEZONE));
//        }
//        c.close();
//        return  result;
//    }
//
//    /**
//     * Converts a timezone string, e.g. "+0200" to a long value that can be added to the date&time
//     * string to convert it in "+0000"
//     * @param timeZone timezone string
//     * @return long containing the value after the +/- sign multiplied by 100 and, if necessary,
//     * converted into a negative number
//     */
//    private long convertTimeZoneInNumber(String timeZone) {
//        char sign = timeZone.charAt(0);
//        int multiplicator = sign == '+' ? 1 : -1;
//        int extendToHours = 100;
//        return Integer.parseInt(timeZone.substring(1))* extendToHours * multiplicator;
//    }

}
