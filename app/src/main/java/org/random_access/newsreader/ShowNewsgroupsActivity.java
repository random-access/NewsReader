package org.random_access.newsreader;

import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
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

import org.random_access.newsreader.adapter.NewsgroupCursorAdapter;
import org.random_access.newsreader.provider.contracts.NewsgroupContract;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.06.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class ShowNewsgroupsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>  {

    private static final String TAG = ShowNewsgroupsActivity.class.getSimpleName();

    public static final String KEY_SERVER_ID = "server-id";
    public static final String KEY_SERVER_TITLE = "server-title";

    private final String[] newsgroupProjection = { NewsgroupContract.NewsgroupEntry._ID, NewsgroupContract.NewsgroupEntry.COL_NAME, NewsgroupContract.NewsgroupEntry.COL_TITLE,
            NewsgroupContract.NewsgroupEntry.COL_FK_SERV_ID};
    public static final int COL_NEWSGROUP_ID = 0;
    public static final int COL_NEWSGROUP_NAME = 1;
    public static final int COL_NEWSGROUP_TITLE = 2;
    public static final int COL_SERVER_ID = 3;

    private long serverId;

    private NewsgroupCursorAdapter mNewsgroupAdapter;
    private  ListView mNewsgroupListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serverId = getIntent().getExtras().getLong(KEY_SERVER_ID);
        setContentView(R.layout.activity_show_newsgroups);
        setTitle(getIntent().getExtras().getString(KEY_SERVER_TITLE));
        mNewsgroupListView = (ListView) findViewById(R.id.show_groups_list);
        mNewsgroupListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        mNewsgroupAdapter = new NewsgroupCursorAdapter(this, null);
        mNewsgroupListView.setAdapter(mNewsgroupAdapter);
        mNewsgroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ShowNewsgroupsActivity.this, ShowMessagesActivity.class);
                intent.putExtra(ShowMessagesActivity.KEY_ROOT_MESSAGE_ID, -1L);
                intent.putExtra(ShowMessagesActivity.KEY_SERVER_ID, serverId);
                intent.putExtra(ShowMessagesActivity.KEY_GROUP_ID, id);
                startActivity(intent);
            }
        });
        setListActions();
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_show_newsgroups, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_sync:
                ContentResolver.requestSync(ShowServerActivity.ACCOUNT, ShowServerActivity.AUTHORITY, Bundle.EMPTY);
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Starts a new or restarts an existing Loader
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, NewsgroupContract.CONTENT_URI, newsgroupProjection,
                NewsgroupContract.NewsgroupEntry.COL_FK_SERV_ID + " = ?", new String[]{serverId + ""}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mNewsgroupAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNewsgroupAdapter.swapCursor(null);
    }

    private void setListActions () {

        mNewsgroupListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mNewsgroupListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                DialogFragment dialog = DialogNewsgroupSettings.newInstance(id);
                Log.d(TAG, "ID: " + id);
                dialog.show(getFragmentManager(), "Test");
                return true;
            }
        });
    }
}
