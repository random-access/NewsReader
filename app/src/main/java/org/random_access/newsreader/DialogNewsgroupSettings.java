package org.random_access.newsreader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.random_access.newsreader.queries.NewsgroupQueries;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 18.08.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class DialogNewsgroupSettings extends DialogFragment {

    private static final String TAG = DialogNewsgroupSettings.class.getSimpleName();

    public static final String KEY_NEWSGROUP_ID = "newsgroup-id";
    private long newsgroupId;

    public static DialogNewsgroupSettings newInstance(long newsgroupId) {
        Bundle bundle = new Bundle();
        bundle.putLong(KEY_NEWSGROUP_ID, newsgroupId);
        DialogNewsgroupSettings dialog = new DialogNewsgroupSettings();
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        newsgroupId = getArguments().getLong(KEY_NEWSGROUP_ID);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View baseView = inflater.inflate(R.layout.dialog_newsgroup_settings, null);
        final RadioGroup rgMsgLoad = (RadioGroup)baseView.findViewById(R.id.rg_msgload);
        addRadioButtons(rgMsgLoad);
        final RadioGroup rgMsgKeep = (RadioGroup)baseView.findViewById(R.id.rg_msgkeep);
        addRadioButtons(rgMsgKeep);
        rgMsgLoad.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                for (int i = 0; i < checkedId; i++) {
                    if (((RadioButton)rgMsgKeep.findViewById(i)).isChecked()) {
                        rgMsgKeep.check(checkedId);
                    }
                    rgMsgKeep.findViewById(i).setEnabled(false);
                }
                for (int i = checkedId; i < rgMsgKeep.getChildCount(); i++) {
                    rgMsgKeep.findViewById(i).setEnabled(true);
                }
            }
        });
        loadValues(newsgroupId, rgMsgKeep, rgMsgLoad);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.newsgroup_settings))
                .setView(baseView)
                .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveValues(rgMsgKeep, rgMsgLoad);
                        Toast.makeText(getActivity(), getResources().getString(R.string.success_modifying_group), Toast.LENGTH_SHORT).show();
                    }
                }).setNeutralButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "cancel");
                    }
                });
        return builder.create();
    }


    private void addRadioButtons(RadioGroup rg) {
        String[] labels = getResources().getStringArray(R.array.sync_period_entries);
        for (int i = 0; i < labels.length; i++) {
            RadioButton button = new RadioButton(getActivity());
            button.setText(labels[i]);
            button.setPadding(5,5,5,5);
            button.setId(i);
            rg.addView(button, i);
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

    private int findValueOfIndex(int index) {
        return getResources().getIntArray(R.array.sync_period_values)[index];
    }

    private void loadValues (long newsgroupId, RadioGroup rgMsgKeep, RadioGroup rgMsgLoad) {
        NewsgroupQueries queries = new NewsgroupQueries(getActivity());
        Log.d(TAG, "Keep messages (db): "  + findIndexOfValue(queries.getMsgKeepDays(newsgroupId)));
        Log.d(TAG, "Load messages (db): "  + findIndexOfValue(queries.getMsgLoadDays(newsgroupId)));
        rgMsgKeep.check(findIndexOfValue(queries.getMsgKeepDays(newsgroupId)));
        rgMsgLoad.check(findIndexOfValue(queries.getMsgLoadDays(newsgroupId)));
    }

    private void saveValues(RadioGroup rgMsgKeep, RadioGroup rgMsgLoad) {
        NewsgroupQueries queries = new NewsgroupQueries(getActivity());
        queries.setMsgKeepDays(newsgroupId, findValueOfIndex(rgMsgKeep.getCheckedRadioButtonId()));
        queries.setMsgLoadDays(newsgroupId, findValueOfIndex(rgMsgLoad.getCheckedRadioButtonId()));

    }


    // TODO check selection: saveValues must be >= loadValues
}
