package org.random_access.newsreader;

import android.app.FragmentManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
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


public class EditSubscriptionsActivity extends AppCompatActivity {

    private static final String TAG = EditSubscriptionsActivity.class.getSimpleName();

    public static final String KEY_SERVER_ID = "server-id";
    public static final String TAG_SUBSCRIPTIONS_FRAGMENT = "subscriptions-fragment";

    private SubscriptionListAdapter adapter;
    private ListView lv;
    private EditText txtSearch;
    private EditSubscriptionsFragment subscriptionsFragment;
    private ArrayList<NewsGroupItem> items;

    private long serverId;
    private String serverName;
    private int serverPort;
    private String user;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getResources().getString(R.string.select_newsgroups));
        serverId = getIntent().getExtras().getLong(KEY_SERVER_ID);
        loadNewsgroups();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_subscriptions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_newsgroups_discard:
                finish();
                return true;
            case R.id.action_newsgroups_ok:
                //Toast.makeText(this, "Save newsgroup subscriptions -> to be implemented", Toast.LENGTH_SHORT).show();
                new AddNewsgroupTask().execute();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getServerData() throws IOException{
        ServerQueries serverQueries = new ServerQueries(this);
        Cursor cursor = serverQueries.getServerWithId(serverId);
        if (cursor.moveToFirst()) {
            serverName = cursor.getString(ServerQueries.COL_NAME);
            serverPort = cursor.getInt(ServerQueries.COL_PORT);
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
        Log.d(TAG, "In onDestroy - subscriptionsFragment == null? " + (subscriptionsFragment == null));
        if (subscriptionsFragment != null) {
            subscriptionsFragment.setNewsGroupItems(items);
        }
    }

    private void loadNewsgroups() {
        FragmentManager fragmentManager = getFragmentManager();
        subscriptionsFragment = (EditSubscriptionsFragment) fragmentManager.findFragmentByTag(TAG_SUBSCRIPTIONS_FRAGMENT);
        if(subscriptionsFragment == null) {
            subscriptionsFragment = new EditSubscriptionsFragment();
            fragmentManager.beginTransaction().add(subscriptionsFragment, TAG_SUBSCRIPTIONS_FRAGMENT).commit();
            new GetNewsTask().execute();
        } else{
            items = subscriptionsFragment.getNewsGroupItems();
            prepareGUI();
        }
    }

    class AddNewsgroupTask extends AsyncTask<Void, Void, Integer[]> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

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
        }
    }

    class GetNewsTask extends AsyncTask<Void, Void, ArrayList<NewsGroupItem>> {

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
                NNTPClient client = connector.connectToNewsServer(EditSubscriptionsActivity.this, serverName, serverPort, user,password);
                String[] newNews = null;
                NewsgroupInfo[] infos = client.listNewsgroups();
               // Thread.sleep(500);
                return getNewsgroupItems(infos);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (LoginException e) {
                e.printStackTrace();
            }
            return new ArrayList<>(); // TODO handle exceptions

                //nntpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));
                // nntpClient.connect("news.fernuni-hagen.de");
                //nntpClient.authenticate("q9101101", "ph71oj63!");
                //NewGroupsOrNewsQuery query = new NewGroupsOrNewsQuery(new GregorianCalendar(2015, 01, 01), true);
                //query.addNewsgroup("feu.test");
                //newNews = nntpClient.listNewNews(query);
                //Log.d("Test", "New news: " + newNews.length);
                //infos = nntpClient.listNewsgroups();
                // return getNewsgroupNames(infos);
        }


        @Override
        protected void onPostExecute(ArrayList<NewsGroupItem> items) {
            EditSubscriptionsActivity.this.items = items;
            prepareGUI();
        }

    }

    private void prepareGUI () {
        setContentView(R.layout.activity_edit_subscriptions);
        adapter = new SubscriptionListAdapter(EditSubscriptionsActivity.this, R.layout.item_subscription, items);
        lv = (ListView) findViewById(R.id.groups_list);
        lv.setTextFilterEnabled(true);
        lv.setAdapter(adapter);
        txtSearch = (EditText) findViewById(R.id.groups_search);
        txtSearch.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
            }
        });
    }





    public ArrayList<NewsGroupItem> getNewsgroupItems (NewsgroupInfo[] infos) {
        Cursor c = new NewsgroupQueries(EditSubscriptionsActivity.this).getNewsgroupsOfServer(serverId);
        ArrayList<NewsGroupItem> items  = new ArrayList<>();
        for (int i = 0; i < infos.length; i++) {
            items.add(i, setupItem(c, infos[i]));
        }
        c.close();
        return items;
    }

    private NewsGroupItem setupItem(Cursor c, NewsgroupInfo info) {
        NewsGroupItem item = new NewsGroupItem(info);
        if (c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
               // Log.d(TAG, "Already selected: " + item.getNewsgroupInfo().getNewsgroup());
               // Log.d(TAG, "Name in DB: " + c.getString(NewsgroupQueries.COL_NAME));
              //  Log.d(TAG, "Name in net: " + item.getNewsgroupInfo().getNewsgroup());
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

    public class NewsGroupItem {
        private long newsgroupId;
        private boolean selected;
        private NewsgroupInfo newsgroupInfo;

        public  NewsGroupItem(NewsgroupInfo newsgroupInfo) {
            this.newsgroupInfo = newsgroupInfo;
            this.selected = false;
            this.newsgroupId = -1;
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
