package org.random_access.newsreader;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import org.random_access.newsreader.adapter.MessageCursorAdapter;
import org.random_access.newsreader.queries.MessageQueries;
import org.random_access.newsreader.queries.NewsgroupQueries;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.06.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class ShowMessagesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = ShowMessagesActivity.class.getSimpleName();

    public static final int RESULT_DATA_CHANGE = 1001;

    public static final String KEY_SERVER_ID = "server-id";
    public static final String KEY_GROUP_ID = "group-id";

    private long serverId;
    private long groupId;
    private String groupName;

    private MessageCursorAdapter mMessageAdapter;
    private ListView lvMessages;

    private SharedPreferences sharedPreferences;
    private Boolean showOnlyTopItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        serverId = getIntent().getExtras().getLong(KEY_SERVER_ID);
        groupId = getIntent().getExtras().getLong(KEY_GROUP_ID);
        groupName = new NewsgroupQueries(ShowMessagesActivity.this).getNewsgroupName(groupId);
        setTitle(groupName);
        setContentView(R.layout.activity_show_messages);
        lvMessages = (ListView)findViewById(R.id.show_messages_list);
        readViewModeFromSettings();
        mMessageAdapter = new MessageCursorAdapter(this, null, showOnlyTopItems);
        lvMessages.setAdapter(mMessageAdapter);
        lvMessages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ShowMessagesActivity.this, ShowSingleArticleActivity.class);
                intent.putExtra(ShowSingleArticleActivity.KEY_SERVER_ID, serverId);
                intent.putExtra(ShowSingleArticleActivity.KEY_GROUP_ID, groupId);
                intent.putExtra(ShowSingleArticleActivity.KEY_GROUP_NAME, groupName);
                intent.putExtra(ShowSingleArticleActivity.KEY_MESSAGE_ID, id);
                startActivity(intent);
            }
        });
        setListActions();
        getLoaderManager().initLoader(0, null, this);
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
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, MessageSettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_new_message:
                Intent sendIntent = new Intent(ShowMessagesActivity.this, WriteMessageActivity.class);
                sendIntent.putExtra(WriteMessageActivity.KEY_SERVER_ID, serverId);
                sendIntent.putExtra(WriteMessageActivity.KEY_NEWSGROUP_ID, groupId);
                sendIntent.putExtra(WriteMessageActivity.KEY_MESSAGE_ID, WriteMessageActivity.NEW_MESSAGE);
                startActivity(sendIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        readViewModeFromSettings();
        Log.d(TAG, "show only top items ? " + showOnlyTopItems);
        // Starts a new or restarts an existing Loader
        getLoaderManager().restartLoader(0, null, this);
    }

    private void readViewModeFromSettings() {
        String viewMode = sharedPreferences.getString("pref_message_view", "f");
        if (viewMode != null) {
            showOnlyTopItems = viewMode.equals("h");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MessageQueries(this).getMessagesInCursorLoader(groupId, showOnlyTopItems);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMessageAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMessageAdapter.changeCursor(null);
    }

    private void setListActions () {

        lvMessages.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lvMessages.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_messages_context, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.action_mark_read:
                        markMessagesNew(false);
                        mode.finish();
                        return true;
                    case R.id.action_mark_unread:
                        markMessagesNew(true);
                        mode.finish();
                        return true;
                    case R.id.action_select_all:
                        setAllItemsChecked();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) { /* unused */ }

            @Override
            public void onDestroyActionMode(ActionMode mode) { /* unused */ }
        });
    }

    private void markMessagesNew(boolean isNew) {
        long[] ids = lvMessages.getCheckedItemIds();
        MessageQueries queries = new MessageQueries(this);
        for (long l : ids) {
            queries.setMessageUnread(l, isNew);
        }
    }

    private void setAllItemsChecked() {
        int count = lvMessages.getCount();
        for (int i = 0; i < count; i++) {
            lvMessages.setItemChecked(i, true);
        }
    }

}
