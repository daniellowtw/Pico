package com.example.picoclient.testclient;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;


public class MainActivity extends ActionBarActivity {
    // Actions this activity can do. To be used by intent service
    public static final String UNLOCK_APP = "UNLOCK_APP";
    public static final String LOCK_APP = "LOCK_APP";
    public static final String DECRYPT_FILE = "DECRYPT_FILE";
    public static final String NOTIFY_USER = "NOTIFY_USER";
    public static final String NOTIFY_USER_ALERT = "NOTIFY_USER_ALERT";
    public static final String NOTIFY_USER_MESSAGE = "NOTIFY_USER_MESSAGE";

    /* Putting state variables here so they can be persistent throughout Activity's life */
    MainFragment mainFragment;
    SharedPreferences appPref;
    private AlarmBroadcastReceiver alarmBroadcastReceiver;
    private String uid;

    @Override
    protected void onStart() {
        Log.v(this.getClass().getSimpleName(), "onStart called ");
        super.onStart();
        ((ProgressBar)findViewById(R.id.uploadProgressBar)).setVisibility(View.GONE);
        ((TextView)findViewById(R.id.uploadProgressText)).setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(this.getClass().getSimpleName(), "onCreate called");
        alarmBroadcastReceiver = new AlarmBroadcastReceiver();
        //The following preference can only be accessed by the activity.
        appPref = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
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
        }
        uid = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        ResponseReceiver responseReceiver = new ResponseReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(responseReceiver, new IntentFilter(UNLOCK_APP));
        LocalBroadcastManager.getInstance(this).registerReceiver(responseReceiver, new IntentFilter(LOCK_APP));
        LocalBroadcastManager.getInstance(this).registerReceiver(responseReceiver, new IntentFilter(DECRYPT_FILE));
        LocalBroadcastManager.getInstance(this).registerReceiver(responseReceiver, new IntentFilter(NOTIFY_USER));
        LocalBroadcastManager.getInstance(this).registerReceiver(responseReceiver, new IntentFilter(NOTIFY_USER_ALERT));
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
        } else if (id == R.id.action_log) {
            Intent i = new Intent(getApplicationContext(), LogActivity.class);
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
        Button tempTestButton = (Button) findViewById(R.id.togglePollButton);
        Log.i("Listener", "Toggle Polling");
        if (!appPref.getBoolean("isPolling", false)) {
            // Start polling
            alarmBroadcastReceiver.setPollingAlarm(getApplicationContext());
            tempTestButton.setText("Stop polling");
            appPref.edit().putBoolean("isPolling", true).commit();
        } else {
            // Start polling
            alarmBroadcastReceiver.cancelPollingAlarm(getApplicationContext());
            tempTestButton.setText("Start polling");
            appPref.edit().putBoolean("isPolling", false).commit();
        }
    }

    public void onKeyCount(View v) {
        ServerAPIIntentService.getKeyCount(getApplicationContext(), uid);
        Log.i("ButtonPress", "Asking for key Count");
    }

    public void onLockOrUnlockApp(View v) {
        if (appPref.contains("secretKey")) {
            ServerAPIIntentService.lockApp(this);
        } else {
            ServerAPIIntentService.unlockApp(this, uid);
        }
    }

    public void onDecrypt(View v) {
        if (appPref.contains("secretKey")) {
            mainFragment.decryptFile(this, appPref.getString("secretKey", "null"));
        } else {
            Toast.makeText(this, "No key found", Toast.LENGTH_SHORT).show();
        }
        Log.i("ButtonPress", "Asking for Decryption key");
    }

    public void onRequestRevKey(View v){
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this)
                .setTitle("Enter OTP challenge")
//                .setMessage("Enter OTP challenge")
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable value = input.getText();
                        ServerAPIIntentService.requestRevKey(getApplicationContext(), uid, value.toString());
                        Toast.makeText(getApplicationContext(), "Checking with server", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();
    }

    public void viewAppPref(View v) {
        Toast.makeText(this, appPref.getAll().toString(), Toast.LENGTH_LONG).show();
    }

    public void toggleBGService(View v) {
        Button tempTestButton = (Button) findViewById(R.id.toggleServiceBtn);
        Log.i("Listener", "Toggle Polling");
        if (!appPref.getBoolean("isServiceActive", false)) {
            // Start polling
            startService(new Intent(this, LoggingService.class));
            Log.i("BG Service", "clicked on started bg service");
            tempTestButton.setText("Stop BG service");
            appPref.edit().putBoolean("isServiceActive", true).commit();
        } else {
            // Start polling
            stopService(new Intent(this, LoggingService.class));
            Log.i("BG Service", "clicked on stopped bg service");
            tempTestButton.setText("Start BG service");
            appPref.edit().putBoolean("isServiceActive", false).commit();
        }
    }

    public void uploadDB(View v) {
        UploadDBAsync asyncTask = new UploadDBAsync(getApplicationContext(),
                findViewById(R.id.uploadProgressBar),
                findViewById(R.id.uploadProgressText));
        asyncTask.execute(getDatabasePath(DBHelper.DATABASE_NAME));
    }

    public void exportDB(View v) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            if (sd.canWrite()) {
                String backupDBPath = "picoData.db";
                File currentDB = getDatabasePath(DBHelper.DATABASE_NAME);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
                Toast.makeText(getApplicationContext(), "Saved to sdcard as " + backupDBPath, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void createAlertMessage(String message){
        final TextView tv = new TextView(this);
        tv.setText(message);
        new AlertDialog.Builder(this)
                .setTitle("Response")
                .setView(tv)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();
    }

    // This instance doesn't get destroyed after handling a broadcast
    private class ResponseReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            Log.i("ResponseReceiver", "Received intent " + intent.getAction() + context);

            // We still need a receiver to update app UI when alarm goes off
            if (intent != null) {
                if (UNLOCK_APP.equals(intent.getAction())) {
                    Log.i(this.getClass().getSimpleName(), "unlocking app on intent");
                    if (mainFragment.isVisible()) {
                        mainFragment.unlockApp();
                    }
                } else if (LOCK_APP.equals(intent.getAction())) {
                    Log.i(this.getClass().getSimpleName(), "locking app on intent");
                    if (mainFragment.isVisible()) {
                        mainFragment.lockApp();
                    }
                } else if (NOTIFY_USER.equals(intent.getAction())) {
                    String message = intent.getStringExtra(NOTIFY_USER_MESSAGE);
                    Log.i(this.getClass().getSimpleName(), "Message to user is " + message);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                } else if (NOTIFY_USER_ALERT.equals(intent.getAction())) {
                    String message = intent.getStringExtra(NOTIFY_USER_MESSAGE);
                    Log.i(this.getClass().getSimpleName(), "Message to user is " + message);
                    createAlertMessage(message);
                }
            }
        }
    }
}
