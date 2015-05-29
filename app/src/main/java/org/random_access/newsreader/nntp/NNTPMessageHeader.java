package org.random_access.newsreader.nntp;

import android.util.Log;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.apache.commons.net.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

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
    private HashMap<String,String> headers = new HashMap<>();

    private static final String UTF_8 = "UTF-8";
    private static final String ISO_8859_15 = "ISO-8859-15";
    private static final String ISO_8859_1 = "ISO-8859-1";
    private static final String WINDOWS_1252 = "WINDOWS-1252";
    private static HashMap<Integer,String> encodings;

    static {
        encodings = new HashMap<>();
        encodings.put(1, UTF_8);
        encodings.put(2, ISO_8859_1);
        encodings.put(3, ISO_8859_15);
        encodings.put(4, WINDOWS_1252);
    }

    private static final String prefix = "=?";
    private static final String suffix = "?=";

    private static final String base64 = "B";
    private static final String quotedPrintable = "Q";

    private String charset;

    private boolean afterSubject = false;

    public void parseHeaderData(BufferedReader inReader) throws IOException{
        String nextLine = "";
        while((nextLine=inReader.readLine()) != null) {
            String dec = decode(nextLine);
            extractLine(dec);
        }
        inReader.close();
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
                charset = UTF_8;
            } else {
                charset = ISO_8859_15; //For now we keep these 2 formats because they work for sure.
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

    //=?UTF-8?B?w6TDtsO8?=
    public String decode(String raw) {
        try {
            Iterator<String> it = encodings.values().iterator();
            while(it.hasNext()) {
                String encoding = it.next();
                if (raw.toUpperCase().contains(prefix + encoding + "?") && raw.contains(suffix)) {
                    String[] parts = raw.split("=\\?");
                    for (int i = 0; i < parts.length; i++) {
                        if (parts[i].toUpperCase().startsWith(encoding + "?")) {
                            Log.d(TAG, "Encoded: " + parts[i]);
                            String[] codeParts = parts[i].split("\\?");
                            if (codeParts[1].equalsIgnoreCase(base64)) {
                                parts[i] = base64Decode(codeParts[2], encoding);
                            } else if (codeParts[1].equalsIgnoreCase(quotedPrintable)) {
                                parts[i] = quotedPrintableDecode(codeParts[2], encoding);
                            }
                        }
                    }
                    Log.d(TAG, arrayToString(parts));
                    return arrayToString(parts);
                }
            }
        } catch (DecoderException e) {
            Log.d(TAG, e.getMessage());
        }
        return raw;
    }

    private String arrayToString(String[] array) {
        StringBuilder sb = new StringBuilder();
        for (String s : array) {
            sb.append(s);
        }
        return sb.toString();
    }

    private String base64Decode(String str, String encoding) {
        try {
            return new String(base64Decode(str.replace("?" + "b" + "?", "?" + base64 + "?") // parse small b to B
                    .replace(prefix + encoding + "?" + base64 +"?", "") // get rid of intro
                    .replace(suffix, "").getBytes(encoding)), encoding); // get rid of suffix
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported encoding");
            return str;
        }
    }

    private byte[] base64Decode(byte[] byteArray) {
        return Base64.decodeBase64(byteArray);
    }


    private String quotedPrintableDecode (String str, String encoding) throws DecoderException {
        try {
            return new String(quotedPrintableDecode(str.replace("=?" + encoding + "?Q?", "").replace("?=", "").getBytes(encoding)), encoding);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported encoding");
            return str;
        }
    }

    private byte[] quotedPrintableDecode(byte[] byteArray) throws DecoderException{
        return QuotedPrintableCodec.decodeQuotedPrintable(byteArray);
    }

}
