package org.random_access.newsreader.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.random_access.newsreader.R;
import org.random_access.newsreader.nntp.NNTPDateFormatter;
import org.random_access.newsreader.queries.MessageHierarchyQueries;
import org.random_access.newsreader.queries.MessageQueries;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.07.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class MessageCursorAdapter extends CursorAdapter {

    private static final String TAG = MessageCursorAdapter.class.getSimpleName();

    private TextView title;
    private TextView date;
    private TextView from;
    private ImageButton btnShowChildren;

    private MessageHierarchyQueries messageHierarchyQueries;
    private Boolean showOnlyTopItems;

    public MessageCursorAdapter(Context context, Cursor cursor, Boolean showOnlyTopItems) {
        super(context, cursor, 0);
        messageHierarchyQueries = new MessageHierarchyQueries(context);
        this.showOnlyTopItems = showOnlyTopItems;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        boolean isParent = showOnlyTopItems && messageHierarchyQueries.hasMessageChildren(cursor.getLong(MessageQueries.COL_ID));
        if (isParent) {
            return LayoutInflater.from(context).inflate(R.layout.item_message_parent, parent, false);
        } else {
            return LayoutInflater.from(context).inflate(R.layout.item_message_simple, parent, false);
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        boolean isNew = cursor.getInt(MessageQueries.COL_NEW) == 1;

        title = (TextView) view.findViewById(R.id.message_title);
        date = (TextView) view.findViewById(R.id.message_date);
        from = (TextView) view.findViewById(R.id.message_from);


        title.setText(cursor.getString(MessageQueries.COL_SUBJECT));
        title.setTextColor(isNew ? context.getResources().getColor(R.color.black) : context.getResources().getColor(R.color.grey));
        date.setText(NNTPDateFormatter.getPrettyDateString(cursor.getLong(MessageQueries.COL_DATE), context));
        date.setTextColor(isNew ? context.getResources().getColor(R.color.light_blue) : context.getResources().getColor(R.color.dark_grey));
        from.setText(cursor.getString(MessageQueries.COL_FROM_NAME));
        from.setTextColor(isNew ? context.getResources().getColor(R.color.black) : context.getResources().getColor(R.color.dark_grey));
        boolean isParent =  showOnlyTopItems && messageHierarchyQueries.hasMessageChildren(cursor.getLong(MessageQueries.COL_ID));
        btnShowChildren = (ImageButton) view.findViewById(R.id.btn_show_children);
        if (btnShowChildren != null) {
            btnShowChildren.setColorFilter(context.getResources().getColor(R.color.light_blue));
            btnShowChildren.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }
}
