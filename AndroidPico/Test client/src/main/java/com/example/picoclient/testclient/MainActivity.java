package com.example.picoclient.testclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
    /* Putting state variables here so they can be persistent throughout Activity's life */
    MainFragment mainFragment;

    // Actions this activity can do. To be used by intent service
    public static final String UNLOCK_APP = "UNLOCK_APP";
    public static final String DECRYPT_FILE = "DECRYPT_FILE";
    public static final String NOTIFY_USER = "NOTIFY_USER";
    public static final String NOTIFY_USER_MESSAGE = "NOTIFY_USER_MESSAGE";
    private String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mainFragment = new MainFragment();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mainFragment)
                    .commit();
        }
        uid = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        ResponseReceiver responseReceiver = new ResponseReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(responseReceiver, new IntentFilter(UNLOCK_APP));
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

    public void onCreateFile(View v){
        mainFragment.saveFile(getApplicationContext());
    }
    public void onShowFile(View v){
        String fileSecret = mainFragment.readFile(getApplicationContext());
        Toast.makeText(this, "File content is: " + fileSecret, Toast.LENGTH_SHORT).show();
    }
    public void onTogglePolling(View v){
        if (mainFragment != null){
            mainFragment.togglePolling(v);
        }
    }
    public void onConnectServer(View v){
        PollIntentService.getKeyCount(getApplicationContext(), uid);
        Log.i("ButtonPress", "Asking for key Count");
    }
    public void onChangeStatus(View v){
        mainFragment.readFile(getApplicationContext());
    }
    public void onDecrypt(View v){
        PollIntentService.getDecryptKey(getApplicationContext(), uid);
        Log.i("ButtonPress", "Asking for Decryption key");
    }

    private class ResponseReceiver extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent) {
            Log.i("ResponseReceiver", "Received intent " + intent.getAction());
            if (intent !=null){
                if (UNLOCK_APP.equals(intent.getAction())){
                    mainFragment.unlockApp();
                    Log.i(this.getClass().getSimpleName(), "unlocking app on intent");
                }
                else if (DECRYPT_FILE.equals(intent.getAction())){
                    String key = intent.getStringExtra("decryptionKey");
                    Log.i(this.getClass().getSimpleName(), "Key is " + key);
                    if (key == null){
                        Toast.makeText(getApplicationContext(), R.string.missing_key, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        mainFragment.decryptFile(getApplicationContext(), key);
                    }
                }
                else if (NOTIFY_USER.equals(intent.getAction())){
                    String message = intent.getStringExtra(NOTIFY_USER_MESSAGE);
                    Log.i(this.getClass().getSimpleName(), "Message to user is " + message);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
