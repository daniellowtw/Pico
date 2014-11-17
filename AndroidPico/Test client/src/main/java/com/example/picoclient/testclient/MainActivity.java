package com.example.picoclient.testclient;

import android.graphics.Color;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {

    /* Putting state variables here so they can be persistent throughout Activity's life */
    static protected Boolean unlockedState = false;
    static Timer timer = new Timer();
    static long endTime = 0;

    class changeToLockTask extends TimerTask{

        @Override
        public void run() {

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Code to initialise the fragment should be placed here. This is where we put our button click handlers
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        private void lockApp(TextView textViewToChange){
            Log.e("action", "unlock App");
            unlockedState = false;
            textViewToChange.setText(R.string.app_status_locked);
            textViewToChange.setBackgroundColor(Color.RED);
        }


        private void unlockApp(TextView textViewToChange){
            Log.e("action", "unlock App");
            unlockedState = true;
            textViewToChange.setText(R.string.app_status_unlocked);
            textViewToChange.setBackgroundColor(Color.GREEN);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            final TextView statusTextView = (TextView) rootView.findViewById(R.id.textStatus);
            final TextView timerText = (TextView) rootView.findViewById(R.id.text_time_left);
            final Handler timerHandler = new Handler();
            final Runnable timerRunnable = new Runnable() {
                @Override
                public void run() {
                    long millis = endTime -  System.currentTimeMillis();
                    // add 1 second more so that we lock when it shows 0.
                    long displayTime = millis + 1000;
                    int seconds = (int) (displayTime / 1000);
                    int minutes = seconds / 60;
                    seconds = seconds % 60;
                    timerText.setText(String.format("Time left before locking: %d:%02d", minutes, seconds));
                    // if there is still time left before locking
                    if (millis > 0 ){
                        timerHandler.postDelayed(this, 500);
                    }
                    // change to lock state and stop changing timer
                    else{
                        lockApp(statusTextView);
                    }
                }
            };

            final Button createShareButton = (Button) rootView.findViewById(R.id.createShareButton);
            final Button connectToServerButton = (Button) rootView.findViewById(R.id.connectToServerButton);
            createShareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity(), "This will create a new key pair", Toast.LENGTH_SHORT).show();
                    Log.e("ButtonPress", "Create share button pressed");
                }
            });
            connectToServerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity(), "This will connect to the server", Toast.LENGTH_SHORT).show();
                    Log.e("ButtonPress", "Create to server button pressed");
                }
            });

            /*
            ** Temp test button is a placeholder for creating new features */
            Button tempTestButton = (Button) rootView.findViewById(R.id.tempButton);
            tempTestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("Info state", unlockedState.toString());
                    // we are changing from unlocked to locked
                    if (unlockedState == true) {
                        // remove task from handler
                        timerHandler.removeCallbacks(timerRunnable);
                        lockApp(statusTextView);
                    } else {
                        // we are changing from locked to unlocked so add timer
                        // add 10 seconds to the endTime
                        endTime = System.currentTimeMillis() + 4000;
                        // start the handler
                        timerHandler.postDelayed(timerRunnable, 0);
                        unlockApp(statusTextView);
                    }
                }
            });

            return rootView;
        }
    }
}
