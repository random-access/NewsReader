package org.random_access.newsreader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.random_access.newsreader.queries.ServerQueries;
import org.random_access.newsreader.queries.SettingsQueries;
import org.w3c.dom.Text;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.06.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */

public class DialogServerSettings extends DialogFragment{

    private static final String TAG = DialogServerConnection.class.getSimpleName();

    public static final String TAG_ADD_SETTINGS = "add-settings";

    private static final String TAG_SERVERTITLE = "servertitle";
    private static final String TAG_SERVER = "server";
    private static final String TAG_PORT = "port";
    private static final String TAG_ENCRYPTION = "encryption";
    private static final String TAG_AUTH = "authentication";
    private static final String TAG_USER = "user";
    private static final String TAG_PASSWORD = "password";

    private String mServerTitle;
    private String mServer;
    private int mPort;
    private boolean mEncryption; // TODO input for encryption - chooser?
    private boolean mAuthentication;
    private String mUser;
    private String mPassword;

    private Resources res;
    private LayoutInflater inflater;
    private View dialogView;
    private EditText mNameText;
    private EditText mEmailText;
    private EditText mSignatureText;
    private EditText mMsgKeepText;

    public static DialogServerSettings newInstance(String servertitle, String server, int port, boolean encryption, boolean auth, String user, String password) {
        Bundle bundle = new Bundle();
        bundle.putString(TAG_SERVERTITLE, servertitle);
        bundle.putString(TAG_SERVER, server);
        bundle.putInt(TAG_PORT, port);
        bundle.putBoolean(TAG_ENCRYPTION, encryption);
        bundle.putBoolean(TAG_AUTH, auth);
        bundle.putString(TAG_USER, user);
        bundle.putString(TAG_PASSWORD, password);
        DialogServerSettings fragment = new DialogServerSettings();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mServerTitle = getArguments().getString(TAG_SERVERTITLE);
        mServer = getArguments().getString(TAG_SERVER);
        mPort = getArguments().getInt(TAG_PORT);
        mEncryption = getArguments().getBoolean(TAG_ENCRYPTION);
        mAuthentication = getArguments().getBoolean(TAG_AUTH);
        mUser = getArguments().getString(TAG_USER);
        mPassword = getArguments().getString(TAG_PASSWORD);

        inflater = getActivity().getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_server_settings, null);
        res = getResources();
        mNameText = (EditText)dialogView.findViewById(R.id.txt_name);
        mEmailText = (EditText)dialogView.findViewById(R.id.txt_email);
        mSignatureText = (EditText)dialogView.findViewById(R.id.txt_signature);
        mMsgKeepText= (EditText) dialogView.findViewById(R.id.txt_msgkeep);
        mNameText.requestFocus();
        // d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return new MyAlertDialog(getActivity(), getResources().getString(R.string.server_settings), dialogView);
    }

    /**
     * Hack to keep dialog open when input is wrong. Needs improvement, but at least it works like expected.
     */
    private class MyAlertDialog extends AlertDialog {

        public MyAlertDialog(Context context, String title, View view) {
            super(context);
            setTitle(title);
            setView(view);
            setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.save), (new OnClickListener() {
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
                    String name = mNameText.getText().toString();
                    String email = mEmailText.getText().toString();
                    String signature = mSignatureText.getText().toString().isEmpty() ?
                            getResources().getString(R.string.signature_hint) : mSignatureText.getText().toString();
                    String msgKeep = mMsgKeepText.getText().toString();
                    handleDialogInput(name, email, signature, msgKeep);
                }
            });
        }

        private void handleDialogInput(String name, String email, String signature, String msgKeep) {
            if (TextUtils.isEmpty(email)) {
                mEmailText.setError(res.getString(R.string.error_empty_field));
                mEmailText.requestFocus();
                getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(true);
            } else {
                int msgKeepAsInt = TextUtils.isEmpty(msgKeep) ? 30 : Integer.parseInt(msgKeep);
                SettingsQueries settingsQueries = new SettingsQueries(getActivity());
                Uri uri = settingsQueries.addSettingsEntry(name, email, signature, msgKeepAsInt);
                long settingsId = Integer.parseInt(uri.getLastPathSegment());
                Log.i(TAG, uri.getPath());
                ServerQueries serverQueries = new ServerQueries(getActivity());
                uri = serverQueries.addServer(mServerTitle, mServer, mPort, false, mAuthentication, mUser, mPassword, settingsId); // TODO handle encrypted connections
                Log.i(TAG, uri.getPath());
                Toast.makeText(getActivity(), res.getString(R.string.success_adding_server), Toast.LENGTH_SHORT).show();
                dismiss();
            }
        }
    }


}
