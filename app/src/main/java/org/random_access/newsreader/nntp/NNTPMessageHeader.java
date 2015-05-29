package org.random_access.newsreader.nntp;

import android.util.Log;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.apache.commons.net.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static final String KEY_FROM = "From";
    public static final String KEY_CONTENT_TYPE = "Content-Type";
    public static final String KEY_CHARSET = "Charset";
    public static final String KEY_DATE = "Date";
    public static final String KEY_SUBJECT = "Subject";
    public static final String KEY_TRANSFER_ENCODING = "Content-Transfer-Encoding"; // standard: 8bit
    private HashMap<String,String> headers = new HashMap<>();

    private String messageId;
    private String charset;
    private String headerSrc;

    private boolean afterSubject = false;

    public boolean parseHeaderData(BufferedReader inReader) throws IOException{
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
        headerSrc = sb.toString();
        return success;
    }

    public String getHeaderSrc() {
        return headerSrc;
    }

    public String getMessageCharset() {
        return charset;
    }

    public String getValue(String key) {
        return headers.get(key);
    }

    private void extractLine(String s) {
        if (s.startsWith(KEY_FROM)) {
            headers.put(KEY_FROM, s.replace("From: ", ""));
        } else if (s.startsWith(KEY_SUBJECT)) {
            headers.put(KEY_SUBJECT, s.replace("Subject: ", ""));
            afterSubject = true;
        } else if (s.startsWith(KEY_DATE)) {
            headers.put(KEY_DATE, s.replace("Date: ", ""));
        } else if (s.startsWith(KEY_CONTENT_TYPE)) {
            if (s.toLowerCase().contains("utf-8")) {
                charset = SupportedEncodings.UTF_8;
            } else {
                charset = SupportedEncodings.ISO_8859_15; //For now we keep these 2 formats because they work for sure.
            }
            headers.put(KEY_CONTENT_TYPE, s.substring(0,s.indexOf(";")));
            headers.put(KEY_CHARSET, "Charset: " + charset);
        } else if (afterSubject) {
            if(s.startsWith(" ")) {
                headers.put(KEY_SUBJECT, headers.get(KEY_SUBJECT) + s.substring(1));
            } else {
                afterSubject = false;
            }
        }
    }

}
