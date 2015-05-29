package org.random_access.newsreader.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.random_access.newsreader.EditSubscriptionsActivity;
import org.random_access.newsreader.nntp.NNTPMessageHeader;

import java.util.ArrayList;

/**
 * <b>Project:</b> FlashCards Manager for Android <br>
 * <b>Date:</b> 29.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class MessageAdapter extends BaseAdapter {

    private static final String TAG = MessageAdapter.class.getSimpleName();

    private Context context;
    private int layoutId;
    private ArrayList<NNTPMessageHeader> subscriptions;

    public MessageAdapter (Context context, int layoutId, ArrayList<NNTPMessageHeader> messageHeaders) {
        this.context = context;
        this. layoutId = layoutId;
        this.subscriptions = messageHeaders;
        Log.d(TAG, "Number of messages: " + messageHeaders.size());
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
