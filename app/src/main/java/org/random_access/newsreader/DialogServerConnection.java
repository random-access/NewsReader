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
import android.widget.EditText;
import android.widget.Toast;

import org.random_access.newsreader.sync.NNTPConnector;

import java.io.IOException;

import javax.security.auth.login.LoginException;

/**
 * Project: FlashCards Manager for Android
 * Date: 18.05.15
 * Author: Monika Schrenk
 * E-Mail: software@random-access.org
 */
public class DialogServerConnection extends DialogFragment {

    private static final String TAG = DialogServerConnection.class.getSimpleName();

    public static final String TAG_ADD_SERVER = "Add-server";

    Resources res;
    LayoutInflater inflater;
    View dialogView;
    EditText mServerTitle, mServerText, mPortText, mUserText, mPasswordText;

    public static DialogServerConnection newInstance() {
        // Bundle bundle = new Bundle();
        DialogServerConnection fragment = new DialogServerConnection();
        // fragment.setArguments(bundle);
        return fragment;
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
        mServerText.requestFocus();
        MyAlertDialog d = new MyAlertDialog(getActivity(), getResources().getString(R.string.server_add), dialogView);
        // d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return d;
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
                    String user = mUserText.getText().toString();
                    String password = mPasswordText.getText().toString();
                    handleDialogInput(servertitle, server, port, user, password);
                }
            });
        }

        private void handleDialogInput(String servertitle, String server, String port, String user, String password) {
            if (TextUtils.isEmpty(server)) {
                mServerText.setError(res.getString(R.string.error_empty_field));
                mServerText.requestFocus();
            } else if (TextUtils.isEmpty(user)) {
                mUserText.setError(res.getString(R.string.error_empty_field));
                mUserText.requestFocus();
            } else {
               new ServerConnectTask().execute(servertitle, server, port, user, password);
            }
        }
    }


    class ServerConnectTask extends AsyncTask<String, Void, String[]> {

        private Exception exc;
        private String message;
        private String servertitle;
        private String server;
        private int port;
        private String user;
        private String password;

        @Override
        protected String[] doInBackground(String... params) {
            servertitle = TextUtils.isEmpty(params[0]) ? params [1] : params [0]; // if blank, servertitle = server
            server = params[1];
            port = TextUtils.isEmpty(params[2]) ? 119 : Integer.parseInt(params[2]); // if blank, serverport = 119
            user = params[3];
            password = params[4];
            message = "SERVERTITLE: " + servertitle + ", SERVER: " + server + ", PORT: " + port + ", USER: " + user +
                    ", GOT PASSWORD: " + (!TextUtils.isEmpty(password));
            try {
                NNTPConnector connector = new NNTPConnector(getActivity());
                connector.connectToNewsServer(getActivity(), server, port, user, password);
            } catch (IOException | LoginException e) {
                exc = e;
            }
            return params;
        }

        @Override
        protected void onPostExecute(String[] args) {
            if (exc == null) {
                Toast.makeText(getActivity(), res.getString(R.string.success_connect_server), Toast.LENGTH_SHORT).show();
                dismiss();
                DialogServerSettings serverSettingsFragment = DialogServerSettings.newInstance(servertitle, server, port, false, user, password);
                serverSettingsFragment.show(getFragmentManager(), DialogServerSettings.TAG_ADD_SETTINGS);
            } else {
                Log.e(TAG, "Error in ServerConnectTask: " + message);
                if (exc.getClass() == LoginException.class) {
                    Toast.makeText(getActivity(), res.getString(R.string.error_connection), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), res.getString(R.string.error_password), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}