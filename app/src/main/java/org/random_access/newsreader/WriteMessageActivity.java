package org.random_access.newsreader;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import org.random_access.newsreader.nntp.NNTPDateFormatter;
import org.random_access.newsreader.queries.MessageQueries;
import org.random_access.newsreader.queries.NewsgroupQueries;
import org.random_access.newsreader.queries.SettingsQueries;


public class WriteMessageActivity extends AppCompatActivity {

    private static final String TAG = WriteMessageActivity.class.getSimpleName();

    public static final String KEY_SERVER_ID = "serverId";
    public static final String KEY_NEWSGROUP_ID = "newsgroupId";
    public static final String KEY_MESSAGE_ID = "messageId";

    private long serverId;
    private long groupId;
    private long messageId;

    private TextView tvFrom;
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
        tvFrom = (TextView)findViewById(R.id.txt_from);
        tvNewsgroup = (TextView) findViewById(R.id.txt_newsgroup);
        txtSubject = (EditText) findViewById(R.id.txt_subject);
        txtMessage = (EditText) findViewById(R.id.txt_message);
        new PrepareMessageTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_write_message, menu);
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

    class PrepareMessageTask extends AsyncTask<Void, Void, String[]> {
        @Override
        protected String[] doInBackground(Void... params) {
            Context ctxt = WriteMessageActivity.this;
            String[] textFields = new String[4];
            String signature = "";
            // get from e-mail address
            Cursor settingsCursor = new SettingsQueries(ctxt).getSettingsForServer(serverId);
            if (settingsCursor.moveToFirst()) {
                textFields[0] = settingsCursor.getString(SettingsQueries.COL_EMAIL);
                signature = settingsCursor.getString(SettingsQueries.COL_SIGNATURE);
            }
            settingsCursor.close();

            // get newsgroup name
            textFields[1] = new NewsgroupQueries(ctxt).getNewsgroupName(groupId);

            // get subject and message
            Cursor messageCursor = new MessageQueries(ctxt).getMessageWithId(messageId);
            if (messageCursor.moveToFirst()) {
                textFields[2] = getResources().getString(R.string.reply) + " " + messageCursor.getString(MessageQueries.COL_SUBJECT);
                String date = NNTPDateFormatter.getPrettyDateStringDate(messageCursor.getLong(MessageQueries.COL_DATE), ctxt);
                String time = NNTPDateFormatter.getPrettyDateStringTime(messageCursor.getLong(MessageQueries.COL_DATE), ctxt);
                String name = messageCursor.getString(MessageQueries.COL_FROM_NAME);
                String replyIntro = getResources().getString(R.string.sent_from, date, time, name);
                textFields[3] = (replyIntro + "\n" + messageCursor.getString(MessageQueries.COL_BODY)).replace("\n", "\n> ")
                                + "\n--\n" + signature;
            }
            return textFields;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            tvFrom.setText(strings[0]);
            tvNewsgroup.setText(strings[1]);
            txtSubject.setText(strings[2]);
            txtMessage.setText(strings[3]);
        }
    }
}
