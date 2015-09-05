package org.random_access.newsreader.nntp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 30.06.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class NNTPMessageBody {

    private static final String TAG = NNTPMessageBody.class.getSimpleName();

    public String parseBodyData(BufferedReader reader, String charset, String encoding) throws IOException{
        String line;
        StringBuilder sbMessageBody = new StringBuilder();
        while((line=reader.readLine()) != null) {
            sbMessageBody.append(line).append("\n");
        }
        reader.close();
        String res = new MessageDecoder().decodeBody(sbMessageBody.toString(), charset, encoding);
        return res;
    }

}
