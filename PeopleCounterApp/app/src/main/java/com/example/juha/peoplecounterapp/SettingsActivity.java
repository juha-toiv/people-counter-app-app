package com.example.juha.peoplecounterapp;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;


public class SettingsActivity extends UserAuthStateListenerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_item_signout:
                mFirebaseAuth.signOut();
                return true;
            case R.id.menu_item_device_list:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class SettingsFragment extends PreferenceFragment{

        private Preference mReceiveNotificationsPreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            mReceiveNotificationsPreference = (Preference) getPreferenceManager().findPreference("key_notifications_new_message");
            mReceiveNotificationsPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Boolean isReceivingNotifications = Boolean.parseBoolean(o.toString());
                    if (FirebaseAuth.getInstance() != null) {
                        if (isReceivingNotifications) {
                            FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.notification_receiveNotificationsTopic));
                        } else {
                            FirebaseMessaging.getInstance().unsubscribeFromTopic(getString(R.string.notification_receiveNotificationsTopic));
                        }
                        return true;
                    }
                    return false;
                }
            });
        }

    }

}
