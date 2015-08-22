package org.random_access.newsreader;

import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.nntp.SimpleNNTPHeader;
import org.random_access.newsreader.nntp.CustomNNTPClient;
import org.random_access.newsreader.nntp.NNTPDateFormatter;
import org.random_access.newsreader.nntp.NNTPMessageHeader;
import org.random_access.newsreader.nntp.SupportedCharsets;
import org.random_access.newsreader.queries.MessageQueries;
import org.random_access.newsreader.queries.NewsgroupQueries;
import org.random_access.newsreader.queries.ServerQueries;
import org.random_access.newsreader.queries.SettingsQueries;
import org.random_access.newsreader.sync.NNTPConnector;

import java.io.IOException;
import java.io.Writer;
import java.util.zip.DataFormatException;

import javax.security.auth.login.LoginException;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 03.07.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */

public class WriteMessageActivity extends AppCompatActivity {

    private static final String TAG = WriteMessageActivity.class.getSimpleName();

    public static final String KEY_SERVER_ID = "serverId";
    public static final String KEY_NEWSGROUP_ID = "newsgroupId";
    public static final String KEY_MESSAGE_ID = "messageId";

    public static final long NEW_MESSAGE = -1L;

    private static final String WRITE_MSG_TAG = "write-message";
    private WriteMessageFragment msgFragment;

    private long serverId;
    private long groupId;
    private long messageId;

    private TextView tvFromEmail;
    private TextView tvNewsgroup;
    private EditText txtSubject;
    private EditText txtMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        serverId = extras.getLong(KEY_SERVER_ID);
        groupId = extras.getLong(KEY_NEWSGROUP_ID);
        messageId = extras.getLong(KEY_MESSAGE_ID);

