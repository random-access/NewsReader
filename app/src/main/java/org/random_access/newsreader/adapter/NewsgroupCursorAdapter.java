package org.random_access.newsreader.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.random_access.newsreader.R;
import org.random_access.newsreader.ShowNewsgroupsActivity;
import org.random_access.newsreader.queries.MessageQueries;


/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.06.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class NewsgroupCursorAdapter extends CursorAdapter {

    public NewsgroupCursorAdapter(Context context, Cursor cursor)  {
        super(context, cursor, 0);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_newsgroup, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvTitle = (TextView) view.findViewById(R.id.group_title);
        TextView tvNewNews = (TextView) view.findViewById(R.id.new_news);
        String name = cursor.getString(ShowNewsgroupsActivity.COL_NEWSGROUP_NAME);
        String title = cursor.getString(ShowNewsgroupsActivity.COL_NEWSGROUP_TITLE);
        int newNewsCount= new MessageQueries(context).getNewMessagesCount(cursor.getLong(ShowNewsgroupsActivity.COL_NEWSGROUP_ID));
        tvTitle.setText(TextUtils.isEmpty(title) ? name : title);
        tvNewNews.setText(Integer.toString(newNewsCount));
        tvNewNews.setVisibility(newNewsCount == 0 ? View.GONE : View.VISIBLE);
    }



}
