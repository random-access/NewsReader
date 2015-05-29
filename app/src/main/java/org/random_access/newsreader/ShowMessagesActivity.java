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

import org.apache.commons.net.nntp.ArticleInfo;
import org.apache.commons.net.nntp.NNTPClient;
import org.apache.commons.net.nntp.NewGroupsOrNewsQuery;
import org.random_access.newsreader.adapter.MessageAdapter;
import org.random_access.newsreader.nntp.NNTPMessageHeader;
import org.random_access.newsreader.queries.NewsgroupQueries;
import org.random_access.newsreader.queries.ServerQueries;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.security.auth.login.LoginException;

/**
 * <b>Project:</b> FlashCards Manager for Android <br>
 * <b>Date:</b> 18.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class ShowMessagesActivity extends AppCompatActivity {

    private static final String TAG = ShowMessagesActivity.class.getSimpleName();

    public static final String KEY_SERVER_ID = "server-id";
    public static final String KEY_GROUP_ID = "group-id";

    private long serverId;
    private long groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    class GetMessagesTask extends AsyncTask<Void, Void, ArrayList<NNTPMessageHeader>> {

        private String groupName;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setContentView(R.layout.progress_wheel);
        }

        @Override
        protected ArrayList<NNTPMessageHeader> doInBackground(Void... voids) {
            try {
                boolean auth = new ServerQueries(ShowMessagesActivity.this).hasServerAuth(serverId);
                NNTPClient client = connectToNewsServer(serverId, auth);
                NewGroupsOrNewsQuery query = new NewGroupsOrNewsQuery(new GregorianCalendar(15,01,01), true);
                groupName = getNewsgroupName(groupId);
                query.addNewsgroup(groupName);
                String[] messages = client.listNewNews(query);
                if (messages == null) {
                    messages = applyNextCommand(client, groupName);
                }
                ArrayList<NNTPMessageHeader> headers = new ArrayList<>();
                for (String m : messages) {
                    NNTPMessageHeader header = new NNTPMessageHeader();
                    BufferedReader reader = new BufferedReader(client.retrieveArticleHeader(m));
                    header.parseHeaderData(reader, m, ShowMessagesActivity.this);
                    headers.add(header);
                }
                client.disconnect();
                return headers;
                /*ArrayList<String> messageList = new ArrayList<>();
                messageList.add("Total messages: " + messages.length);
                Collections.addAll(messageList, messages);
                client.disconnect();
                return messageList; */
            } catch (IOException | LoginException e) {
                e.printStackTrace();
            }
            return null;
        }

        private String[] applyNextCommand (NNTPClient client, String group) throws  IOException{
            ArrayList<String> articleList = new ArrayList<>();
            client.selectNewsgroup(group);
            ArticleInfo pointer = new ArticleInfo();
            int i = 0;
            while (client.selectNextArticle(pointer) && i < 100){
                // client.selectArticle(pointer.articleNumber, pointer);
                Log.d(TAG, "pointer.articleNumber = " + pointer.articleNumber + ", pointer.articleId = " + pointer.articleId);
                articleList.add(pointer.articleId);
                i++;
            }
            String[] articleArray = new String[articleList.size()];
            return articleList.toArray(articleArray);
        }

        @Override
        protected void onPostExecute(ArrayList<NNTPMessageHeader> headers) {
            setContentView(R.layout.activity_show_messages);
            ListView lv = (ListView)findViewById(R.id.show_messages_list);
            final MessageAdapter adapter = new MessageAdapter(ShowMessagesActivity.this, R.layout.item_message_template, headers);
            lv.setAdapter(adapter);
            /* final ArrayAdapter<String> adapter = new ArrayAdapter<>(ShowMessagesActivity.this, R.layout.item_message, strings);
            lv.setAdapter(adapter); */
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(ShowMessagesActivity.this, ShowSingleArticleActivity.class);
                    intent.putExtra(ShowSingleArticleActivity.KEY_SERVER_ID, serverId);
                    intent.putExtra(ShowSingleArticleActivity.KEY_GROUP_ID, groupId);
                    intent.putExtra(ShowSingleArticleActivity.KEY_ARTICLE_ID, ((NNTPMessageHeader)adapter.getItem(position)).getValue(NNTPMessageHeader.KEY_MESSAGE_ID));
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
    private NNTPClient connectToNewsServer(long serverId, boolean auth) throws IOException, LoginException {
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
        if (!auth) {
            c.close();
            return nntpClient;
        }
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