        setContentView(R.layout.activity_write_message);
        tvFromEmail = (TextView)findViewById(R.id.txt_from);
        tvNewsgroup = (TextView) findViewById(R.id.txt_newsgroup);
        txtSubject = (EditText) findViewById(R.id.txt_subject);
        txtMessage = (EditText) findViewById(R.id.txt_message);
        fillMessageFields();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_write_message, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (msgFragment != null) {
            msgFragment.setFromSubject(txtSubject.getText().toString());
            msgFragment.setFromMessage(txtMessage.getText().toString());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_send:
                new SendMessageTask().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * construct header lines and write message via NNTPClient
     */
    class SendMessageTask extends AsyncTask<Void, Void,Void> {

        private Exception exception;
        private String header, body;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // construct header & body
            SimpleNNTPHeader nntpHeader = new NNTPMessageHeader().buildHeader(msgFragment.getFromName(), msgFragment.getFromEmail(),
                    msgFragment.getFromNewsgroup(), 0, msgFragment.getReferences(), txtSubject.getText().toString(),
                    WriteMessageActivity.this);
            header = nntpHeader.toString();
            Log.d(TAG, header);
            body = txtMessage.getText().toString() + msgFragment.getFromSignature();
        }

        @Override
        protected Void doInBackground(Void... params) {
           Context ctxt = WriteMessageActivity.this;
            // get connection params, write msg to server & validate success
            try {
                Cursor c = new ServerQueries(ctxt).getServerWithId(serverId);
                if (c.moveToFirst()) {
                    NNTPConnector nntpConnector = new NNTPConnector(ctxt);
                    CustomNNTPClient client = nntpConnector.connectToNewsServer(serverId, SupportedCharsets.UTF_8);
                    Writer writer = client.postArticle();
                    if(writer != null) {
                        writer.write(header);
                        writer.write(body);
                        writer.close();
                        if (!client.completePendingCommand()) {
                            throw new IOException("cannot complete posting");
                        }
                    } else {
                        throw  new IOException("cannot post article");
                    }
                    client.disconnect();
                } else {
                    throw new DataFormatException("no server data found");
                }
            } catch (IOException | LoginException | DataFormatException e) {
                exception = e;
                Log.e(TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (exception != null) {
                Toast.makeText(WriteMessageActivity.this, getResources().getString(R.string.send_error), Toast.LENGTH_SHORT).show();
                Log.e(TAG, exception.getMessage());
            } else {
                Toast.makeText(WriteMessageActivity.this, getResources().getString(R.string.send_success), Toast.LENGTH_SHORT).show();
                finish();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            Log.e(TAG, e.getMessage());
                        }
                        Log.d(TAG, "Starting sync...");
                        ContentResolver.requestSync(ShowServerActivity.ACCOUNT, ShowServerActivity.AUTHORITY, Bundle.EMPTY);
                    }
                }).start();
            }
        }
    }

    /**
     * Distinguish between a fresh activity start (load parameters from database) and
     * a restart because of a config change (load parameters from a WriteMessageFragment instance)
     */
    private void fillMessageFields() {
        FragmentManager fragmentManager = getFragmentManager();
        msgFragment = (WriteMessageFragment)fragmentManager.findFragmentByTag(WRITE_MSG_TAG);
        if (msgFragment == null) {
            msgFragment = new WriteMessageFragment();
            fragmentManager.beginTransaction().add(msgFragment, WRITE_MSG_TAG).commit();
            new PrepareMessageTask().execute();
        } else {
            prepareGUI();
        }
    }

    
    private class PrepareMessageTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Context ctxt = WriteMessageActivity.this;
            // get from e-mail address
            Cursor settingsCursor = new SettingsQueries(ctxt).getSettingsForServer(serverId);
            if (settingsCursor.moveToFirst()) {
                msgFragment.setFromEmail(settingsCursor.getString(SettingsQueries.COL_EMAIL));
                msgFragment.setFromName(settingsCursor.getString(SettingsQueries.COL_NAME));
                msgFragment.setFromSignature("\n--\n" + settingsCursor.getString(SettingsQueries.COL_SIGNATURE));
            }
            settingsCursor.close();

            // get newsgroup name
            msgFragment.setFromNewsgroup(new NewsgroupQueries(ctxt).getNewsgroupName(groupId));


            if (messageId == NEW_MESSAGE) {
                msgFragment.setFromMessage("");
                msgFragment.setFromSubject("");
                msgFragment.setReferences("");
            } else { // get subject, message and refIds if reply message
                Cursor messageCursor = new MessageQueries(ctxt).getMessageWithId(messageId);
                if (messageCursor.moveToFirst()) {
                    String subject = messageCursor.getString(MessageQueries.COL_SUBJECT);
                    String re = getResources().getString(R.string.reply);
                    msgFragment.setFromSubject(subject.startsWith(re) ? subject : re + " " + subject);
                    String date = NNTPDateFormatter.getPrettyDateStringDate(messageCursor.getLong(MessageQueries.COL_DATE), ctxt);
                    String time = NNTPDateFormatter.getPrettyDateStringTime(messageCursor.getLong(MessageQueries.COL_DATE), ctxt);
                    String name = messageCursor.getString(MessageQueries.COL_FROM_NAME);
                    String replyIntro = getResources().getString(R.string.sent_from, date, time, name);
                    msgFragment.setFromMessage((replyIntro + "\n" + messageCursor.getString(MessageQueries.COL_BODY)).replace("\n", "\n> "));
                    String references = messageCursor.getString(MessageQueries.COL_REFERENCES);
                    msgFragment.setReferences(references + " " + messageCursor.getString(MessageQueries.COL_MSG_ID));
                }
                messageCursor.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            prepareGUI();
        }
    }

    private void prepareGUI() {
        tvFromEmail.setText(msgFragment.getFromEmail());
        tvNewsgroup.setText(msgFragment.getFromNewsgroup());
        txtSubject.setText(msgFragment.getFromSubject());
        txtMessage.setText(msgFragment.getFromMessage());
    }
}
