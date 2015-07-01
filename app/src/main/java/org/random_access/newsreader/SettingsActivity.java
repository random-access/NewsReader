package org.random_access.newsreader;

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.util.Log;

import org.random_access.newsreader.sync.NNTPSyncAdapter;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 26.06.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class SettingsActivity extends PreferenceActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            Preference syncInterval = findPreference("pref_sync_interval");
            syncInterval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    long newSyncInterval = Long.parseLong((String)newValue) * ShowServerActivity.SECONDS_PER_MINUTE;
                    ContentResolver.removePeriodicSync(ShowServerActivity.ACCOUNT, ShowServerActivity.AUTHORITY, Bundle.EMPTY);
                    ContentResolver.addPeriodicSync(ShowServerActivity.ACCOUNT, ShowServerActivity.AUTHORITY, Bundle.EMPTY, newSyncInterval);
                    Log.i(TAG, "Periodic sync changed to " + (newSyncInterval / ShowServerActivity.SECONDS_PER_MINUTE));
                    return true;
                }
            });
        }
    }



}
