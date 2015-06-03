package org.random_access.newsreader;

import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.random_access.newsreader.nntp.CustomNNTPClient;
import org.random_access.newsreader.nntp.NNTPMessageHeader;
import org.random_access.newsreader.queries.ServerQueries;
import org.random_access.newsreader.sync.NNTPConnector;

import java.io.BufferedReader;
import java.io.IOException;

import javax.security.auth.login.LoginException;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 18.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
@SuppressWarnings("ALL")
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

    private boolean decodingOk;
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
      //   Log.d(TAG, "In onDestroy - subscriptionsFragment == null? " + (articleFragment == null));
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

    private class FetchArticleTask extends AsyncTask<Void, Void,String[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setContentView(R.layout.progress_wheel);
        }

        @Override
        protected String[] doInBackground(Void... voids) {
            try {
                auth = new ServerQueries(ShowSingleArticleActivity.this).hasServerAuth(serverId);

                // fetch header
                CustomNNTPClient client = new NNTPConnector(ShowSingleArticleActivity.this).connectToNewsServer(serverId, null, auth);
                BufferedReader reader = new BufferedReader(client.retrieveArticleHeader(articleId));
                NNTPMessageHeader headerData = new NNTPMessageHeader();
                decodingOk = headerData.parseHeaderData(reader, articleId, ShowSingleArticleActivity.this);
                String charset = headerData.getCharset();
                client.disconnect();

                // fetch body
                client = new NNTPConnector(ShowSingleArticleActivity.this).connectToNewsServer(serverId, charset, auth);
                String line;

                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(client.retrieveArticleBody(articleId));
                while((line=reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                reader.close();
                client.disconnect();

                // save results
                String[] result = new String[5];
                result[0] = headerData.getSender();
                result[1] = headerData.getSubject();
                result[2] = headerData.getDate();
                result[3] = sb.toString();
                result[4] = headerData.getTransferEncoding();
                Log.d(TAG, headerData.getHeaderSource());
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
        if (!decodingOk) {
            Toast.makeText(this, "Could not decode headers properly!", Toast.LENGTH_SHORT).show();
        }
        /*if (!articleData[4].equals("8bit")) {
            Toast.makeText(this, "Transport encoding: " + articleData[4], Toast.LENGTH_SHORT).show();
        }*/
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
}
