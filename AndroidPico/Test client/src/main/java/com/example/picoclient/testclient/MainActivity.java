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
    public static final String UNLOCK_APP = "test_intent_filter";
    public static final String GET_KEY_COUNT = "GET_KEY_COUNT";
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
        LocalBroadcastManager.getInstance(this).registerReceiver(responseReceiver, new IntentFilter(GET_KEY_COUNT));
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
        mainFragment.decryptFile(getApplicationContext());
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
                else if (GET_KEY_COUNT.equals(intent.getAction())){
                    Toast.makeText(getApplicationContext(),intent.getStringExtra("Count"), Toast.LENGTH_SHORT).show();
                    Log.i(this.getClass().getSimpleName(), "Count is " + intent.getStringExtra("Count"));
                }
            }
        }
    }
}
