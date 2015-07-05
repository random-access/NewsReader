package org.random_access.newsreader.nntp;

import java.util.HashMap;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 30.06.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class SupportedEncodings {

    private static final HashMap<Integer, String> encodings;
    public static final String _7BIT = "7bit";
    public static final String _8BIT = "8bit";
    public static final String BASE_64 = "base-64";
    public static final String QUOTED_PRINTABLE = "quoted-printable";

    static {
        encodings = new HashMap<>();
        encodings.put(0, _7BIT);
        encodings.put(1, _8BIT);
        encodings.put(2, BASE_64);
        encodings.put(3, QUOTED_PRINTABLE);
    }

    public static HashMap<Integer, String> getEncodings () {
        return encodings;
    }
}
