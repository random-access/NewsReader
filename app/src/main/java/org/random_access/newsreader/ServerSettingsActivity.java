package org.random_access.newsreader;

import android.app.FragmentManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.random_access.newsreader.queries.ServerQueries;
import org.random_access.newsreader.queries.SettingsQueries;
import org.random_access.newsreader.sync.NNTPConnector;

import java.io.IOException;

import javax.security.auth.login.LoginException;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 18.08.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class ServerSettingsActivity extends AppCompatActivity {

    private static final String TAG = ServerSettingsActivity.class.getSimpleName();

    public static final String TAG_SERVER_ID = "server-id";
    private long serverId;

    private static final String TAG_SERVER_SETTINGS = "server-settings";

    private ServerSettingsFragment serverSettingsFragment;
    private EditText txtServerTitle, txtServer, txtPort, txtUserName, txtPassword, txtUserDisplayName, txtEmailAddress, txtSignature;
    private CheckBox chkAuth, chkEncryption;
    private Spinner spMsgLoadPeriod;
    private TextView lblUser, lblPassword;

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
        switch (id) {
            case R.id.action_save:
               modifyServerSettings();
                return true;
            case R.id.action_discard:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void modifyServerSettings() {
        if (TextUtils.isEmpty(txtServer.getText().toString())) {
            txtServer.setError(getResources().getString(R.string.error_empty_field));
            txtServer.requestFocus();
        } else if (serverSettingsFragment.isAuth() && TextUtils.isEmpty(txtUserName.getText().toString())) {
            txtUserName.setError(getResources().getString(R.string.error_empty_field));
            txtUserName.requestFocus();
        } else if (TextUtils.isEmpty(txtEmailAddress.getText().toString())) {
            txtEmailAddress.setError(getResources().getString(R.string.error_empty_field));
            txtEmailAddress.requestFocus();
        } else {
            updateSettingsFragment();
            new ServerConnectTask().execute();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serverSettingsFragment != null) {
            updateSettingsFragment();
        }
    }

    private void updateSettingsFragment() {
        String standardPort = chkEncryption.isChecked() ? "563" : "119"; // TODO global vars
        serverSettingsFragment.setServerTitle(TextUtils.isEmpty(txtServerTitle.getText().toString()) ? txtServer.getText().toString() : txtServerTitle.getText().toString());
        serverSettingsFragment.setServerName(txtServer.getText().toString());
        serverSettingsFragment.setServerPort(TextUtils.isEmpty(txtPort.getText().toString()) ? standardPort : txtPort.getText().toString());
        serverSettingsFragment.setEncryption(chkEncryption.isChecked());
        serverSettingsFragment.setAuth(chkAuth.isChecked());
        serverSettingsFragment.setUserName(txtUserName.getText().toString());
        serverSettingsFragment.setPassword(txtPassword.getText().toString());
        serverSettingsFragment.setUserDisplayName(txtUserDisplayName.getText().toString());
        serverSettingsFragment.setMailAddress(txtEmailAddress.getText().toString());
        serverSettingsFragment.setSignature(txtSignature.getText().toString());
        serverSettingsFragment.setChooseMsgLoadTimeIndex(spMsgLoadPeriod.getSelectedItemPosition());
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

    private class LoadServerSettingsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            ServerQueries serverQueries = new ServerQueries(ServerSettingsActivity.this);
            Cursor c = serverQueries.getServerWithId(serverId);
            if (c.moveToFirst()) {
                serverSettingsFragment.setServerTitle(c.getString(ServerQueries.COL_TITLE));
                serverSettingsFragment.setServerName(c.getString(ServerQueries.COL_NAME));
                serverSettingsFragment.setServerPort(c.getString(ServerQueries.COL_PORT));
                serverSettingsFragment.setEncryption(c.getInt(ServerQueries.COL_ENCRYPTION) == 1);
                serverSettingsFragment.setAuth(c.getInt(ServerQueries.COL_AUTH) == 1);
                serverSettingsFragment.setUserName(c.getString(ServerQueries.COL_USER));
                serverSettingsFragment.setPassword(c.getString(ServerQueries.COL_PASSWORD));
            }
            c.close();
            SettingsQueries settingsQueries = new SettingsQueries(ServerSettingsActivity.this);
            c = settingsQueries.getSettingsForServer(serverId);
            if (c.moveToFirst()) {
                serverSettingsFragment.setUserDisplayName(c.getString(SettingsQueries.COL_NAME));
                serverSettingsFragment.setMailAddress(c.getString(SettingsQueries.COL_EMAIL));
                serverSettingsFragment.setSignature(c.getString(SettingsQueries.COL_SIGNATURE));
                serverSettingsFragment.setChooseMsgLoadTimeIndex(findIndexOfValue(c.getInt(SettingsQueries.COL_MSG_LOAD_DEFAULT)));
            }
            c.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            prepareGui();
        }
    }

    private int findIndexOfValue(int value) {
        int[] array = getResources().getIntArray(R.array.sync_period_values);
        for (int i = 0; i < array.length; i++ ) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    private void prepareGui() {
        txtServerTitle = (EditText) findViewById(R.id.txt_servertitle);
        txtServer = (EditText) findViewById(R.id.txt_server);
        txtPort = (EditText) findViewById(R.id.txt_port);
        chkEncryption = (CheckBox) findViewById(R.id.chk_ssl);
        chkAuth = (CheckBox) findViewById(R.id.chk_auth);
        lblUser = (TextView) findViewById(R.id.lbl_user);
        txtUserName = (EditText) findViewById(R.id.txt_user);
        lblPassword = (TextView) findViewById(R.id.lbl_pass);
        txtPassword = (EditText) findViewById(R.id.txt_password);
        txtUserDisplayName = (EditText) findViewById(R.id.txt_name);
        txtEmailAddress = (EditText) findViewById(R.id.txt_email);
        txtSignature = (EditText) findViewById(R.id.txt_signature);
        spMsgLoadPeriod = (Spinner) findViewById(R.id.rg_msgload);

        txtServerTitle.setText(serverSettingsFragment.getServerTitle() == null ? "" : serverSettingsFragment.getServerTitle() );
        txtServer.setText(serverSettingsFragment.getServerName() == null ? "" : serverSettingsFragment.getServerName());
        txtPort.setText(serverSettingsFragment.getServerPort() == null ? "" : serverSettingsFragment.getServerPort());
        chkEncryption.setChecked(serverSettingsFragment.hasEncryption());
        chkAuth.setChecked(serverSettingsFragment.isAuth());
        manageAuthVisibility(chkAuth.isChecked());
        txtUserName.setText(serverSettingsFragment.getUserName() == null ? "" : serverSettingsFragment.getUserName());
        txtPassword.setText(serverSettingsFragment.getPassword() == null ? "" : serverSettingsFragment.getPassword());
        txtUserDisplayName.setText(serverSettingsFragment.getUserDisplayName() == null ? "" : serverSettingsFragment.getUserDisplayName());
        txtEmailAddress.setText(serverSettingsFragment.getMailAddress() == null ? "" : serverSettingsFragment.getMailAddress());
        txtSignature.setText(serverSettingsFragment.getSignature() == null ? "" : serverSettingsFragment.getSignature());
        spMsgLoadPeriod.setSelection(serverSettingsFragment.getChooseMsgLoadTimeIndex());

        chkAuth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                manageAuthVisibility(isChecked);
            }
        });
        chkEncryption.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                txtPort.setHint(isChecked ? getResources().getString(R.string.port_hint_ssl) : getResources().getString(R.string.port_hint));
            }
        });
    }

    private void manageAuthVisibility(boolean isChecked) {
        lblUser.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        txtUserName.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        lblPassword.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        txtPassword.setVisibility(isChecked ? View.VISIBLE : View.GONE);
    }

    class ServerConnectTask extends AsyncTask<String, Void, String[]> {
        private String msg;
        private String debugOutput;

        @Override
        protected String[] doInBackground(String... params) {
            debugOutput = "SERVERTITLE: " + serverSettingsFragment.getServerTitle() + ", SERVER: " + serverSettingsFragment.getServerName() + ", PORT: " + serverSettingsFragment.getServerPort()
                    + ", AUTH: " + serverSettingsFragment.isAuth() + ", USER: " + serverSettingsFragment.getUserName() + ", GOT PASSWORD: " + (!TextUtils.isEmpty(serverSettingsFragment.getPassword()));

            if (NetworkStateHelper.isOnline(ServerSettingsActivity.this)) {
                try {
                    int port = Integer.parseInt(TextUtils.isEmpty(serverSettingsFragment.getServerPort()) ? "119" : serverSettingsFragment.getServerPort());// TODO handle standard ports
                    NNTPConnector connector = new NNTPConnector(ServerSettingsActivity.this);
                    connector.connectToNewsServer(serverSettingsFragment.getServerName(), port, serverSettingsFragment.hasEncryption(),
                            serverSettingsFragment.isAuth(), serverSettingsFragment.getUserName(), serverSettingsFragment.getPassword());
                    ServerQueries serverQueries = new ServerQueries(ServerSettingsActivity.this);
                    serverQueries.modifyServer(serverId, serverSettingsFragment.getServerTitle(), serverSettingsFragment.getServerName(),
                            port, serverSettingsFragment.hasEncryption(), serverSettingsFragment.isAuth(), serverSettingsFragment.getUserName(),
                            serverSettingsFragment.getPassword());
                    long settingsId = serverQueries.getServerSettingsId(serverId);
                    SettingsQueries settingsQueries = new SettingsQueries(ServerSettingsActivity.this);
                    int loadTimeSpan = getResources().getIntArray(R.array.sync_period_values)[serverSettingsFragment.getChooseMsgLoadTimeIndex()];
                    settingsQueries.modifySettingsEntry(settingsId, serverSettingsFragment.getUserDisplayName(), serverSettingsFragment.getMailAddress(),
                            serverSettingsFragment.getSignature(), loadTimeSpan);
                } catch (IOException e) {
                    msg = getResources().getString(R.string.error_connection);
                } catch (LoginException e) {
                    msg = getResources().getString(R.string.error_password);
                }
            } else {
                msg = getResources().getString(R.string.error_offline);
            }
            return params;
        }

        @Override
        protected void onPostExecute(String[] args) {
            if (TextUtils.isEmpty(msg)) {
                Toast.makeText(ServerSettingsActivity.this, getResources().getString(R.string.success_modifying_server), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Successfully connected to server: " + debugOutput);
                finish();
            } else {
                Log.e(TAG, "Error in ServerConnectTask: " + debugOutput);
                Toast.makeText(ServerSettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
