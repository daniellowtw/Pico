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
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class LoggingService extends Service {
    public static final String STATE_LOGGING_INTENT = "Stage logging";
    private static final String TAG = "PicoLoggingService";
    BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(android.content.Intent.ACTION_BATTERY_CHANGED)) {
                Log.d(TAG, "ACTION_BATTERY_CHANGED");
                Bundle extras = intent.getExtras();
                Log.d(TAG, "keys: " + extras.keySet());
                ContentValues values = new ContentValues();
                Long now = Long.valueOf(System.currentTimeMillis());
                values.put("sampletime", now);
                values.put("level", extras.getInt("level"));
                values.put("voltage", extras.getInt("voltage"));
                values.put("temperature", extras.getInt("temperature"));
                values.put("plugged", extras.getInt("plugged"));
                insertBatteryData(values);
                // if we're not charging
                if (extras.getInt("plugged") != 2) {
//                    warnUser(extras.getInt("level"));
                }
            }
        }
    };
    BroadcastReceiver stateLoggingReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(STATE_LOGGING_INTENT)) {
                Log.d(TAG, "STATE_LOGGING_INTENT received");
                Bundle extras = intent.getExtras();
                Log.d(TAG, "keys: " + extras.keySet());
                ContentValues values = new ContentValues();
                values.put("poll_start_time", extras.getLong("poll_start_time"));
                values.put("poll_end_time", extras.getLong("poll_end_time"));
                values.put("poll_status", extras.getInt("poll_status"));

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                values.put("connectivity_status", isConnected? 1:0);
                values.put("availability_status", extras.getInt("availability_status"));
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
