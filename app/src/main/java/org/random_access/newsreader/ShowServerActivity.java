package org.random_access.newsreader;

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.DialogInterface;
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
import android.widget.Toast;

import org.random_access.newsreader.adapter.ServerCursorAdapter;
import org.random_access.newsreader.provider.contracts.ServerContract;
import org.random_access.newsreader.queries.ServerQueries;
import org.random_access.newsreader.sync.NNTPSyncDummyAccount;

/**
 * <b>Project:</b> FlashCards Manager for Android <br>
 * <b>Date:</b> 18.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class ShowServerActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "org.random_access.newsreader.provider";

    private Account mAccount;

    // Sync interval constants
    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long SYNC_INTERVAL_IN_MINUTES = 2L; //15L;
    public static final long SYNC_INTERVAL = SYNC_INTERVAL_IN_MINUTES * SECONDS_PER_MINUTE;

    String[] serverProjection = { ServerContract.ServerEntry._ID, ServerContract.ServerEntry.COL_TITLE,
            ServerContract.ServerEntry.COL_SERVERNAME };
    public static final int COL_SERVER_ID = 0;
    public static final int COL_SERVER_TITLE = 1;
    public static final int COL_SERVER_NAME = 2;

    private static final String TAG = ShowServerActivity.class.getSimpleName();

    private ListView mServerListView;
    private ServerCursorAdapter mServerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_server);
        // Create the dummy account
        mAccount = NNTPSyncDummyAccount.createSyncAccount(this);
        ContentResolver.addPeriodicSync(mAccount, AUTHORITY, Bundle.EMPTY, SYNC_INTERVAL);
        mServerListView = (ListView)findViewById(R.id.server_list);
        mServerListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        mServerAdapter = new ServerCursorAdapter(this, null);
        setListActions();
        mServerListView.setAdapter(mServerAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_server, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_add_project:
                DialogServerConnection addServerFragment = DialogServerConnection.newInstance();
                addServerFragment.show(getFragmentManager(), DialogServerConnection.TAG_ADD_SERVER);
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
        return new CursorLoader(this, ServerContract.CONTENT_URI, serverProjection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mServerAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mServerAdapter.swapCursor(null);
    }

    private void deleteSelectedServer() {
        long[] currentSelections = mServerListView.getCheckedItemIds();
        OnDeleteProjectsDialogListener dialogClickListener = new OnDeleteProjectsDialogListener(currentSelections);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setNeutralButton(getResources().getString(R.string.no), dialogClickListener)
                .setPositiveButton(getResources().getString(R.string.yes), dialogClickListener)
                .setTitle(getResources().getString(R.string.delete))
                .setMessage(getResources().getQuantityString(R.plurals.really_delete_server, currentSelections.length, currentSelections.length))
                .setCancelable(false);
        builder.show();
    }


    class OnDeleteProjectsDialogListener implements DialogInterface.OnClickListener {

        long[] currentSelection;

        OnDeleteProjectsDialogListener(long[] currentSelection) {
            this.currentSelection = currentSelection;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    int selCount = currentSelection.length;
                    for (long l : currentSelection) {
                        new ServerQueries(ShowServerActivity.this).deleteServerWithId(l);
                        Log.d(TAG, "Delete server with id " + l);
                    }
                    Toast.makeText(ShowServerActivity.this, getResources().
                            getQuantityString(R.plurals.deleted_server, selCount, selCount), Toast.LENGTH_SHORT).show();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    // user cancelled
                    break;
            }
        }
    };

    private void setListActions () {
        mServerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mServerAdapter.getmCurrentDetailPosition() == -1) {
                    mServerAdapter.setmCurrentDetailPosition(position);
                } else if (mServerAdapter.getmCurrentDetailPosition() == position) {
                    mServerAdapter.setmCurrentDetailPosition(-1);
                } else {
                    mServerAdapter.setmCurrentDetailPosition(position);
                }
                Log.d(TAG, "Set mCurrentDetailView to " + mServerAdapter.getmCurrentDetailPosition());
            }
        });

        mServerListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mServerListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_server_context, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.action_delete_server:
                        deleteSelectedServer();
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
    }


}

