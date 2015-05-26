package org.random_access.newsreader;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.apache.commons.net.nntp.NNTPClient;
import org.apache.commons.net.nntp.NewGroupsOrNewsQuery;
import org.random_access.newsreader.queries.NewsgroupQueries;
import org.random_access.newsreader.queries.ServerQueries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.security.auth.login.LoginException;


public class ShowMessagesActivity extends AppCompatActivity {

    private static final String TAG = ShowMessagesActivity.class.getSimpleName();

    public static final String KEY_SERVER_ID = "server-id";
    public static final String KEY_GROUP_ID = "group-id";

    private long serverId;
    private long groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_messages);
        serverId = getIntent().getExtras().getLong(KEY_SERVER_ID);
        groupId = getIntent().getExtras().getLong(KEY_GROUP_ID);
        new GetMessagesTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_messages, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class GetMessagesTask extends AsyncTask<Void, Void, ArrayList<String>> {

        private String groupName;

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            try {
                NNTPClient client = connectToNewsServer(serverId);
                NewGroupsOrNewsQuery query = new NewGroupsOrNewsQuery(new GregorianCalendar(15,01,01), true);
                groupName = getNewsgroupName(groupId);
                query.addNewsgroup(groupName);
                String[] messages = client.listNewNews(query);
                ArrayList<String> messageList = new ArrayList<>();
                messageList.add("Total messages: " + messages.length);
                Collections.addAll(messageList, messages);
                client.disconnect();
                return messageList;
            } catch (IOException | LoginException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            ListView lv = (ListView)findViewById(R.id.show_messages_list);
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(ShowMessagesActivity.this, R.layout.item_message, strings);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(ShowMessagesActivity.this, ShowSingleArticleActivity.class);
                    intent.putExtra(ShowSingleArticleActivity.KEY_SERVER_ID, serverId);
                    intent.putExtra(ShowSingleArticleActivity.KEY_GROUP_ID, groupId);
                    intent.putExtra(ShowSingleArticleActivity.KEY_ARTICLE_ID, adapter.getItem(position));
                    intent.putExtra(ShowSingleArticleActivity.KEY_GROUP_NAME, groupName);
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * Helper method to establish a connection to a given news server
     * @param serverId database ID of a server entry
     * @returna NNTPClient object to communicate with
     * @throws IOException
     */
    private NNTPClient connectToNewsServer(long serverId) throws IOException, LoginException {
        ServerQueries sQueries = new ServerQueries(ShowMessagesActivity.this);
        Cursor c = sQueries.getServerWithId(serverId);
        if (!c.moveToFirst()){
            c.close();
            Log.d(TAG, "Found no server with the given ID in database");
            throw new IOException("Found no server with the given ID in database");
        }
        NNTPClient nntpClient = new NNTPClient();
        // TODO handle encrypted connections
        nntpClient.connect(c.getString(ServerQueries.COL_NAME), c.getInt(ServerQueries.COL_PORT));
        boolean authOk = nntpClient.authenticate(c.getString(ServerQueries.COL_USER), c.getString(ServerQueries.COL_PASSWORD));
        c.close();
        if (authOk) {
            Log.d(TAG, "Successfully logged in!");
            return nntpClient;
        } else {
            throw new LoginException("Login failed");
        }
    }

    /**
     * Helper method to get the name of a newsgroup for a given ID
     * @param newsGroupId database _ID field identifying a Newsgroup entry
     * @return String containing the name of the given newsgroup, e.g. formatted like this: "section1.section2.*.sectionl"
     * @throws IOException if there is no newsgroup matching the ID
     */
    private String getNewsgroupName(long newsGroupId) throws IOException {
        NewsgroupQueries nQueries = new NewsgroupQueries(ShowMessagesActivity.this);
        Cursor c = nQueries.getNewsgroupForId(newsGroupId);
        if (!c.moveToFirst()) {
            throw new IOException("No newsgroup with the given ID found");
        }
        String newsgroupName = c.getString(NewsgroupQueries.COL_NAME);
        c.close();
        return newsgroupName;
    }
}
