package org.random_access.newsreader.queries;

import android.content.Context;
import android.database.Cursor;

import org.random_access.newsreader.provider.contracts.MessageContract;

import java.io.IOException;
import java.util.ArrayList;

/**
 * <b>Project:</b> FlashCards Manager for Android <br>
 * <b>Date:</b> 18.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class MessageQueries {

    private Context context;

    private static final String[] PROJECTION_MESSAGE = new String[] {MessageContract.MessageEntry._ID, MessageContract.MessageEntry.COL_MSG_ID,
            MessageContract.MessageEntry.COL_FK_N_ID, MessageContract.MessageEntry.COL_DATE , MessageContract.MessageEntry.COL_TIMEZONE};

    // TODO 0 line -> charset
    public static final int COL_ID = 1;
    public static final int COL_MSG_ID = 2;
    public static final int COL_FK_N_ID = 3;
    public static final int COL_DATE = 4;
    public static final int COL_TIMEZONE = 5;

    public MessageQueries(Context context) {
        this.context = context;
    }

    /**
     * Returns an arraylist of the youngest message ID's, containing:
     * <ul>
     *      <li>0 entries if there are no messages at all</li>
     *      <li>1 entry if there is 1 newest message</li>
     *      <li>> 1 entries if there are messages posted at exactly the same date & time</li>
     * </ul>
     * This method also takes into consideration the time zone by adding its hours to the date-time-value.
     * @param newsgroupId ID of newsgroup we want to look at
     * @return ArrayList<String>, containing all the message ID's of the youngest message(-s) or being empty
     */
    public ArrayList<String> getListOfYoungestMessagesInNewsgroup (long newsgroupId) {
        ArrayList<String> result = new ArrayList<>();
        Cursor c =  context.getContentResolver().query
                (MessageContract.CONTENT_URI, PROJECTION_MESSAGE, MessageContract.MessageEntry.COL_FK_N_ID + " = ?", new String[] {newsgroupId + ""},
                        MessageContract.MessageEntry.COL_DATE + " DESC, " + MessageContract.MessageEntry.COL_TIMEZONE + " DESC");
        if (c.moveToFirst()) {
            long messageDate = c.getLong(COL_DATE) + convertTimeZoneInNumber(c.getString(COL_TIMEZONE));
            long newMessageDate = messageDate;
            while (messageDate == newMessageDate) {
                result.add(c.getString(COL_MSG_ID));
                c.moveToNext();
                newMessageDate  = c.getLong(COL_DATE) + convertTimeZoneInNumber(c.getString(COL_TIMEZONE));
            }
        }

        c.close();
        return  result;
    }

    /**
     * Converts a timezone string, e.g. "+0200" to a long value that can be added to the date&time
     * string to convert it in "+0000"
     * @param timeZone timezone string
     * @return long containing the value after the +/- sign multiplied by 100 and, if necessary,
     * converted into a negative number
     */
    private long convertTimeZoneInNumber(String timeZone) {
        char sign = timeZone.charAt(0);
        int multiplicator = sign == '+' ? 1 : -1;
        int extendToHours = 100;
        return Integer.parseInt(timeZone.substring(1))* extendToHours * multiplicator;
    }



}
