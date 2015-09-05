package org.random_access.newsreader.nntp;

import android.util.Log;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.apache.commons.net.util.Base64;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.06.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
class MessageDecoder {

    private static final String TAG = MessageDecoder.class.getSimpleName();

    private static final String BASE_64_SHORT = "B";
    private static final String QUOTED_PRINTABLE_SHORT = "Q";



    private static final String decodePattern = "=\\?(.*?)\\?([bBqQ])\\?(.*?)\\?=";

    //=?UTF-8?B?w6TDtsO8?=
    public String decodeHeader(String text) throws DecoderException {
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
            Iterator<String> it = SupportedCharsets.getEncodings().values().iterator();
            while(it.hasNext() && encoding == null) {
                String currentTry = it.next();
                if (currentTry.equalsIgnoreCase(matcher.group(1))) {
                    encoding = currentTry;
                    if (matcher.group(2).toUpperCase().equalsIgnoreCase(BASE_64_SHORT)) {
                        // decode matcher.group(3) with encoding, base64
                        // & append it to sb
                        sb.append(base64Decode(matcher.group(3), encoding));
                    } else if (matcher.group(2).toUpperCase().equalsIgnoreCase(QUOTED_PRINTABLE_SHORT)){
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

    public String decodeBody (String text, String charset, String encoding) {
        switch(encoding) {
            case SupportedEncodings.BASE_64:
                return base64Decode(text, charset);
            case SupportedEncodings.QUOTED_PRINTABLE:
                try {
                    return quotedPrintableDecode(text, charset);
                } catch (DecoderException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Unsupported charset: [" + charset + "] - encoding: " + encoding);
                    return text;
                }

            default:
                return text;
        }
    }


    private String base64Decode(String str, String charset) {
        try {
            return new String(base64Decode(str.getBytes(charset)), charset);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported charset: " + charset);
            return str;
        }
    }

    private byte[] base64Decode(byte[] byteArray) {
        return Base64.decodeBase64(byteArray);
    }


    private String quotedPrintableDecode (String str, String charset) throws DecoderException {
        try {
            // replace all "_" with " " -> header encoding often contains "_" for spaces.
            // maybe should find a way to distinguish between space replacement and real "_"
            return new String(quotedPrintableDecode(str.getBytes(charset)), charset).replace('_', ' ');
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported charset: " + charset);
            return str;
        }
    }

    private byte[] quotedPrintableDecode(byte[] byteArray) throws DecoderException{
        return QuotedPrintableCodec.decodeQuotedPrintable(byteArray);
    }

}
