package org.random_access.newsreader.queries;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.06.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
class QueryHelper {

    public static int count(Context context, Uri uri,String selection,String[] selectionArgs) {
        Cursor cursor = context.getContentResolver().query(uri,new String[] {"count(*) AS count"},
                selection, selectionArgs, null);
        if (cursor.getCount() == 0) {
            cursor.close();
            return 0;
        } else {
            cursor.moveToFirst();
            int result = cursor.getInt(0);
            cursor.close();
            return result;
        }
    }

    public static String makePlaceholderArray(int length) {
        if (length < 1) {
            throw new RuntimeException("No placeholders");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append("?, ");
        }
        sb.replace(sb.length()-2, sb.length(), "");
        return sb.toString();
    }
}
