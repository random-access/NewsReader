package org.random_access.newsreader.nntp;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.codec.DecoderException;
import org.random_access.newsreader.queries.MessageQueries;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * <b>Project:</b> FlashCards Manager for Android <br>
 * <b>Date:</b> 26.06.15 <br>
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
    private String references;
    private long[] refIds;
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
    public static final String KEY_REFERENCES = "References";
    public static final String KEY_HEADER_SOURCE = "Header-Source";
    private HashMap<String,String> headers = new HashMap<>();

    private String lastKey = "";

    public boolean parseHeaderData(BufferedReader inReader, String messageId, Context context) throws IOException {
        this.messageId = messageId;
        // headers.put(KEY_MESSAGE_ID, messageId);
        MessageHeaderDecoder decoder = new MessageHeaderDecoder();
        boolean success = true;
        String nextLine;
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
        parseFullNameAndEmail();
        parseReferences(context);
        this.headerSource = sb.toString();
        return success;
    }

    private void extractLine(String s) {
        if (s.startsWith(KEY_FROM)) {
            sender = s.replace("From: ", "");
            lastKey = KEY_FROM;
        } else if ( s.startsWith(" ") && lastKey.equals(KEY_FROM)){
            sender += s.substring(1);
        } else if (s.startsWith(KEY_SUBJECT)) {
            this.subject = s.replace("Subject: ", "");
            lastKey = KEY_SUBJECT;
        } else if (s.startsWith(" ") && lastKey.equals(KEY_SUBJECT)) {
            this.subject += s.substring(1);
        } else if (s.startsWith(KEY_DATE)) {
            this.date = s.replace("Date: ", "");
            lastKey = "";
        } else if (s.startsWith(KEY_CONTENT_TYPE)) {
            if (s.toUpperCase().contains("UTF-8")) {
                this.charset = SupportedEncodings.UTF_8;
            } else {
                this.charset = SupportedEncodings.ISO_8859_15;
                //For now we keep these 2 formats because they work for sure.
            }
            this.contentType = s.substring(0,s.indexOf(";")).replace("Content-Type: ", "");
            lastKey = "";
        } else if (s.startsWith(KEY_REFERENCES)) {
            references = s.replace("References: ", "");
            lastKey = KEY_REFERENCES;
        } else if (s.startsWith(" ") && lastKey.equals(KEY_REFERENCES)) {
            references += s;
        } else {
            lastKey = "";
        }
    }

    private void parseFullNameAndEmail() {
        NNTPFromFieldFormatter fm = new NNTPFromFieldFormatter(sender);
        fullName = fm.getFullName();
        email = fm.getEmail();
    }

    private void parseReferences(Context context) {
        if (!TextUtils.isEmpty(references)) {
            Log.d(TAG, "References: " + references);
            String[] refs = references.split(" ");
            MessageQueries messageQueries = new MessageQueries(context);
            refIds = new long[refs.length];
            int currentIndex = 0;
            for (String s : refs) {
                long currentId = messageQueries.getIdFromMessageId(s);
                if (currentId != -1) {
                    refIds[currentIndex] = currentId;
                    currentIndex++;
                }
            }
            refIds = Arrays.copyOf(refIds, currentIndex);
        } else {
            refIds = new long[0];
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

    public long[] getRefIds() {
        return refIds;
    }

    public String getHeaderSource() {
        return headerSource;
    }
}
