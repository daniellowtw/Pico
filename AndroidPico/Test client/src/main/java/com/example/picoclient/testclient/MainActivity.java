package com.example.picoclient.testclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;

public class MainActivity extends ActionBarActivity {
    // Actions this activity can do. To be used by intent service
    public static final String UNLOCK_APP = "UNLOCK_APP";
    public static final String LOCK_APP = "LOCK_APP";
    public static final String DECRYPT_FILE = "DECRYPT_FILE";
    public static final String NOTIFY_USER = "NOTIFY_USER";
    public static final String NOTIFY_USER_MESSAGE = "NOTIFY_USER_MESSAGE";
    /* Putting state variables here so they can be persistent throughout Activity's life */
    MainFragment mainFragment;
    SharedPreferences appPref;
    // TODO: Should this be here or in the fragment?
    private AlarmBroadcastReceiver alarmBroadcastReceiver;
    private String uid;

    @Override
    protected void onDestroy() {
        Log.i(this.getClass().getSimpleName(), "onDestroy called");
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        Log.i(this.getClass().getSimpleName(), "onStop called");
        super.onStop();
    }

    @Override
    protected void onRestart() {
        Log.i(this.getClass().getSimpleName(), "onRestart called");
        super.onRestart();
    }

    @Override
    protected void onStart() {
        Log.i(this.getClass().getSimpleName(), "onStart called " + getApplicationContext());
        Log.i(this.getClass().getSimpleName(), "Secret is " + appPref.getString("SecretKey", "No secret found"));
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(this.getClass().getSimpleName(), "onStop called");
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(this.getClass().getSimpleName(), "onCreate called");
        alarmBroadcastReceiver = new AlarmBroadcastReceiver();
        //The following preference can only be accessed by the activity.
        appPref = getPreferences(Context.MODE_PRIVATE);
        mainFragment = new MainFragment();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            Log.i(this.getClass().getSimpleName(), "No saved instance state");
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mainFragment)
                    .commit();
        } else {
            Log.i(this.getClass().getSimpleName(), "Saved state");
            Log.i("a", savedInstanceState.toString());
        }
        uid = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        ResponseReceiver responseReceiver = new ResponseReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(responseReceiver, new IntentFilter(UNLOCK_APP));
        LocalBroadcastManager.getInstance(this).registerReceiver(responseReceiver, new IntentFilter(LOCK_APP));
        LocalBroadcastManager.getInstance(this).registerReceiver(responseReceiver, new IntentFilter(DECRYPT_FILE));
        LocalBroadcastManager.getInstance(this).registerReceiver(responseReceiver, new IntentFilter(NOTIFY_USER));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onCreateFile(View v) {
        mainFragment.saveFile(getApplicationContext());
    }

    public void onShowFile(View v) {
        String fileSecret = null;
        try {
            fileSecret = mainFragment.readFile(getApplicationContext());
            Toast.makeText(this, "File content is: " + fileSecret, Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "File content is: Error getting string", Toast.LENGTH_SHORT).show();
        }
    }

    public void onTogglePolling(View v) {
        if (mainFragment != null) {
            mainFragment.togglePolling(v);
        }
    }

    public void onConnectServer(View v) {
        ServerAPIIntentService.getKeyCount(getApplicationContext(), uid);
        Log.i("ButtonPress", "Asking for key Count");
    }

    public void onLockApp(View v) {
        mainFragment.lockApp();
    }

    public void onDecrypt(View v) {
        ServerAPIIntentService.getDecryptKey(getApplicationContext(), uid);
        Log.i("ButtonPress", "Asking for Decryption key");
    }

    // This instance doesn't get destroyed after handling a broadcast
    private class ResponseReceiver extends BroadcastReceiver {
        SharedPreferences appPref;

        public void onReceive(Context context, Intent intent) {
            if (appPref == null) {
                Log.i("receiver", "appPref not defined");
            }
            appPref = getPreferences(Context.MODE_PRIVATE);
            Log.i("ResponseReceiver", "Received intent " + intent.getAction() + context);
            if (intent != null) {
                if (UNLOCK_APP.equals(intent.getAction())) {
                    alarmBroadcastReceiver.setLockingAlarm(getApplicationContext());
                    SharedPreferences.Editor editor = appPref.edit();
                    editor.putInt("KeyCount", appPref.getInt("KeyCount", 0) + 1);
                    editor.putString("SecretKey", intent.getStringExtra("Secret"));
                    if (editor.commit()) {
                        Log.i(this.getClass().getSimpleName(), "Commit successful");
                    }
                    Log.i(this.getClass().getSimpleName(), "unlocking app on intent, secret is " + appPref.getString("SecretKey", "none"));
                    mainFragment.unlockApp();
                } else if (LOCK_APP.equals(intent.getAction())) {
                    Log.i(this.getClass().getSimpleName(), "locking app on intent");
                    mainFragment.lockApp();
                } else if (DECRYPT_FILE.equals(intent.getAction())) {
                    String key = intent.getStringExtra("decryptionKey");
                    Log.i(this.getClass().getSimpleName(), "Key is " + key);
                    if (key == null) {
                        Toast.makeText(getApplicationContext(), R.string.missing_key, Toast.LENGTH_SHORT).show();
                    } else {
                        mainFragment.decryptFile(getApplicationContext(), key);
                    }
                } else if (NOTIFY_USER.equals(intent.getAction())) {
                    String message = intent.getStringExtra(NOTIFY_USER_MESSAGE);
                    Log.i(this.getClass().getSimpleName(), "Message to user is " + message);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
