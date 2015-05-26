package org.random_access.newsreader.nntp;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Project: FlashCards Manager for Android
 * Date: 25.05.15
 * Author: Monika Schrenk
 * E-Mail: software@random-access.org
 */
public class HeaderData {

    public static final String KEY_FROM = "From";
    public static final String KEY_CONTENT_TYPE = "Content-Type";
    public static final String KEY_CHARSET = "Charset";
    public static final String KEY_DATE = "Date";
    public static final String KEY_SUBJECT = "Subject";

    private HashMap<String,String> headers = new HashMap<>();

    private String charset;

    public void parseHeaderData(BufferedReader inReader) throws IOException{
        String nextLine = "";
        while((nextLine=inReader.readLine()) != null) {
            extractLine(nextLine);
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
        } else if (s.startsWith(KEY_DATE)) {
            headers.put(KEY_DATE, s.replace("Date: ", ""));
        } else if (s.startsWith(KEY_CONTENT_TYPE)) {
            if (s.toLowerCase().contains("utf-8")) {
                charset = "UTF-8";
            } else {
                charset = "ISO-8859-1"; //For now we keep these 2 formats because they work for sure.
            }
            headers.put(KEY_CONTENT_TYPE, s.substring(0,s.indexOf(";")));
            headers.put(KEY_CHARSET, "Charset: " + charset);
        }
    }

}
