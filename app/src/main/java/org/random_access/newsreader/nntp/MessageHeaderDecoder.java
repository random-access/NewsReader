package org.random_access.newsreader.nntp;

import android.util.Log;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.apache.commons.net.util.Base64;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>Project:</b> FlashCards Manager for Android <br>
 * <b>Date:</b> 29.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class MessageHeaderDecoder {

    private static final String TAG = MessageHeaderDecoder.class.getSimpleName();

    private static final String BASE_64 = "B";
    private static final String QUOTED_PRINTABLE = "Q";



    String decodePattern = "=\\?(.*?)\\?([bBqQ])\\?(.*?)\\?=";

    //=?UTF-8?B?w6TDtsO8?=
    public String decode(String text) throws DecoderException {
        StringBuilder sb = new StringBuilder();
        Pattern pattern = Pattern.compile(decodePattern);
        Matcher matcher = pattern.matcher(text);
        int startUnencoded = 0;
        while (matcher.find()) {
            //System.out.println(matcher.group(0));
            if (matcher.start() > startUnencoded) {
                sb.append(text.substring(startUnencoded, matcher.start()));
            }
            String encoding = null;
            Iterator<String> it = SupportedEncodings.getEncodings().values().iterator();
            while(it.hasNext() && encoding == null) {
                String currentTry = it.next();
                if (currentTry.equalsIgnoreCase(matcher.group(1))) {
                    encoding = currentTry;
                    if (matcher.group(2).toUpperCase().equalsIgnoreCase(BASE_64)) {
                        // decode matcher.group(3) with encoding, base64
                        // & append it to sb
                        sb.append(base64Decode(matcher.group(3), encoding));
                    } else if (matcher.group(2).toUpperCase().equalsIgnoreCase(QUOTED_PRINTABLE)){
                        // decode matcher.group(3) with encoding, quotedP
                        // & append it to sb
                        sb.append(quotedPrintableDecode(matcher.group(3), encoding));
                    }
                }
            }
            startUnencoded = matcher.end();
        }
        if (startUnencoded < text.length()) {
            sb.append(text.substring(startUnencoded, text.length()));
        }
        return sb.toString();
    }


    private String base64Decode(String str, String encoding) {
        try {
            return new String(base64Decode(str.getBytes(encoding)), encoding); // get rid of suffix
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
            return new String(quotedPrintableDecode(str.getBytes(encoding)), encoding);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported encoding");
            return str;
        }
    }

    private byte[] quotedPrintableDecode(byte[] byteArray) throws DecoderException{
        return QuotedPrintableCodec.decodeQuotedPrintable(byteArray);
    }

}
