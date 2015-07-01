package org.random_access.newsreader;

import android.app.FragmentManager;
import android.content.Intent;
import android.database.Cursor;
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

import org.random_access.newsreader.nntp.NNTPDateFormatter;
import org.random_access.newsreader.queries.MessageQueries;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.07.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
@SuppressWarnings("ALL")
public class ShowSingleArticleActivity extends AppCompatActivity {

    private static final String TAG = ShowSingleArticleActivity.class.getSimpleName();

    public static final String KEY_SERVER_ID = "server-id";
    public static final String KEY_GROUP_ID = "group-id";
    public static final String KEY_GROUP_NAME = "group-name";
    public static final String KEY_MESSAGE_ID = "msg-id";

    private ShowSingleArticleFragment articleFragment;
    private static final String TAG_ARTICLE_FRAGMENT = "article-fragment-tag";

    private TextView tvSubject, tvFrom, tvDate, tvText;
    private ImageButton btnReply, btnFullHeader;

    private long serverId;
    private long groupId;
    private long messageId;

    private boolean extended;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getData();
        loadMessage();
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
        Log.d(TAG, "In onDestroy - articleFragment == null? " + (articleFragment == null));
        if (articleFragment != null) {
            articleFragment.setExtended(extended);
        }
    }

    private void getData() {
        Bundle extras = getIntent().getExtras();
        setTitle(extras.getString(KEY_GROUP_NAME));
        serverId = extras.getLong(KEY_SERVER_ID);
        groupId = extras.getLong(KEY_GROUP_ID);
        messageId = extras.getLong(KEY_MESSAGE_ID);
    }

    private void loadMessage() {
        FragmentManager fragmentManager = getFragmentManager();
        articleFragment = (ShowSingleArticleFragment) fragmentManager.findFragmentByTag(TAG_ARTICLE_FRAGMENT);
        if(articleFragment == null) {
            Log.d(TAG, "ArticleFragment == null? " +  (articleFragment == null));
            articleFragment = new ShowSingleArticleFragment();
            fragmentManager.beginTransaction().add(articleFragment, TAG_ARTICLE_FRAGMENT).commit();
            new LoadMessageTask().execute();
        } else{
            extended = articleFragment.isExtended();
            prepareGUI();
        }
    }

    private class LoadMessageTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            MessageQueries mQueries = new MessageQueries(ShowSingleArticleActivity.this);
            Cursor c = mQueries.getMessageWithId(messageId);
            if (c.moveToFirst()) {
                articleFragment.setFromName(c.getString(MessageQueries.COL_FROM_NAME));
                articleFragment.setSubject(c.getString(MessageQueries.COL_SUBJECT));
                articleFragment.setPrettyDate(NNTPDateFormatter.getPrettyDateString(c.getLong(MessageQueries.COL_DATE), ShowSingleArticleActivity.this));
                articleFragment.setMessageBody(c.getString(MessageQueries.COL_BODY));
                articleFragment.setMessageHeader(c.getString(MessageQueries.COL_HEADER));
            }
            c.close();
            mQueries.setMessageUnread(messageId, false);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            prepareGUI();
        }
    }


    private void prepareGUI() {
        setContentView(R.layout.activity_show_single_article);

        tvSubject = (TextView) findViewById(R.id.article_subject);
        tvFrom = (TextView) findViewById(R.id.article_from);
        tvDate = (TextView) findViewById(R.id.article_date);
        tvText = (TextView) findViewById(R.id.article_text);

        tvFrom.setText(articleFragment.getFromName());
        tvSubject.setText(articleFragment.getSubject());
        tvDate.setText(articleFragment.getPrettyDate());
        tvText.setText(articleFragment.getMessageBody());

        btnReply = (ImageButton) findViewById(R.id.article_reply);
        btnReply.setColorFilter(getResources().getColor(R.color.light_blue));
        btnFullHeader = (ImageButton) findViewById(R.id.article_fullheader);
        btnFullHeader.setColorFilter(getResources().getColor(R.color.light_blue));

        tvFrom.setVisibility(extended ? View.VISIBLE : View.GONE);
        tvDate.setVisibility(extended ? View.VISIBLE : View.GONE);
        btnReply.setVisibility(extended ? View.VISIBLE : View.GONE);
        btnFullHeader.setVisibility(extended ? View.VISIBLE : View.GONE);

        addListeners();
    }

    private void addListeners() {
        tvSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvFrom.setVisibility(extended ? View.GONE : View.VISIBLE);
                tvDate.setVisibility(extended ? View.GONE : View.VISIBLE);
                btnReply.setVisibility(extended ? View.GONE : View.VISIBLE);
                btnFullHeader.setVisibility(extended ? View.GONE : View.VISIBLE);
                extended = !extended;
            }
        });
        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(ShowSingleArticleActivity.this, "Not yet implemented, but coming soon!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ShowSingleArticleActivity.this, WriteMessageActivity.class);
                intent.putExtra(WriteMessageActivity.KEY_SERVER_ID, serverId);
                intent.putExtra(WriteMessageActivity.KEY_NEWSGROUP_ID, groupId);
                intent.putExtra(WriteMessageActivity.KEY_MESSAGE_ID, messageId);
                startActivity(intent);

            }
        });
        btnFullHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfoFragment df = InfoFragment.getInstance(getResources().getString(R.string.title_msg_header), articleFragment.getMessageHeader());
                df.show(getFragmentManager(), "FullHeaderFragmen");
            }
        });
    }
}
