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
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 18.05.15 <br>
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
        return messageHeaders.size() - 1 - position;
    }

    @Override
    public Object getItem(int position) {
        return messageHeaders.get(messageHeaders.size() - 1 - position);
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
        NNTPMessageHeader currentElem = (NNTPMessageHeader)getItem(position);
        String name = currentElem.getFullName();
        String email = currentElem.getEmail();
        holder.title.setText(currentElem.getSubject());
        holder.date.setText(currentElem.getDate());
        holder.from.setText(TextUtils.isEmpty(name) ? email : name);
        return newView;
    }


    static class ViewHolder{
        TextView title;
        TextView date;
        TextView from;
    }
}
