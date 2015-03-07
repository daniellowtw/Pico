package com.example.picoclient.testclient;

/**
 * Created by Daniel on 12/02/2015.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DBHelper extends SQLiteOpenHelper {
    static final String TAG = "DBHelper";
    static final String DATABASE_NAME = "datalog.db";
    static final int DATABASE_VERSION = 1;
    static final String LEVELS_TABLE = "power_levels";
    static final String STATES_TABLE = "states";

    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "DatabaseHelper onCreate called");
        db.execSQL("CREATE TABLE " + LEVELS_TABLE + " ("
                + "sampletime INTEGER PRIMARY KEY,"
                + "level INTEGER,"
                + "scale INTEGER,"
                + "voltage INTEGER,"
                + "temperature INTEGER,"
                + "plugged INTEGER,"
                + "health INTEGER"
                + ");");
        db.execSQL("CREATE TABLE " + STATES_TABLE + " ("
                + "poll_start_time INTEGER,"
                + "poll_end_time INTEGER,"
                + "poll_status INTEGER,"
                + "traffic_received INTEGER,"
                + "traffic_transmitted INTEGER,"
                + "availability_status INTEGER,"
                + "network_connectivity INTEGER,"
                + "network_availability INTEGER,"
                + "network_type TEXT,"
                + "network_subtype TEXT,"
                + "network_extra_info TEXT,"
                + "comments TEXT DEFAULT ''"
                + ");");
    }

    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "database upgrade requested.  ignored.");
    }
}