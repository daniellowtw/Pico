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

// Might change this to a fragment in the future

public class LogActivity extends Activity {
SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.contains("successfulAttempts")){
            TextView tv = (TextView)findViewById(R.id.successCounts);
            tv.setText("Success count: " + Integer.toString(prefs.getInt("successfulAttempts", -1)));
        }
        if (prefs.contains("failedAttempts")){
            Log.i("Cool", "continas failed stuff");
            TextView tv = (TextView)findViewById(R.id.failedCounts);
            tv.setText("Failure count: " + Integer.toString(prefs.getInt("failedAttempts", -1)));
        }

        try {
            Process process = Runtime.getRuntime().exec("logcat -d *:E");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log=new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
            }
            TextView tv = (TextView)findViewById(R.id.textView1);
            tv.setText(log.toString());
        }
        catch (IOException e) {}
    }
}
