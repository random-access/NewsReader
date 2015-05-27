package org.random_access.newsreader;

import android.app.FragmentManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.random_access.newsreader.nntp.CustomNNTPClient;
import org.random_access.newsreader.nntp.HeaderData;
import org.random_access.newsreader.queries.NewsgroupQueries;
import org.random_access.newsreader.queries.ServerQueries;

import java.io.BufferedReader;
import java.io.IOException;

import javax.security.auth.login.LoginException;

/**
 * <b>Project:</b> FlashCards Manager for Android <br>
 * <b>Date:</b> 18.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class ShowSingleArticleActivity extends AppCompatActivity {

    private static final String TAG = ShowSingleArticleActivity.class.getSimpleName();

    public static final String KEY_SERVER_ID = "server-id";
    public static final String KEY_GROUP_ID = "group-id";
    public static final String KEY_ARTICLE_ID = "article-id";
    public static final String KEY_GROUP_NAME = "group-name";

    private ShowSingleArticleFragment articleFragment;
    private static final String TAG_ARTICLE_FRAGMENT = "article-fragment-tag";

    private TextView tvSubject, tvFrom, tvDate, tvText;
    private ImageButton btnReply;

    private long serverId;
    private long groupId;
    private String articleId;
    private  boolean auth;

    private String [] articleData;
    private boolean extended;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getIntent().getExtras().getString(KEY_GROUP_NAME));

        serverId = getIntent().getExtras().getLong(KEY_SERVER_ID);
        groupId = getIntent().getExtras().getLong(KEY_GROUP_ID);
        articleId = getIntent().getExtras().getString(KEY_ARTICLE_ID);

        loadArticles();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "In onDestroy - subscriptionsFragment == null? " + (articleFragment == null));
        if (articleFragment != null) {
            articleFragment.setArticleData(articleData);
            articleFragment.setExtended(extended);
        }
    }


    private void loadArticles() {
        FragmentManager fragmentManager = getFragmentManager();
        articleFragment = (ShowSingleArticleFragment) fragmentManager.findFragmentByTag(TAG_ARTICLE_FRAGMENT);
        if(articleFragment == null) {
            articleFragment = new ShowSingleArticleFragment();
            fragmentManager.beginTransaction().add(articleFragment, TAG_ARTICLE_FRAGMENT).commit();
            new FetchArticleTask().execute();
        } else{
            extended = articleFragment.isExtended();
            articleData = articleFragment.getArticleData();
            prepareGUI();
        }
    }

    class FetchArticleTask extends AsyncTask<Void, Void,String[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setContentView(R.layout.progress_wheel);
        }

        @Override
        protected String[] doInBackground(Void... voids) {
            try {
                auth = new ServerQueries(ShowSingleArticleActivity.this).hasServerAuth(serverId);
                CustomNNTPClient client = connectToNewsServer(serverId, null, auth);
                BufferedReader reader = new BufferedReader(client.retrieveArticleHeader(articleId));
                HeaderData headerData = new HeaderData();
                headerData.parseHeaderData(reader);
                String charset = headerData.getMessageCharset();

                client = connectToNewsServer(serverId, charset, auth);
                headerData = new HeaderData();
                headerData.parseHeaderData(new BufferedReader(client.retrieveArticleHeader(articleId)));
                reader = new BufferedReader(client.retrieveArticleBody(articleId));
                String line = "";
                String[] result = new String[4];
                StringBuilder sb = new StringBuilder();
                while((line=reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                reader.close();
                client.disconnect();

                result[0] = headerData.getValue(HeaderData.KEY_FROM);
                result[1] = headerData.getValue(HeaderData.KEY_SUBJECT);
                result[2] = headerData.getValue(HeaderData.KEY_DATE);
                result[3] = sb.toString();
                return result;
            } catch (IOException | LoginException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] articleData) {
            ShowSingleArticleActivity.this.articleData = articleData;
            extended = true;
            prepareGUI();
        }
    }

    private void prepareGUI() {
        setContentView(R.layout.activity_show_single_article);

        tvSubject = (TextView) findViewById(R.id.article_subject);
        tvFrom = (TextView) findViewById(R.id.article_from);
        tvDate = (TextView) findViewById(R.id.article_date);
        tvText = (TextView) findViewById(R.id.article_text);

        tvFrom.setText(articleData[0] == null? "null" : articleData[0]);
        tvSubject.setText(articleData[1] == null ? "null" : articleData[1]);
        tvDate.setText(articleData[2] == null ? "null" : articleData [2]);
        tvText.setText(articleData[3] == null ? "null" : articleData [3]);

        btnReply = (ImageButton) findViewById(R.id.article_reply);

        tvFrom.setVisibility(extended ? View.VISIBLE : View.GONE);
        tvDate.setVisibility(extended ? View.VISIBLE : View.GONE);
        btnReply.setVisibility(extended ? View.VISIBLE : View.GONE);

        addListeners();
    }

    private void addListeners() {
        tvSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvFrom.setVisibility(extended ? View.GONE : View.VISIBLE);
                tvDate.setVisibility(extended ? View.GONE : View.VISIBLE);
                btnReply.setVisibility(extended ? View.GONE : View.VISIBLE);
                extended = !extended;
            }
        });
        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ShowSingleArticleActivity.this, "Not yet implemented, but coming soon!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Helper method to establish a connection to a given news server
     * @param serverId database ID of a server entry
     * @returna NNTPClient object to communicate with
     * @throws IOException
     */
    private CustomNNTPClient connectToNewsServer(long serverId, String charset, boolean auth) throws IOException, LoginException {
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
