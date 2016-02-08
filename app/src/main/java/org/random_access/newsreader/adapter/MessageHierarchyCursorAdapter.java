package org.random_access.newsreader.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.random_access.newsreader.R;
import org.random_access.newsreader.nntp.NNTPDateFormatter;
import org.random_access.newsreader.queries.MessageQueries;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 26.07.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class MessageHierarchyCursorAdapter extends CursorAdapter {

    private static final String TAG = MessageHierarchyCursorAdapter.class.getSimpleName();

    public MessageHierarchyCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_message_parent, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        boolean isNew = cursor.getInt(MessageQueries.COL_NEW) == 1 || cursor.getInt(MessageQueries.COL_NEW) == -1;
            // -1: root messages which have unread children -> should be shown as new
        boolean isFresh = cursor.getInt(MessageQueries.COL_FRESH) == 1 || cursor.getInt(MessageQueries.COL_FRESH) == -1;
            // -1: root messages which have fresh children -> should be shown as fresh

        TextView title = (TextView) view.findViewById(R.id.message_title);
        TextView date = (TextView) view.findViewById(R.id.message_date);
        TextView from = (TextView) view.findViewById(R.id.message_from);
        ImageView imgChildren = (ImageView) view.findViewById(R.id.img_children);
        ImageView fresh = (ImageView) view.findViewById(R.id.message_fresh);

        title.setText(cursor.getString(MessageQueries.COL_SUBJECT));
        title.setTextColor(isNew ? ContextCompat.getColor(context, R.color.black) : ContextCompat.getColor(context, R.color.grey));
        date.setText(NNTPDateFormatter.getPrettyDateString(cursor.getLong(MessageQueries.COL_DATE), context));
        date.setTextColor(isNew ? ContextCompat.getColor(context, R.color.light_blue) : ContextCompat.getColor(context, R.color.dark_grey));
        from.setText(cursor.getString(MessageQueries.COL_FROM_NAME));
        from.setTextColor(isNew ? ContextCompat.getColor(context, R.color.black) : ContextCompat.getColor(context, R.color.dark_grey));
        boolean isParent = new MessageQueries(context).hasMessageChildren(cursor.getLong(MessageQueries.COL_ID));
        imgChildren.setVisibility(isParent ? View.VISIBLE : View.INVISIBLE);
        imgChildren.setColorFilter(ContextCompat.getColor(context, R.color.light_blue));
        fresh.setVisibility(isFresh ? View.VISIBLE : View.INVISIBLE);
    }

}
