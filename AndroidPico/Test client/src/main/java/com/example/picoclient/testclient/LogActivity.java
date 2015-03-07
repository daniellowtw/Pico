package com.example.picoclient.testclient;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

// Might change this to a fragment in the future

public class LogActivity extends Activity {
SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Log", "onResume");
        try {
            StringBuilder log=new StringBuilder();
            String line = "";
            Map<String,?> allPref = prefs.getAll();
            for (Map.Entry<String,?> e :allPref.entrySet()){
                log.append(e.getKey());
                log.append(": ");
                log.append(e.getValue().toString());
                log.append("\n");
            }
            TextView tv = (TextView)findViewById(R.id.textView1);
            tv.setText(log.toString());
        }
        catch (Exception e) {
            Log.e("Log",e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}
