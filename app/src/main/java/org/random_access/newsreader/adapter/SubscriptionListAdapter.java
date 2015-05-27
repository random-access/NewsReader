package org.random_access.newsreader.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.random_access.newsreader.EditSubscriptionsActivity;
import org.random_access.newsreader.R;

import java.util.ArrayList;

/**
 * <b>Project:</b> FlashCards Manager for Android <br>
 * <b>Date:</b> 18.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class SubscriptionListAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = SubscriptionListAdapter.class.getSimpleName();

    private Context context;
    private int layoutId;
    private ArrayList<EditSubscriptionsActivity.NewsGroupItem> subscriptions;
    private ArrayList<EditSubscriptionsActivity.NewsGroupItem> originalSubscriptions;
    private int currentDetailView = -1;
    private View lastDetailView;

    private SubscriptionFilter filter;

    public SubscriptionListAdapter(Context context, int layoutId, ArrayList<EditSubscriptionsActivity.NewsGroupItem> subscriptions) {
        this.context = context;
        this.layoutId = layoutId;
        this.subscriptions = subscriptions;
        this.originalSubscriptions = subscriptions;
        Log.d(TAG, "Subscriptionssize: " + subscriptions.size());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return subscriptions.get(position);
    }

    @Override
    public int getCount() {
        return subscriptions.size();
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
            holder.selected = (CheckBox) newView.findViewById(R.id.subscription_checkbox);
            holder.title = (TextView) newView.findViewById(R.id.subscription_title);
            newView.setTag(holder);
        }
        holder.selected.setChecked(subscriptions.get(position).isSelected());
        holder.title.setText(subscriptions.get(position).getNewsgroupInfo().getNewsgroup());
        final int fPosition = position;
        final TextView tvTitle = holder.title;
        holder.selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox)v).isChecked()) {
                    subscriptions.get(fPosition).setSelected(true);
                } else {
                    subscriptions.get(fPosition).setSelected(false);
                }
            }
        });
       holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDetailView == fPosition) {
                    tvTitle.setSingleLine(true);
                    currentDetailView = -1;
                    lastDetailView = null;
                } else {
                    tvTitle.setSingleLine(false);
                    if (lastDetailView != null) {
                        ((TextView) lastDetailView).setSingleLine(true);
                    }
                    lastDetailView = v;
                    currentDetailView = fPosition;
                }
                Log.d(TAG, "currentDetailView = " + currentDetailView);
            }
        });
        return newView;
    }

    private void resetDetailView() {
        if (lastDetailView != null) {
            ((TextView) lastDetailView).setSingleLine(true);
        }
        lastDetailView = null;
        currentDetailView = -1;
    }

    public static class ViewHolder {
        CheckBox selected;
        public TextView title;
    }


    @Override
    public Filter getFilter() {
        if (filter == null){
            filter  = new SubscriptionFilter();
        }
        return filter;
    }

    public void setTextConstraint(CharSequence constraint) {

    }


    public void setRadioButtonConstraint(CharSequence constraint) {

    }

    private class SubscriptionFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();
            ArrayList<EditSubscriptionsActivity.NewsGroupItem> filteredSubscriptions = new ArrayList<>();

            constraint = constraint.toString().toLowerCase();
            Log.d(TAG, "Constraint: " + "*" + constraint + "*");
            if (constraint.toString().length() > 0) {
                if (constraint.equals(context.getResources().getString(R.string.cmd_selected))){
                    for (int i = 0; i < originalSubscriptions.size(); i++) {
                        EditSubscriptionsActivity.NewsGroupItem subscriptionName = originalSubscriptions.get(i);
                        if (subscriptionName.isSelected()) {
                            filteredSubscriptions.add(subscriptionName);
                        }
                    }
                } else {
                    for (int i = 0; i < originalSubscriptions.size(); i++) {
                        EditSubscriptionsActivity.NewsGroupItem subscriptionName = originalSubscriptions.get(i);
                        if (subscriptionName.getNewsgroupInfo().getNewsgroup().toLowerCase().contains(constraint.toString())) {
                            filteredSubscriptions.add(subscriptionName);
                        }
                    }
                }
                results.count = filteredSubscriptions.size();
                results.values = filteredSubscriptions;
            } else {
                synchronized(this)
                {
                    results.values = originalSubscriptions;
                    results.count = originalSubscriptions.size();
                }
            }
            // Log.d(TAG, "Number of results: " + results.count);
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            subscriptions = (ArrayList<EditSubscriptionsActivity.NewsGroupItem>) results.values;
            resetDetailView();
            // Log.d(TAG, "Resetted currentDetailView");
            notifyDataSetChanged();
        }
    }

    // TODO save last detail view on config changes
    public int getCurrentDetailView() {
        return currentDetailView;
    }

    public void setCurrentDetailView(int currentDetailView) {
        this.currentDetailView = currentDetailView;
    }
}
