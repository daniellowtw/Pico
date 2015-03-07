package com.example.picoclient.testclient;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoggingService extends Service {
    public static final String STATE_LOGGING_INTENT = "Stage logging";
    private static final String TAG = "PicoLoggingService";
    BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(android.content.Intent.ACTION_BATTERY_CHANGED)) {
                Bundle extras = intent.getExtras();
                ContentValues values = new ContentValues();
                Long now = Long.valueOf(System.currentTimeMillis());
                values.put("sampletime", now);
                values.put("level", extras.getInt(BatteryManager.EXTRA_LEVEL));
                values.put("scale", extras.getInt(BatteryManager.EXTRA_SCALE));
                values.put("voltage", extras.getInt(BatteryManager.EXTRA_VOLTAGE));
                values.put("temperature", extras.getInt(BatteryManager.EXTRA_TEMPERATURE));
                values.put("plugged", extras.getInt(BatteryManager.EXTRA_PLUGGED));
                values.put("health", extras.getInt(BatteryManager.EXTRA_HEALTH));
                insertBatteryData(values);
            }
        }
    };
    BroadcastReceiver stateLoggingReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(STATE_LOGGING_INTENT)) {
                Bundle extras = intent.getExtras();
                ContentValues values = new ContentValues();
                values.put("poll_start_time", extras.getLong("poll_start_time"));
                values.put("poll_end_time", extras.getLong("poll_end_time"));
                values.put("comments", extras.getString("comments"));
//                Whether the poll succeeded or not
                values.put("poll_status", extras.getInt("poll_status"));
//                Whether we have the secret share
                values.put("availability_status", extras.getInt("availability_status"));
                values.put("traffic_received", extras.getLong("traffic_received"));
                values.put("traffic_transmitted", extras.getLong("traffic_transmitted"));

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//                Might be null if we're not connected
                if (activeNetwork!=null){
                    values.put("network_connectivity", activeNetwork.isConnected()?1:0);
                    values.put("network_availability", activeNetwork.isAvailable()?1:0);
                    values.put("network_type", activeNetwork.getTypeName());
                    values.put("network_subtype", activeNetwork.getSubtypeName());
                    values.put("network_extra_info", activeNetwork.getExtraInfo());
                }
                else{
                    // No network
                    values.put("network_connectivity", 0);
                    values.put("network_availability", 0);
                }
                insertStateData(values);
            }
        }
    };
    ConnectivityManager cm = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("BG SERVICE", "Started service");
        cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        IntentFilter batteryFilter = new IntentFilter();
        batteryFilter.addAction(android.content.Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mIntentReceiver, batteryFilter);

        IntentFilter stateLoggingFilter = new IntentFilter();
        stateLoggingFilter.addAction(STATE_LOGGING_INTENT);
        registerReceiver(stateLoggingReceiver, stateLoggingFilter);
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mIntentReceiver);
        unregisterReceiver(stateLoggingReceiver);
    }

    public boolean insertBatteryData(ContentValues values) {
        Log.d(TAG, "insertBatteryData: " + values);

        DBHelper mOpenHelper = null;
        SQLiteDatabase db = null;
        boolean rc = true;

        try {
            mOpenHelper = new DBHelper(this);
            db = mOpenHelper.getReadableDatabase();
            Long rowid = db.insert(DBHelper.LEVELS_TABLE, "none", values);
            if (rowid < 0) {
                Log.e(TAG, "database insert failed: " + rowid);
                rc = false;
            } else {
                Log.d(TAG, "sample collected, rowid=" + rowid);
            }
        } catch (Exception e) {
            Log.e(TAG, "database exception");
            rc = false;
        }
        if (db != null) db.close();
        if (mOpenHelper != null) mOpenHelper.close();
        return rc;
    }

    public boolean insertStateData(ContentValues values) {
        Log.d(TAG, "insert state data: " + values);

        DBHelper mOpenHelper = null;
        SQLiteDatabase db = null;
        boolean rc = true;

        try {
            mOpenHelper = new DBHelper(this);
            db = mOpenHelper.getReadableDatabase();
            Long rowid = db.insert(DBHelper.STATES_TABLE, "none", values);
            if (rowid < 0) {
                Log.e(TAG, "database insert failed: " + rowid);
                rc = false;
            } else {
                Log.d(TAG, "sample collected, rowid=" + rowid);
            }
        } catch (Exception e) {
            Log.e(TAG, "database exception");
            rc = false;
        }
        if (db != null) db.close();
        if (mOpenHelper != null) mOpenHelper.close();
        return rc;
    }
}
