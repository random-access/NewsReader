package org.random_access.newsreader.nntp;

import android.content.Context;
import android.util.Log;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.apache.commons.net.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>Project:</b> FlashCards Manager for Android <br>
 * <b>Date:</b> 18.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class NNTPMessageHeader {

    private static final String TAG = NNTPMessageHeader.class.getSimpleName();

    public static final String KEY_MESSAGE_ID = "Message-ID";
    public static final String KEY_FROM = "From";
    public static final String KEY_NAME = "Name";
    public static final String KEY_EMAIL = "Email";
    public static final String KEY_CONTENT_TYPE = "Content-Type";
    public static final String KEY_CHARSET = "Charset";
    public static final String KEY_DATE = "Date";
    public static final String KEY_SUBJECT = "Subject";
    public static final String KEY_TRANSFER_ENCODING = "Content-Transfer-Encoding"; // standard: 8bit
    public static final String KEY_HEADER_SOURCE = "Header-Source";
    private HashMap<String,String> headers = new HashMap<>();

    private Context context;
    private boolean afterSubject = false;

    public boolean parseHeaderData(BufferedReader inReader, String messageId, Context context) throws IOException{
        this.context = context;
        headers.put(KEY_MESSAGE_ID, messageId);
        MessageHeaderDecoder decoder = new MessageHeaderDecoder();
        boolean success = true;
        String nextLine = "";
        StringBuilder sb = new StringBuilder();
        while((nextLine=inReader.readLine()) != null) {
            sb.append(nextLine).append("\n");
            String dec;
            try {
                dec = decoder.decode(nextLine);
            } catch (DecoderException e) {
                dec  = nextLine;
                success = false;
            }
            extractLine(dec);
        }
        inReader.close();
        headers.put(KEY_HEADER_SOURCE, sb.toString());
        return success;
    }

    public String getValue(String key) {
        return headers.get(key);
    }

    private void extractLine(String s) {
        if (s.startsWith(KEY_FROM)) {
            String from = s.replace("From: ", "");
            headers.put(KEY_FROM, from);
            NNTPFromFieldFormatter fm = new NNTPFromFieldFormatter(from);
            headers.put(KEY_NAME, fm.getFullName());
            headers.put(KEY_EMAIL, fm.getEmail());
        } else if (s.startsWith(KEY_SUBJECT)) {
            headers.put(KEY_SUBJECT, s.replace("Subject: ", ""));
            afterSubject = true;
        } else if (s.startsWith(KEY_DATE)) {
            String date = new NNTPDateFormatter().getPrettyDateString(s.replace("Date: ", ""), NNTPDateFormatter.DATE_PATTERN_MSG_HEADER, context);
            headers.put(KEY_DATE, date);
        } else if (s.startsWith(KEY_CONTENT_TYPE)) {
            if (s.toUpperCase().contains("UTF-8")) {
                headers.put(KEY_CHARSET, SupportedEncodings.UTF_8);
            } else {
                headers.put(KEY_CHARSET, SupportedEncodings.ISO_8859_15); //For now we keep these 2 formats because they work for sure.
            }
            headers.put(KEY_CONTENT_TYPE, s.substring(0,s.indexOf(";")).replace("Content-Type: ", ""));
        } else if (afterSubject) {
            if(s.startsWith(" ")) {
                headers.put(KEY_SUBJECT, headers.get(KEY_SUBJECT) + s.substring(1));
            } else {
                afterSubject = false;
            }
        }
    }

}
