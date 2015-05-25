package org.random_access.newsreader.queries;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Project: FlashCards Manager for Android
 * Date: 23.05.15
 * Author: Monika Schrenk
 * E-Mail: software@random-access.org
 */
public class QueryHelper {

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
}
