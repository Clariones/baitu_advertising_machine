package com.skynet.adplayer.activities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.skynet.adplayer.BuildConfig;
import com.skynet.adplayer.R;
import com.skynet.adplayer.common.Constants;
import com.skynet.adplayer.utils.MiscUtils;
import com.skynet.adplayer.utils.SystemPropertyUtils;

public class SettingsActivity extends AppCompatActivity {    @Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    getFragmentManager().beginTransaction()
            .replace(R.id.preference_block, new SettingsFragment())
            .commit();
}


    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.addPreferencesFromResource(R.xml.preferences);

            Preference button = findPreference("pref_key_close_and_return");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SettingsFragment.this.getActivity().finish();
                    return true;
                }
            });

            Preference btnReboot = findPreference("pref_key_reboot");
            btnReboot.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder inputDialog = new AlertDialog.Builder(SettingsFragment.this.getActivity());
                    inputDialog.setTitle("确定要重启系统么？");
                    inputDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SettingsFragment.this.getActivity().finish();
                            MiscUtils.restart(MainActivity.me);
                        }
                    });
                    inputDialog.setNegativeButton("取消", null);
                    inputDialog.show();
                    return true;
                }
            });

            //
            Preference btnClearPlayList = findPreference("pref_key_clear_play_list");
            btnClearPlayList.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder inputDialog = new AlertDialog.Builder(SettingsFragment.this.getActivity());
                    inputDialog.setTitle("清除播放列表后，将没有内容可以播放。确定清除吗？");
                    inputDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SettingsFragment.this.getActivity().finish();
                            MainActivity.me.clearAllPlayList();
                        }
                    });
                    inputDialog.setNegativeButton("取消", null);
                    inputDialog.show();
                    return true;
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            initSummaries();
        }

        private void initSummaries() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            updateStringSummary(prefs, "pref_key_model_name", Build.MANUFACTURER+" "+SystemPropertyUtils.getModel());
            updateStringSummary(prefs, "pref_key_serial_number", SystemPropertyUtils.getSerialNo());
            updateStringSummary(prefs, Constants.PREF_KEY_ADMIN_PASSWORD, prefs.getString(Constants.PREF_KEY_ADMIN_PASSWORD, Constants.DEFAULT_ADMIN_PASSWORD));
            updateStringSummary(prefs, "pref_key_apk_version", BuildConfig.VERSION_NAME);
            updateStringSummary(prefs, "pref_key_ext_storage", Environment.getExternalStorageDirectory().getAbsolutePath());
        }

        private void updateStringSummary(SharedPreferences prefs, String key, String value) {
            Preference pref = findPreference(key);
            pref.setSummary(value);
            //Log.i(TAG, "Prereference " + key + " value is " + prefs.getString(key, ""));
        }
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        }
    }
}
