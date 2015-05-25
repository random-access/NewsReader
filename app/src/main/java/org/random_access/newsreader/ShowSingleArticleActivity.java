package org.random_access.newsreader;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.random_access.newsreader.nntp.CustomNNTPClient;
import org.random_access.newsreader.nntp.HeaderData;
import org.random_access.newsreader.queries.NewsgroupQueries;
import org.random_access.newsreader.queries.ServerQueries;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.security.auth.login.LoginException;


public class ShowSingleArticleActivity extends AppCompatActivity {

    private static final String TAG = ShowSingleArticleActivity.class.getSimpleName();

    public static final String KEY_SERVER_ID = "server-id";
    public static final String KEY_GROUP_ID = "group-id";
    public static final String KEY_ARTICLE_ID = "article-id";

    private TextView tvSubject, tvFrom, tvContentType, tvCharset, tvDate, tvText;

    private long serverId;
    private long groupId;
    private String articleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_single_article);
        serverId = getIntent().getExtras().getLong(KEY_SERVER_ID);
        groupId = getIntent().getExtras().getLong(KEY_GROUP_ID);
        articleId = getIntent().getExtras().getString(KEY_ARTICLE_ID);

        tvSubject = (TextView) findViewById(R.id.article_subject);
        tvFrom = (TextView) findViewById(R.id.article_from);
        tvContentType = (TextView) findViewById(R.id.article_content_type);
        tvCharset = (TextView) findViewById(R.id.article_charset);
        tvDate = (TextView) findViewById(R.id.article_date);
        tvText = (TextView) findViewById(R.id.article_text);

        new FetchArticleTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_single_article, menu);
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

    class FetchArticleTask extends AsyncTask<Void, Void,String[]> {

        @Override
        protected String[] doInBackground(Void... voids) {
            try {
                CustomNNTPClient client = connectToNewsServer(serverId, null);
                BufferedReader reader = new BufferedReader(client.retrieveArticleHeader(articleId));
                HeaderData headerData = new HeaderData();
                headerData.parseHeaderData(reader);
                String charset = headerData.getMessageCharset();

                client = connectToNewsServer(serverId, charset);
                headerData.parseHeaderData(new BufferedReader(client.retrieveArticleHeader(articleId)));
                reader = new BufferedReader(client.retrieveArticleBody(articleId));
                String line = "";
                String[] result = new String[6];
                StringBuilder sb = new StringBuilder();
                sb.append("\n\nMessage: \n");
                while((line=reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                reader.close();
                client.disconnect();

                result[0] = headerData.getValue(HeaderData.KEY_FROM);
                result[1] = headerData.getValue(HeaderData.KEY_SUBJECT);
                result[2] = headerData.getValue(HeaderData.KEY_CONTENT_TYPE);
                result[3] = headerData.getValue(HeaderData.KEY_CHARSET);
                result[4] = headerData.getValue(HeaderData.KEY_DATE);
                result[5] = sb.toString();
                return result;
            } catch (IOException | LoginException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            tvFrom.setText(strings[0] == null? "null" : strings[0]);
            tvSubject.setText(strings[1] == null ? "null" : strings[1]);
            tvContentType.setText(strings[2] == null ? "null" : strings[2]);
            tvCharset.setText(strings[3] == null ? "null" : strings[3]);
            tvDate.setText(strings[4] == null ? "null" : strings [4]);
            tvText.setText(strings[5] == null ? "null" : strings [5]);
        }
    }

    private HashMap<String, String> getRelevantHeaderData(ArrayList<String> headers) {
        String from_key = "From: ";
        String subject_key = "Subject: ";
        String date_key = "Date: ";
        String content_type_key = "Content-Type: ";

        HashMap<String,String> map = new HashMap<String, String>();
        for (String s : headers) {
            if (s.startsWith(from_key)) {
                map.put(from_key, s);
            } else if (s.startsWith(subject_key)) {
                map.put(subject_key, s);
            } else if (s.startsWith(date_key)) {
                map.put(date_key, s);
            } else if (s.startsWith(content_type_key)) {
                String charset;
                if (s.toLowerCase().contains("utf-8")) {
                    charset = "UTF-8";
                } else {
                    charset = "ISO-8859-1";
                }
                map.put(content_type_key, charset);
            }
        }
        return map;
    }

    /**
     * Helper method to establish a connection to a given news server
     * @param serverId database ID of a server entry
     * @returna NNTPClient object to communicate with
     * @throws IOException
     */
    private CustomNNTPClient connectToNewsServer(long serverId, String charset) throws IOException, LoginException {
        ServerQueries sQueries = new ServerQueries(ShowSingleArticleActivity.this);
        Cursor c = sQueries.getServerWithId(serverId);
        if (!c.moveToFirst()){
            c.close();
            Log.d(TAG, "Found no server with the given ID in database");
            throw new IOException("Found no server with the given ID in database");
        }
        CustomNNTPClient nntpClient = new CustomNNTPClient();
        if (charset != null) {
           nntpClient.setCustomEncoding(charset);
        }
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
        NewsgroupQueries nQueries = new NewsgroupQueries(ShowSingleArticleActivity.this);
        Cursor c = nQueries.getNewsgroupForId(newsGroupId);
        if (!c.moveToFirst()) {
            throw new IOException("No newsgroup with the given ID found");
        }
        String newsgroupName = c.getString(NewsgroupQueries.COL_NAME);
        c.close();
        return newsgroupName;
    }
}
