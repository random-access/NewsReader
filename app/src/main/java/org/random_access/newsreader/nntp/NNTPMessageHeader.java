package org.random_access.newsreader.nntp;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.net.nntp.SimpleNNTPHeader;
import org.random_access.newsreader.queries.MessageQueries;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;

/**
 * <b>Project:</b> Newsreader for Android <br>
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
    private String contentType = DEFAULT_CONTENT_TYPE;
    private String charset = DEFAULT_CHARSET;
    private String date;
    private String subject;
    private String transferEncoding = DEFAULT_TRANSFER_ENCODING;
    private String references = "";
    private long[] refIds;
    private long parentMsg = -1;
    private long rootMsg = -1;
    private int level = 0;
    private String headerSource;

    private static final String DEFAULT_CONTENT_TYPE = "text/plain";
    private static final String DEFAULT_USER_AGENT = "NewsReader for Android/1.0 http://www.random-access.org/newsreader";
    private static final String DEFAULT_CHARSET = SupportedCharsets.UTF_8;
    private static final String DEFAULT_TRANSFER_ENCODING = SupportedEncodings._8BIT;

    private static final String KEY_FROM = "From";
    private static final String KEY_NEWSGROUPS = "Newsgroups";
    private static final String KEY_CONTENT_TYPE = "Content-Type";
    private static final String KEY_CHARSET = "charset";
    private static final String KEY_DATE = "Date";
    private static final String KEY_SUBJECT = "Subject";
    private static final String KEY_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    private static final String KEY_REFERENCES = "References"; // TODO kick out duplicates
    private static final String KEY_IN_REPLY_TO = "In-Reply-To";
    private static final String KEY_USER_AGENT = "User-Agent";

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

    public SimpleNNTPHeader buildHeader(String fullName, String email, String newsgroup, long date, String replyIds, String subject, Context context) {
        SimpleNNTPHeader header = new SimpleNNTPHeader(fullName + " <" + email + ">", subject);
        header.addNewsgroup(newsgroup);
        header.addHeaderField(KEY_USER_AGENT, DEFAULT_USER_AGENT);
        header.addHeaderField(KEY_REFERENCES, replyIds);
        // header.addHeaderField(KEY_DATE, date); TODO
        header.addHeaderField(KEY_IN_REPLY_TO, replyIds.substring(replyIds.lastIndexOf("<"), replyIds.length()));
        header.addHeaderField(KEY_TRANSFER_ENCODING, DEFAULT_TRANSFER_ENCODING);
        header.addHeaderField(KEY_CONTENT_TYPE, DEFAULT_CONTENT_TYPE + "; " + KEY_CHARSET + "=" + DEFAULT_CHARSET);
        Log.d(TAG, header.toString());
        return header;
    }

   /* private String concatNewsgroups (String[] newsgroups) {
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

    private String getInReplyToString(long[] refIds, Context context) {
        MessageQueries messageQueries = new MessageQueries(context);
        if (refIds == null || refIds.length == 0) {
            return "";
        } else {
            return messageQueries.getMessageIdFromId(refIds[refIds.length - 1]);
        }
    }

     private String getReferencesAsString(long[] refIds, Context context) {
        MessageQueries messageQueries = new MessageQueries(context);
        if (refIds == null || refIds.length == 0) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            for (long l : refIds) {
                String msgId = messageQueries.getMessageIdFromId(l);
                if (!TextUtils.isEmpty(msgId)) {
                    sb.append(msgId).append(" ");
                }
            }
            sb.replace(sb.length()-1, sb.length()-1, "");
            return sb.toString();
        }
    } */

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
            rootMsg = currentIndex > 0 ? refIds[0] : -1;
            parentMsg = currentIndex > 0 ? refIds[currentIndex - 1] : -1;
            level = currentIndex;
            refIds = Arrays.copyOf(refIds, currentIndex);
        } else {
            refIds = new long[0];
        }
    }

    private String parseContentTypeAndCharset(String s) {
        if (s.toUpperCase().contains(SupportedCharsets.ISO_8859_1)) {
            this.charset = SupportedCharsets.ISO_8859_15;
        } else if (s.toUpperCase().contains(SupportedCharsets.WINDOWS_1252)) {
            this.charset = SupportedCharsets.WINDOWS_1252;
        } else {
            this.charset = SupportedCharsets.UTF_8;
            //For now we keep these 3 formats because they work for sure
        }
        return s.substring(0,s.indexOf(";")).replace(KEY_CONTENT_TYPE + ": ", "");
    }

    private String parseContentTransferEncoding(String s) {
        String enc = s.replace(KEY_TRANSFER_ENCODING + ": ", "").toLowerCase();
        String result = "";
        switch (enc) {
            case SupportedEncodings._7BIT:
                result = SupportedEncodings._7BIT;
                break;
            case SupportedEncodings._8BIT:
                result = SupportedEncodings._8BIT;
                break;
            case SupportedEncodings.QUOTED_PRINTABLE:
                result = SupportedEncodings.QUOTED_PRINTABLE;
                break;
            case SupportedEncodings.BASE_64:
                result = SupportedEncodings.BASE_64;
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

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getCharset() { return charset; }

    public String getDate() {
        return date;
    }

    public void setDate (String date) {this.date = date; }

    public String getSubject() {
        return subject;
    }

    public String getTransferEncoding() {
        return transferEncoding;
    }

    public long[] getRefIds() {
        return refIds;
    }

    public long getParentMsg() {
        return parentMsg;
    }

    public long getRootMsg() {
        return rootMsg;
    }

    public int getLevel () {
        return level;
    }

    public String getReferences() {return  references; }

    public String getHeaderSource() {
        return headerSource;
    }
}
