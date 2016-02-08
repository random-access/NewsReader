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

import org.random_access.newsreader.MsgHierarchyView;
import org.random_access.newsreader.R;
import org.random_access.newsreader.nntp.NNTPDateFormatter;
import org.random_access.newsreader.queries.MessageQueries;

/**
 * <b>Project:<b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 27.06.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class MessageChildrenCursorAdapter extends CursorAdapter {

    private static final String TAG = MessageChildrenCursorAdapter.class.getSimpleName();

    public MessageChildrenCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_message_child, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        boolean isNew = cursor.getInt(MessageQueries.COL_NEW) == 1;
        boolean isFresh = cursor.getInt(MessageQueries.COL_FRESH) == 1;

        TextView title = (TextView) view.findViewById(R.id.message_title);
        TextView date = (TextView) view.findViewById(R.id.message_date);
        TextView from = (TextView) view.findViewById(R.id.message_from);
        ImageView fresh = (ImageView) view.findViewById(R.id.message_fresh);
        MsgHierarchyView levelIcon = (MsgHierarchyView) view.findViewById(R.id.ic_level);

        title.setText(cursor.getString(MessageQueries.COL_SUBJECT));
        title.setTextColor(isNew ? ContextCompat.getColor(context, R.color.black) : ContextCompat.getColor(context, R.color.grey));
        date.setText(NNTPDateFormatter.getPrettyDateString(cursor.getLong(MessageQueries.COL_DATE), context));
        date.setTextColor(isNew ? ContextCompat.getColor(context, R.color.light_blue) : ContextCompat.getColor(context, R.color.dark_grey));
        from.setText(cursor.getString(MessageQueries.COL_FROM_NAME));
        from.setTextColor(isNew ? ContextCompat.getColor(context, R.color.black) : ContextCompat.getColor(context, R.color.dark_grey));
        fresh.setVisibility(isFresh ? View.VISIBLE : View.INVISIBLE);
        levelIcon.setLevel(cursor.getInt(MessageQueries.COL_LEVEL));
    }

}
