package org.random_access.newsreader.nntp;

import java.util.HashMap;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.06.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class SupportedCharsets {

    private static final HashMap<Integer, String> encodings;
    public static final String UTF_8 = "UTF-8";
    public static final String ISO_8859_15 = "ISO-8859-15";
    public static final String ISO_8859_1 = "ISO-8859-1";
    public static final String WINDOWS_1252 = "WINDOWS-1252";

    static {
        encodings = new HashMap<>();
        encodings.put(0, UTF_8);
        encodings.put(1, ISO_8859_1);
        encodings.put(2, ISO_8859_15);
        encodings.put(3, WINDOWS_1252);
    }

    public static HashMap<Integer, String> getEncodings () {
        return encodings;
    }

}
