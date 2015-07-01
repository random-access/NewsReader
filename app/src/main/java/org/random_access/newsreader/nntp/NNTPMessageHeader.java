package org.random_access.newsreader.nntp;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.codec.DecoderException;
import org.random_access.newsreader.queries.MessageQueries;
import org.random_access.newsreader.sync.NNTPSyncDummyAccount;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;

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

    public static final String KEY_FROM = "From";
    public static final String KEY_NEWSGROUPS = "Newsgroups";
    public static final String KEY_CONTENT_TYPE = "Content-Type"; // for now assuming text/plain
    public static final String KEY_CHARSET = "Charset";
    public static final String KEY_DATE = "Date";
    public static final String KEY_SUBJECT = "Subject";
    public static final String KEY_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    public static final String KEY_REFERENCES = "References";

    private String lastKey = "";

    public boolean parseHeaderData(BufferedReader inReader, String messageId, Context context) throws IOException {
        this.messageId = messageId;
        MessageDecoder decoder = new MessageDecoder();
        boolean success = true;
        String nextLine;
        StringBuilder sb = new StringBuilder();
        while((nextLine=inReader.readLine()) != null) {
            sb.append(nextLine).append("\n");
            String dec;
            try {
                dec = decoder.decodeHeader(nextLine);
            } catch (DecoderException e) {
                dec  = nextLine;
                Log.e(TAG, "Couldn't decode " + nextLine);
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

    private String buildHeader(String fullName, String email, String[] newsgroups, String charset, String date, String subject, String transferEncoding,long[] refIds) {
        StringBuilder sb = new StringBuilder();
        sb.append(KEY_FROM).append(": \"").append(fullName).append("\" ").append("<").append(email).append(">\n")
                .append(KEY_NEWSGROUPS).append(": ").append(concatNewsgroups(newsgroups)).append("\n")
                .append(KEY_SUBJECT).append(": ").append(subject).append("\n")
                .append(KEY_DATE).append(": ");
                // TODO

        return null;
    }

    private String concatNewsgroups (String[] newsgroups) {
        if (newsgroups == null || newsgroups.length == 0) {
            return "";
        } else if (newsgroups.length == 1) {
            return newsgroups [0];
        } else {
            StringBuilder sb = new StringBuilder();
            for (String s : newsgroups) {
                sb.append(s).append(", ");
            }
            sb.replace(sb.length()-2, sb.length()-1, "");
            return sb.toString();
        }
    }

    private void extractLine(String s) {
        if (s.startsWith(KEY_FROM)) {
            sender = s.replace(KEY_FROM + ": ", "");
            lastKey = KEY_FROM;
        } else if ( s.startsWith(" ") && lastKey.equals(KEY_FROM)){
            sender += s.substring(1);
        } else if (s.startsWith(KEY_SUBJECT)) {
            this.subject = s.replace(KEY_SUBJECT + ": ", "");
            lastKey = KEY_SUBJECT;
        } else if (s.startsWith(" ") && lastKey.equals(KEY_SUBJECT)) {
            this.subject += s.substring(1);
        } else if (s.startsWith(KEY_DATE)) {
            this.date = s.replace(KEY_DATE + ": ", "");
            lastKey = "";
        } else if (s.startsWith(KEY_CONTENT_TYPE)) {
            this.contentType = parseContentTypeAndCharset(s);
            lastKey = "";
        } else  if(s.startsWith(KEY_TRANSFER_ENCODING)){
            transferEncoding = parseContentTransferEncoding(s);
        } else if (s.startsWith(KEY_REFERENCES)) {
            references = s.replace(KEY_REFERENCES + ": ", "");
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

    private String parseContentTypeAndCharset(String s) {
        if (s.toUpperCase().contains(SupportedHeaderEncodings.UTF_8)) {
            this.charset = SupportedHeaderEncodings.UTF_8;
        } else {
            this.charset = SupportedHeaderEncodings.ISO_8859_15;
            //For now we keep these 2 formats because they work for sure
        }
        return s.substring(0,s.indexOf(";")).replace(KEY_CONTENT_TYPE + ": ", "");
    }

    private String parseContentTransferEncoding(String s) {
        String enc = s.replace(KEY_TRANSFER_ENCODING + ": ", "").toLowerCase();
        String result = "";
        switch (enc) {
            case SupportedBodyEncodings._7BIT:
                result = SupportedBodyEncodings._7BIT;
                break;
            case SupportedBodyEncodings._8BIT:
                result = SupportedBodyEncodings._8BIT;
                break;
            case SupportedBodyEncodings.QUOTED_PRINTABLE:
                result = SupportedBodyEncodings.QUOTED_PRINTABLE;
                break;
            case SupportedBodyEncodings.BASE_64:
                result = SupportedBodyEncodings.BASE_64;
                break;
            default:
                Log.e(TAG, "Unsupported transfer encoding: [" + enc + "]");
                break;
        }
        return result;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId() {
        // TODO create message ID
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) { this.email = email; }

    public String getCharset() { return charset; }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getDate() {
        return date;
    }

    public void setDate (String date) {this.date = date; }

    public String getSubject() {
        return subject;
    }

    public void  setSubject(String subject) { this.subject = subject; }

    public String getTransferEncoding() {
        return transferEncoding;
    }

    public void setTransferEncoding(String transferEncoding) { this.transferEncoding = transferEncoding; }

    public long[] getRefIds() {
        return refIds;
    }

    public void setRefIds(long[] refIds) { this.refIds = refIds; }

    public String getHeaderSource() {
        return headerSource;
    }
}
