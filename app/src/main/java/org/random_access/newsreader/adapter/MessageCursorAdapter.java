package org.random_access.newsreader.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.random_access.newsreader.R;
import org.random_access.newsreader.ShowNewsgroupsActivity;
import org.random_access.newsreader.ShowSingleArticleActivity;
import org.random_access.newsreader.nntp.NNTPDateFormatter;
import org.random_access.newsreader.queries.MessageQueries;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.07.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class MessageCursorAdapter extends CursorAdapter {

    private TextView title;
    private TextView date;
    private TextView from;

    public MessageCursorAdapter(Context context, Cursor cursor)  {
        super(context, cursor, 0);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_message_template, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        title = (TextView) view.findViewById(R.id.message_title);
        date = (TextView) view.findViewById(R.id.message_date);
        from = (TextView) view.findViewById(R.id.message_from);

        boolean isNew = cursor.getInt(MessageQueries.COL_NEW) == 1;

        title.setText(cursor.getString(MessageQueries.COL_SUBJECT));
        title.setTextColor(isNew ? context.getResources().getColor(R.color.black) : context.getResources().getColor(R.color.grey));
        date.setText(NNTPDateFormatter.getPrettyDateString(cursor.getLong(MessageQueries.COL_DATE), context));
        date.setTextColor(isNew ? context.getResources().getColor(R.color.light_blue) : context.getResources().getColor(R.color.dark_grey));
        from.setText(cursor.getString(MessageQueries.COL_FROM_NAME));
        from.setTextColor(isNew ? context.getResources().getColor(R.color.black) : context.getResources().getColor(R.color.dark_grey));
    }
}
