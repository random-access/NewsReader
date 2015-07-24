package org.random_access.newsreader;

import android.app.FragmentManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import org.random_access.newsreader.queries.ServerQueries;


public class ServerSettingsActivity extends AppCompatActivity {

    public static final String TAG_SERVER_ID = "server-id";
    private long serverId;

    private static final String TAG_SERVER_SETTINGS = "server-settings";

    private ServerSettingsFragment serverSettingsFragment;
    private EditText txtServerTitle, txtServer, txtPort, txtUserName, txtPassword, txtUserDisplayName, txtEmailAddress, txtSignature;
    private CheckBox chkAuth;
    private Spinner spMsgKeepPeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serverId = getIntent().getExtras().getLong(TAG_SERVER_ID);
        setContentView(R.layout.activity_server_settings);
        loadServerSettings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_server_settings, menu);
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
        if (serverSettingsFragment != null) {
            serverSettingsFragment.setServerTitle(txtServerTitle.getText().toString());
            serverSettingsFragment.setServerName(txtServer.getText().toString());
            serverSettingsFragment.setServerPort(txtPort.getText().toString());
            serverSettingsFragment.setAuth(chkAuth.isChecked());
            serverSettingsFragment.setUserName(txtUserName.getText().toString());
            serverSettingsFragment.setPassword(txtPassword.getText().toString());
            serverSettingsFragment.setUserDisplayName(txtUserDisplayName.getText().toString());
            serverSettingsFragment.setMailAddress(txtEmailAddress.getText().toString());
            serverSettingsFragment.setSignature(txtSignature.getText().toString());
            serverSettingsFragment.setChooseMsgKeepTimeIndex(spMsgKeepPeriod.getSelectedItemPosition());
        }
    }

    private void loadServerSettings() {
        FragmentManager fragmentManager = getFragmentManager();
        serverSettingsFragment = (ServerSettingsFragment)fragmentManager.findFragmentByTag(TAG_SERVER_SETTINGS);
        if (serverSettingsFragment == null) {
            serverSettingsFragment = new ServerSettingsFragment();
            fragmentManager.beginTransaction().add(serverSettingsFragment, TAG_SERVER_SETTINGS).commit();
            if (serverId != -1) {
                new LoadServerSettingsTask().execute();
            }
        } else {
            prepareGui();
        }
    }

    class LoadServerSettingsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // TODO get values
            ServerQueries serverQueries = new ServerQueries(ServerSettingsActivity.this);
            Cursor c = serverQueries.getServerWithId(serverId);
            if (c.moveToFirst()) {
                serverSettingsFragment.setServerTitle(c.getString(ServerQueries.COL_TITLE));
                serverSettingsFragment.setServerName(c.getString(ServerQueries.COL_NAME));
                serverSettingsFragment.setServerPort(c.getString(ServerQueries.COL_PORT));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            prepareGui();
        }
    }

    private void prepareGui() {
        txtServerTitle = (EditText) findViewById(R.id.txt_servertitle);
        txtServer = (EditText) findViewById(R.id.txt_server);
        txtPort = (EditText) findViewById(R.id.txt_port);
        chkAuth = (CheckBox) findViewById(R.id.chk_auth);
        txtUserName = (EditText) findViewById(R.id.txt_user);
        txtPassword = (EditText) findViewById(R.id.txt_password);
        txtUserDisplayName = (EditText) findViewById(R.id.txt_name);
        txtEmailAddress = (EditText) findViewById(R.id.txt_email);
        txtSignature = (EditText) findViewById(R.id.txt_signature);
        spMsgKeepPeriod = (Spinner) findViewById(R.id.sp_msgkeep);

        txtServerTitle.setText(serverSettingsFragment.getServerTitle() == null ? "" : serverSettingsFragment.getServerTitle() );
        txtServer.setText(serverSettingsFragment.getServerName() == null ? "" : serverSettingsFragment.getServerName());
        txtPort.setText(serverSettingsFragment.getServerPort() == null ? "" : serverSettingsFragment.getServerPort());
        chkAuth.setChecked(serverSettingsFragment.isAuth());
        txtUserName.setText(serverSettingsFragment.getUserName() == null ? "" : serverSettingsFragment.getUserName());
        txtPassword.setText(serverSettingsFragment.getPassword() == null ? "" : serverSettingsFragment.getPassword());
        txtUserDisplayName.setText(serverSettingsFragment.getUserDisplayName() == null ? "" : serverSettingsFragment.getUserDisplayName());
        txtEmailAddress.setText(serverSettingsFragment.getMailAddress() == null ? "" : serverSettingsFragment.getMailAddress());
        txtSignature.setText(serverSettingsFragment.getSignature() == null ? "" : serverSettingsFragment.getSignature());
        spMsgKeepPeriod.setSelection(serverSettingsFragment.getChooseMsgKeepTimeIndex());
    }
}
