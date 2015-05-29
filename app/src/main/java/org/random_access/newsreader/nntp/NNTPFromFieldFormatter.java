package org.random_access.newsreader.nntp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>Project:</b> FlashCards Manager for Android <br>
 * <b>Date:</b> 29.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class NNTPFromFieldFormatter {

    private static final String decodePatternMail = "(<[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}>)";
    private String fullName = "";
    private String email = "";

    public NNTPFromFieldFormatter(String fromField) {
        split(fromField);
    }

    private void split(String text) {
        Pattern pattern = Pattern.compile(decodePatternMail);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            email = matcher.group(0);
            text = text.replace(email, "");
            email = email.substring(1,email.length()-1);
        }
        text = text.replaceAll("[_\"\'()]", " ");
        String[] nameArray = text.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String s : nameArray) {
            sb.append(s).append(" ");
        }
        fullName = sb.toString().trim();
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }
}
