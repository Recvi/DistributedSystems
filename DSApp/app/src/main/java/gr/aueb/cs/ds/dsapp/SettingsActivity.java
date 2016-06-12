package gr.aueb.cs.ds.dsapp;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;


public class SettingsActivity extends PreferenceActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MySettings()).commit();

    }

    public static class MySettings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext()).registerOnSharedPreferenceChangeListener(this);

            addPreferencesFromResource(R.xml.pref_general);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
            System.out.println(sharedPreferences.getString(key,"none"));
            Preference pref = findPreference(key);
            pref.setSummary(sharedPreferences.getString(key,""));
        }

    }

}
