package org.random_access.newsreader;

import android.app.FragmentManager;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.apache.commons.net.nntp.NNTPClient;
import org.apache.commons.net.nntp.NewsgroupInfo;
import org.random_access.newsreader.adapter.SubscriptionListAdapter;
import org.random_access.newsreader.queries.NewsgroupQueries;
import org.random_access.newsreader.queries.ServerQueries;
import org.random_access.newsreader.sync.NNTPConnector;

import java.io.IOException;
import java.util.ArrayList;

import javax.security.auth.login.LoginException;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.07.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class EditSubscriptionsActivity extends AppCompatActivity {

    private static final String TAG = EditSubscriptionsActivity.class.getSimpleName();

    public static final String KEY_SERVER_ID = "server-id";
    private static final String TAG_SUBSCRIPTIONS_FRAGMENT = "subscriptions-fragment";

    private SubscriptionListAdapter adapter;
    private RadioGroup selection;
    private EditText txtSearch;
    private EditSubscriptionsFragment subscriptionsFragment;
    private ArrayList<NewsGroupItem> items;

    private long serverId;
    private String serverName;
    private int serverPort;
    private boolean auth;
    private String user;
    private String password;

    private int checkedSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "In onCreate");
        setTitle(getResources().getString(R.string.select_newsgroups));
        serverId = getIntent().getExtras().getLong(KEY_SERVER_ID);
        loadNewsgroups();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_subscriptions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_newsgroups_discard:
                finish();
                return true;
            case R.id.action_newsgroups_ok:
                new AddNewsgroupTask().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Get server data from local database
     * @throws IOException if there is no data for the given _ID
     */
    private void getServerData() throws IOException{
        ServerQueries serverQueries = new ServerQueries(this);
        Cursor cursor = serverQueries.getServerWithId(serverId);
        if (cursor.moveToFirst()) {
            serverName = cursor.getString(ServerQueries.COL_NAME);
            serverPort = cursor.getInt(ServerQueries.COL_PORT);
            auth = cursor.getInt(ServerQueries.COL_AUTH) == 1;
            user = cursor.getString(ServerQueries.COL_USER);
            password = cursor.getString(ServerQueries.COL_PASSWORD);
        } else {
            throw new IOException("Server ID not found!");
        }
        cursor.close();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscriptionsFragment != null) {
            subscriptionsFragment.setNewsGroupItems(items);
            subscriptionsFragment.setCheckedSelection(selection.getCheckedRadioButtonId());
            subscriptionsFragment.setCurrentDetailView(adapter.getCurrentDetailView());
            Log.d(TAG, "Save selection: only selected = " + (selection.getCheckedRadioButtonId() == R.id.groups_selection));
        }
    }

    /**
     * Load all newsgroups either from the web if activity is started for the first time, otherwise from a non-visible fragment
     * storing data across config changes
     */
    private void loadNewsgroups() {
        FragmentManager fragmentManager = getFragmentManager();
        subscriptionsFragment = (EditSubscriptionsFragment) fragmentManager.findFragmentByTag(TAG_SUBSCRIPTIONS_FRAGMENT);
        if(subscriptionsFragment == null) {
            subscriptionsFragment = new EditSubscriptionsFragment();
            fragmentManager.beginTransaction().add(subscriptionsFragment, TAG_SUBSCRIPTIONS_FRAGMENT).commit();
            checkedSelection = R.id.groups_all;
            new GetNewsTask().execute();
        } else{
            items = subscriptionsFragment.getNewsGroupItems();
            checkedSelection = subscriptionsFragment.getCheckedSelection();
            Log.d(TAG, "Restore selection: only selected = " + (checkedSelection == R.id.groups_selection));
            prepareGUI();
            adapter.setCurrentDetailView(subscriptionsFragment.getCurrentDetailView());
        }
    }

    /**
     * Add selected groups not in database yet and remove groups that were in database but are not selected anymore
     */
    private class AddNewsgroupTask extends AsyncTask<Void, Void, Integer[]> {

        @Override
        protected Integer[] doInBackground(Void... params) {
            NewsgroupQueries queries = new NewsgroupQueries(EditSubscriptionsActivity.this);
            int addCount = 0;
            int deleteCount = 0;
            for (int i = 0; i < items.size(); i++) {
                // newsgroup not yet in database, but selected
                if (items.get(i).isSelected() && items.get(i).getNewsgroupId() == -1) {
                    queries.addNewsgroup(items.get(i).getNewsgroupInfo().getNewsgroup(), serverId);
                    addCount++;
                    // newsgroup in database, but deselected
                } else if (!items.get(i).isSelected() && items.get(i).getNewsgroupId() != -1) {
                    queries.deleteNewsgroup(items.get(i).getNewsgroupId());
                    deleteCount++;
                }
            }
            return new Integer[] {addCount, deleteCount};
        }

        @Override
        protected void onPostExecute(Integer[] count) {
            Toast.makeText(EditSubscriptionsActivity.this, getResources().getQuantityString(R.plurals.success_add_groups, count[0], count[0])
                    + ", " + getResources().getQuantityString(R.plurals.delete_groups, count[1], count[1]), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Get list of available newsgroups (NewsgroupInfo elements) from the given server, using given login data
     */
    private class GetNewsTask extends AsyncTask<Void, Void, ArrayList<NewsGroupItem>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setContentView(R.layout.progress_wheel);
        }

        @Override
        protected ArrayList<NewsGroupItem> doInBackground(Void... voids) {
            try {
                getServerData();
                NNTPConnector connector = new NNTPConnector(EditSubscriptionsActivity.this);
                NNTPClient client = connector.connectToNewsServer(EditSubscriptionsActivity.this, serverName, serverPort, auth, user, password);
                NewsgroupInfo[] infos = client.listNewsgroups();
                return getNewsgroupItems(infos);
            } catch (IOException | LoginException e) {
                e.printStackTrace();  // TODO handle exceptions
            }
            return new ArrayList<>();
        }


        @Override
        protected void onPostExecute(ArrayList<NewsGroupItem> items) {
            EditSubscriptionsActivity.this.items = items;
            prepareGUI();
        }

    }

    /**
     * Find and configure all view elements
     */
    private void prepareGUI () {
        setContentView(R.layout.activity_edit_subscriptions);
        adapter = new SubscriptionListAdapter(EditSubscriptionsActivity.this, items);
        ListView lv = (ListView) findViewById(R.id.groups_list);
        lv.setTextFilterEnabled(true);
        lv.setAdapter(adapter);
        txtSearch = (EditText) findViewById(R.id.groups_search);
        txtSearch.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) { /* unused */  }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)  { /* unused */  }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
            }
        });
        selection = (RadioGroup)findViewById(R.id.radio_group);
        selection.check(checkedSelection);
        selection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                txtSearch.setText("");
                manageCheckFilter(checkedId);
            }
        });
        manageCheckFilter(selection.getCheckedRadioButtonId());
    }

    private void manageCheckFilter(int checkedId) {
        adapter.setSelectedItemsOnly(checkedId == R.id.groups_selection);
        adapter.getFilter().filter(txtSearch.getText().toString());
    }

    /**
     * Wrap all NewsgroupInfos that were downloaded in NewsgroupItems and return an arraylist
     * @param infos array of downloaded NewsgroupInfos
     * @return arraylist containing all NewsgroupItems
     */
    private ArrayList<NewsGroupItem> getNewsgroupItems(NewsgroupInfo[] infos) {
        Cursor c = new NewsgroupQueries(EditSubscriptionsActivity.this).getNewsgroupsOfServer(serverId);
        ArrayList<NewsGroupItem> items  = new ArrayList<>();
        for (int i = 0; i < infos.length; i++) {
            items.add(i, setupItem(c, infos[i]));
        }
        c.close();
        return items;
    }

    /**
     * Check if this newsgroup is already saved in local database
     * @param c a cursor pointing to the beginning of the local newsgroup database
     * @param info NewsgroupInfo to wrap with this item
     * @return a selected newsgroup item if the user already subscribed to this group, otherwise a deselected item
     */
    private NewsGroupItem setupItem(Cursor c, NewsgroupInfo info) {
        NewsGroupItem item = new NewsGroupItem(info);
        if (c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                if (c.getString(NewsgroupQueries.COL_NAME).equals(item.getNewsgroupInfo().getNewsgroup())) {
                    item.setNewsgroupId(c.getLong(NewsgroupQueries.COL_ID));
                    item.setSelected(true);
                }
                c.moveToNext();
            }
            c.moveToFirst();
        }
        return item;
    }

    /**
     * Wrapper class for a newsgroup info downloaded from a server, its database _ID value (if in database, otherwise -1) and
     * its selection status
     */
    public class NewsGroupItem implements Comparable<NewsGroupItem>{
        private long newsgroupId;
        private boolean selected;
        private final NewsgroupInfo newsgroupInfo;

        public  NewsGroupItem(NewsgroupInfo newsgroupInfo) {
            this.newsgroupInfo = newsgroupInfo;
            this.selected = false;
            this.newsgroupId = -1;
        }

        @Override
        public int compareTo(@NonNull NewsGroupItem item) {
            if (item.getNewsgroupInfo() == null || item.getNewsgroupInfo().getNewsgroup() == null) {
                return -1;
            }
            return newsgroupInfo.getNewsgroup().compareTo(item.getNewsgroupInfo().getNewsgroup());
        }

        public NewsgroupInfo getNewsgroupInfo() {
            return newsgroupInfo;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected (boolean selected) {
            this.selected = selected;
        }

        public long getNewsgroupId () {
            return newsgroupId;
        }

        public void setNewsgroupId (long newsgroupId) {
            this.newsgroupId = newsgroupId;
        }
    }
}
