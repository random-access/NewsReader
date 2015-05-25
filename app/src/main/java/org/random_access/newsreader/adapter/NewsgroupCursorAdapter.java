package org.random_access.newsreader.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.random_access.newsreader.R;
import org.random_access.newsreader.ShowNewsgroupsActivity;


/**
 * Project: FlashCards Manager for Android
 * Date: 23.05.15
 * Author: Monika Schrenk
 * E-Mail: software@random-access.org
 */
public class NewsgroupCursorAdapter extends CursorAdapter {

    private Resources res;
    private TextView tvTitle;
    private TextView tvNewNews;

    public NewsgroupCursorAdapter(Context context, Cursor cursor)  {
        super(context, cursor, 0);
        res = context.getResources();
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_newsgroup, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        tvTitle = (TextView)view.findViewById(R.id.group_title);
        tvNewNews = (TextView)view.findViewById(R.id.new_news);
        String name = cursor.getString(ShowNewsgroupsActivity.COL_NEWSGROUP_NAME);
        String title = cursor.getString(ShowNewsgroupsActivity.COL_NEWSGROUP_TITLE);
        String newNews = 0 + ""; //TODO count new news
        tvTitle.setText(TextUtils.isEmpty(title) ? name : title);
        tvNewNews.setText(newNews);

    }



}
