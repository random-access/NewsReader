package org.random_access.newsreader.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.random_access.newsreader.R;
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
    private ArrayList<NNTPMessageHeader> messageHeaders;

    public MessageAdapter (Context context, int layoutId, ArrayList<NNTPMessageHeader> messageHeaders) {
        this.context = context;
        this. layoutId = layoutId;
        this.messageHeaders = messageHeaders;
        Log.d(TAG, "Number of messages: " + messageHeaders.size());
    }

    @Override
    public int getCount() {
        return messageHeaders.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return messageHeaders.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View newView = convertView;
        ViewHolder holder = null;

        if (newView != null) {
            holder = (ViewHolder) newView.getTag();
        } else {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            newView = inflater.inflate(layoutId, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) newView.findViewById(R.id.message_title);
            holder.date = (TextView) newView.findViewById(R.id.message_date);
            holder.from = (TextView) newView.findViewById(R.id.message_from);
            newView.setTag(holder);
        }
        String name = messageHeaders.get(position).getValue(NNTPMessageHeader.KEY_NAME);
        String email = messageHeaders.get(position).getValue(NNTPMessageHeader.KEY_EMAIL);
        holder.title.setText(messageHeaders.get(position).getValue(NNTPMessageHeader.KEY_SUBJECT));
        holder.date.setText(messageHeaders.get(position).getValue(NNTPMessageHeader.KEY_DATE));
        holder.from.setText(TextUtils.isEmpty(name) ? email : name);
        return newView;
    }


    static class ViewHolder{
        TextView title;
        TextView date;
        TextView from;
    }
}
