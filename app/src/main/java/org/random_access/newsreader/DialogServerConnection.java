package org.random_access.newsreader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.random_access.newsreader.sync.NNTPConnector;

import java.io.IOException;

import javax.security.auth.login.LoginException;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.06.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class DialogServerConnection extends DialogFragment {

    private static final String TAG = DialogServerConnection.class.getSimpleName();

    public static final String TAG_ADD_SERVER = "Add-server";

    private Resources res;
    private LayoutInflater inflater;
    private View dialogView;
    private EditText mServerTitle;
    private EditText mServerText;
    private EditText mPortText;
    private EditText mUserText;
    private EditText mPasswordText;
    private TextView mUserLabel;
    private TextView mPassLabel;
    private CheckBox mAuthentication;

    private AlertDialog dialog;

    public static DialogServerConnection newInstance() {
        // Bundle bundle = new Bundle();
        // DialogServerConnection fragment = new DialogServerConnection();
        // fragment.setArguments(bundle);
        return new DialogServerConnection();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        inflater = getActivity().getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_add_server, null);
        res = getResources();
        mServerTitle = (EditText) dialogView.findViewById(R.id.txt_servertitle);
        mServerText = (EditText)dialogView.findViewById(R.id.txt_server);
        mPortText = (EditText)dialogView.findViewById(R.id.txt_port);
        mUserText = (EditText)dialogView.findViewById(R.id.txt_user);
        mPasswordText = (EditText) dialogView.findViewById(R.id.txt_password);
        mUserLabel = (TextView) dialogView.findViewById(R.id.lbl_user);
        mPassLabel = (TextView) dialogView.findViewById(R.id.lbl_pass);
        mAuthentication = (CheckBox) dialogView.findViewById(R.id.chk_auth);
        mAuthentication.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mUserLabel.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                mUserText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                mPassLabel.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                mPasswordText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
        mServerText.requestFocus();
        // d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog = new MyAlertDialog(getActivity(), getResources().getString(R.string.server_add), dialogView);
        return dialog;
    }

    /**
     * Hack to keep dialog open when input is wrong. Needs improvement, but at least it works like expected.
     */
    private class MyAlertDialog extends AlertDialog {

        public MyAlertDialog(Context context, String title, View view) {
            super(context);
            setTitle(title);
            setView(view);
            setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.connect), (new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // this will never be called
                }
            }));
            setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.cancel), (new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismiss();
                }
            }));
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);
                    String servertitle = mServerTitle.getText().toString();
                    String server = mServerText.getText().toString();
                    String port = mPortText.getText().toString();
                    String withAuth = mAuthentication.isChecked() ? "1" : "0";
                    String user = mUserText.getText().toString();
                    String password = mPasswordText.getText().toString();
                    handleDialogInput(servertitle, server, port, withAuth, user, password);
                }
            });
        }

        private void handleDialogInput(String servertitle, String server, String port, String withAuth, String user, String password) {
            if (TextUtils.isEmpty(server)) {
                mServerText.setError(res.getString(R.string.error_empty_field));
                mServerText.requestFocus();
                getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(true);
            } else {
               new ServerConnectTask().execute(servertitle, server, port, withAuth, user, password);
            }
        }
    }


    class ServerConnectTask extends AsyncTask<String, Void, String[]> {

        private String msg;
        private String message;
        private String servertitle;
        private String server;
        private int port;
        private boolean withAuth;
        private String user;
        private String password;

        @Override
        protected String[] doInBackground(String... params) {
            servertitle = TextUtils.isEmpty(params[0]) ? params [1] : params [0]; // if blank, servertitle = server
            server = params[1];
            port = TextUtils.isEmpty(params[2]) ? 119 : Integer.parseInt(params[2]); // if blank, serverport = 119
            withAuth = params[3].equals("1");
            user = params[4];
            password = params[5];
            message = "SERVERTITLE: " + servertitle + ", SERVER: " + server + ", PORT: " + port + ", AUTH: " + withAuth + ", USER: " + user +
                    ", GOT PASSWORD: " + (!TextUtils.isEmpty(password));
            if (NetworkStateHelper.isOnline(getActivity())) {
                try {
                    NNTPConnector connector = new NNTPConnector(getActivity());
                    connector.connectToNewsServer(getActivity(), server, port, withAuth, user, password);
                } catch (IOException e) {
                    msg = res.getString(R.string.error_connection);
                } catch (LoginException e) {
                    msg = res.getString(R.string.error_password);
                }
            } else {
                msg = res.getString(R.string.error_offline);
            }
            return params;
        }

        @Override
        protected void onPostExecute(String[] args) {
            if (TextUtils.isEmpty(msg)) {
                Toast.makeText(getActivity(), res.getString(R.string.success_connect_server), Toast.LENGTH_SHORT).show();
                dismiss();
                DialogServerSettings serverSettingsFragment = DialogServerSettings.newInstance(servertitle, server, port, false, withAuth, user, password);
                serverSettingsFragment.show(getFragmentManager(), DialogServerSettings.TAG_ADD_SETTINGS);
            } else {
                Log.e(TAG, "Error in ServerConnectTask: " + message);
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(true);
            }
        }
    }

}