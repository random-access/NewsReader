package org.random_access.newsreader.nntp;

import android.content.Context;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 18.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
class NNTPDateFormatter {

    private static final String TAG = NNTPDateFormatter.class.getSimpleName();

    public static final String DATE_PATTERN_DATABASE = "yyyyMMddhhmmss Z";
    public static final String DATE_PATTERN_MSG_HEADER = "EEE, dd MMM yyyy hh:mm:ss Z";

    // pattern database: "yyyyMMddhhmmss Z"
    // ----> eg 20150502181729 +0200

    // pattern message header: "EEE, d MMM yyyy HH:mm:ss Z"
    // ----> eg Sat, 02 May 2015 18:17:29 +0200
    private GregorianCalendar parseDateStringToGregorianCalendar(String dateString, String pattern) throws ParseException {
       // Log.d(TAG, "Date to parse: " + dateString);
        DateFormat df = new SimpleDateFormat(pattern, Locale.US);
        Date date = df.parse(dateString);
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal;
    }

    public String getPrettyDateString(String rawDate, @SuppressWarnings("SameParameterValue") String pattern, Context context) {
        SimpleDateFormat rawFormat = new SimpleDateFormat(pattern, Locale.US);
        Date date = null;
        try {
            date = rawFormat.parse(rawDate);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date from filename!");
        }
        DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(context);
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        if (date != null) {
            String formattedDate = dateFormat.format(date);
            String formattedTime = timeFormat.format(date);
            return formattedDate + " " + formattedTime;
        } else {
            return rawDate;
        }

    }
}
