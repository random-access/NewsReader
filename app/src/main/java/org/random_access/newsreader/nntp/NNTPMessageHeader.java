package org.random_access.newsreader.nntp;

import android.content.Context;

import org.apache.commons.codec.DecoderException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 18.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class NNTPMessageHeader {

    private static final String TAG = NNTPMessageHeader.class.getSimpleName();

    private String messageId;
    private String sender;
    private String fullName;
    private String email;
    private String contentType;
    private String charset;
    private String date;
    private String subject;
    private String transferEncoding;
    private String headerSource;



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
        this.messageId = messageId;
        // headers.put(KEY_MESSAGE_ID, messageId);
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
        this.headerSource = sb.toString();
        // headers.put(KEY_HEADER_SOURCE, sb.toString());
        return success;
    }

   // public String getValue(String key) {
    //    return headers.get(key);
    //}

    private void extractLine(String s) {
        if (s.startsWith(KEY_FROM)) {
            String from = s.replace("From: ", "");
            // headers.put(KEY_FROM, sender);
            this.sender = from;
            NNTPFromFieldFormatter fm = new NNTPFromFieldFormatter(from);
           //  headers.put(KEY_NAME, fm.getFullName());
            this.fullName = fm.getFullName();
            // headers.put(KEY_EMAIL, fm.getEmail());
            this.email = fm.getEmail();
        } else if (s.startsWith(KEY_SUBJECT)) {
           //  headers.put(KEY_SUBJECT, s.replace("Subject: ", ""));
            this.subject = s.replace("Subject: ", "");
            afterSubject = true;
        } else if (s.startsWith(KEY_DATE)) {
            String date = new NNTPDateFormatter().getPrettyDateString(s.replace("Date: ", ""), NNTPDateFormatter.DATE_PATTERN_MSG_HEADER, context);
            this.date = date;
            // headers.put(KEY_DATE, date);
        } else if (s.startsWith(KEY_CONTENT_TYPE)) {
            if (s.toUpperCase().contains("UTF-8")) {
                this.charset = SupportedEncodings.UTF_8;
                // headers.put(KEY_CHARSET, SupportedEncodings.UTF_8);
            } else {
                this.charset = SupportedEncodings.ISO_8859_15;
                // headers.put(KEY_CHARSET, SupportedEncodings.ISO_8859_15); //For now we keep these 2 formats because they work for sure.
            }
            // headers.put(KEY_CONTENT_TYPE, s.substring(0,s.indexOf(";")).replace("Content-Type: ", ""));
            this.contentType = s.substring(0,s.indexOf(";")).replace("Content-Type: ", "");
        } else if (afterSubject) {
            if(s.startsWith(" ")) {
                this.subject += s.substring(1);
                // headers.put(KEY_SUBJECT, headers.get(KEY_SUBJECT) + s.substring(1));
            } else {
                afterSubject = false;
            }
        }
    }

    public String getMessageId() {
        return messageId;
    }

    public String getSender() {
        return sender;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getContentType() {
        return contentType;
    }

    public String getCharset() {
        return charset;
    }

    public String getDate() {
        return date;
    }

    public String getSubject() {
        return subject;
    }

    public String getTransferEncoding() {
        return transferEncoding;
    }

    public String getHeaderSource() {
        return headerSource;
    }
}
