package org.random_access.newsreader;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

/**
 * <b>Project:</b> FlashCards Manager for Android <br>
 * <b>Date:</b> 27.06.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class MessageSettingsActivity extends PreferenceActivity{
    private static final String TAG = MessageSettingsActivity.class.getSimpleName();

    private  Preference viewMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MessageSettingsFragment()).commit();
    }

    public static class MessageSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_messages);
        }
    }
}
