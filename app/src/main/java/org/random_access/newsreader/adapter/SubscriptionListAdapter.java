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
import java.util.Collections;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 18.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class SubscriptionListAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = SubscriptionListAdapter.class.getSimpleName();

    private final Context context; // activity hosting list view
    private final int layoutId; // ID of list item

    // data - whole data & filter result
    private final ArrayList<EditSubscriptionsActivity.NewsGroupItem> originalNewsgroupItems;
    private ArrayList<EditSubscriptionsActivity.NewsGroupItem> filteredNewsgroupItems;

    private int currentDetailView = -1; // view that takes as much lines as it needs to display the whole text
    private View lastDetailView = null; // for closing last picked detail view
    private boolean selectedItemsOnly = false; // show only items selected by users or all items
    private SubscriptionFilter filter = null; // filter list according to edittext & selection state

    public SubscriptionListAdapter(Context context, ArrayList<EditSubscriptionsActivity.NewsGroupItem> subscriptions) {
        this.context = context;
        this.layoutId = R.layout.item_subscription;
        Collections.sort(subscriptions);
        this.originalNewsgroupItems = subscriptions;
        this.filteredNewsgroupItems = originalNewsgroupItems;
        Log.d(TAG, "Number of groups: " + subscriptions.size());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return filteredNewsgroupItems.get(position);
    }

    @Override
    public int getCount() {
        return filteredNewsgroupItems.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View newView = convertView;
        ViewHolder holder;

        // reuse existing views
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
        holder.selected.setChecked(filteredNewsgroupItems.get(position).isSelected());
        holder.title.setText(filteredNewsgroupItems.get(position).getNewsgroupInfo().getNewsgroup());
        final int fPosition = position;
        final TextView tvTitle = holder.title;

        /******              Listeners on single view elements             *****/
        holder.selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // manage selection of items
                filteredNewsgroupItems.get(fPosition).setSelected(((CheckBox)v).isChecked());
            }
        });
        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // manages opening and closing the detail view
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

    /**
     * Close detail view when list gets filtered to not get in trouble with wrong indices...
     */
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

    private class SubscriptionFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();
            ArrayList<EditSubscriptionsActivity.NewsGroupItem> filteredSubscriptions = new ArrayList<>();

            constraint = constraint.toString().toLowerCase();
            Log.d(TAG, "Constraint: " + "*" + constraint + "*");
            for (int i = 0; i < originalNewsgroupItems.size(); i++) {
                EditSubscriptionsActivity.NewsGroupItem item = originalNewsgroupItems.get(i);
                if (selectedItemsOnly) { // add all selected items that match the given constraint
                    if (item.isSelected() && item.getNewsgroupInfo().getNewsgroup().toLowerCase().contains(constraint.toString())) {
                        filteredSubscriptions.add(item);
                    }
                } else { // add all items that match the given constraint
                    if (item.getNewsgroupInfo().getNewsgroup().toLowerCase().contains(constraint.toString())) {
                        filteredSubscriptions.add(item);
                    }
                }
            }
            results.count = filteredSubscriptions.size();
            results.values = filteredSubscriptions;
            Log.d(TAG, "Number of results: " + results.count);
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredNewsgroupItems = (ArrayList<EditSubscriptionsActivity.NewsGroupItem>) results.values;
            resetDetailView();
            notifyDataSetChanged();
        }
    }

    /**
     * Save index of currently selected detail view across config changes
     * @return index of currently selected detail view
     */
    public int getCurrentDetailView() {
        return currentDetailView;
    }

    /**
     * Restore index of currently selected detail view across config changes
     * @param currentDetailView index of currently selected detail view
     */
    public void setCurrentDetailView(int currentDetailView) {
        this.currentDetailView = currentDetailView;
    }

    /**
     * Restore selecting checked items / all items across config changes
     * @param selectedItemsOnly true if only selection should be displayed, otherwise false
     */
    public void setSelectedItemsOnly(boolean selectedItemsOnly) {
        this.selectedItemsOnly = selectedItemsOnly;
    }
}
