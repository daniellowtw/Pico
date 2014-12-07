package com.example.picoclient.testclient;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import java.util.Timer;

import javax.crypto.SecretKey;

public class MainActivity extends ActionBarActivity {

    /* Putting state variables here so they can be persistent throughout Activity's life */
    static protected Boolean unlockedState = false;
    static protected Boolean pollingState = false;
    static long endTime = 0;
    static long pollEndTime = 0;

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
    public static class PlaceholderFragment extends Fragment implements AsyncResponse<String> {

        public AsyncResponse<String> thisFragment = this;

        public PlaceholderFragment() {
        }

        private void saveFile(Context ctx) {
            // Create a new file
            try {
                // catches IOException below
                Log.e("action", "saving file");
                final String TESTSTRING = new String("Hello Android");

                // Encrypt string
                Encryptor encryptor = new Encryptor();
                // TODO: Need to delete this from memory because it contains key
                String ciphertext = encryptor.encrypt(TESTSTRING, "1234");
                final AsyncTaskCreateKey asyncTaskCreateKey = new AsyncTaskCreateKey();
                asyncTaskCreateKey.delegate = thisFragment;
                asyncTaskCreateKey.execute(new String(Base64.encodeToString(encryptor.key.getEncoded(), Base64.DEFAULT)));
//                Log.i("action", "String key on server" + new String(encryptor.key.getEncoded()));

                FileOutputStream fOut = ctx.openFileOutput("samplefile.txt", ctx.MODE_PRIVATE);
                OutputStreamWriter osw = new OutputStreamWriter(fOut);
                osw.write(ciphertext);
                Log.e("data", ciphertext);
                osw.flush();
                osw.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            // End create a new file
        }

        private String readFile(Context ctx) {
            try {
                FileInputStream fIn = ctx.openFileInput("samplefile.txt");
                InputStreamReader isr = new InputStreamReader(fIn);
                Scanner s = new Scanner(isr);
                String readString = s.nextLine();
                Log.i("File Reading stuff", "success = " + readString);
                return readString;
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return "Error getting string";
            }
        }

        private void lockApp(TextView textViewToChange) {
            Log.i("action", "unlock App");
            unlockedState = false;
            textViewToChange.setText(R.string.app_status_locked);
            textViewToChange.setBackgroundColor(Color.RED);
        }

        private void unlockApp(TextView textViewToChange) {
            Log.i("action", "unlock App");
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
                    long millis = endTime - System.currentTimeMillis();
                    // add 1 second more so that we lock when it shows 0.
                    long displayTime = millis + 1000;
                    int seconds = (int) (displayTime / 1000);
                    int minutes = seconds / 60;
                    seconds = seconds % 60;
                    timerText.setText(String.format("Time left before locking: %d:%02d", minutes, seconds));
                    // if there is still time left before locking
                    if (millis > 0) {
                        timerHandler.postDelayed(this, 500);
                    }
                    // change to lock state and stop changing timer
                    else {
                        timerText.setText("");
                        lockApp(statusTextView);
                    }
                }
            };

            final Button createFileButton = (Button) rootView.findViewById(R.id.createFileButton);
            final Button readFileButton = (Button) rootView.findViewById(R.id.readFileButton);
            final Button connectToServerButton = (Button) rootView.findViewById(R.id.connectToServerButton);
            final Button changeStatusButton = (Button) rootView.findViewById(R.id.changeStatusButton);
            final Button decryptFileButton = (Button) rootView.findViewById(R.id.decryptFileButton);
            final TextView lastLog = (TextView) rootView.findViewById(R.id.lastLog);
            final Handler pollTimerHandler = new Handler();
            final Runnable pollTimerRunnable = new Runnable() {
                @Override
                public void run() {
                    // TODO Is this an anti-pattern?
                    if (!pollingState) return;
                    long millis = pollEndTime - System.currentTimeMillis();
                    if (millis > 0) {
                        // Check every one second? Do I want this?
                        timerHandler.postDelayed(this, 1000);
                    }
                    // change to lock state and stop changing timer
                    else {
                        String timestamp = DateFormat.getDateTimeInstance().format(new Date());
                        Log.i("Poll", "called at " + timestamp);
                        lastLog.append("Called at " + timestamp + "\n");
                        pollEndTime += 4000;
                        timerHandler.postDelayed(this, 4000);
                    }
                }
            };


            lockApp(statusTextView);

            createFileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveFile(getActivity().getApplicationContext());
                    // Toast message will be created by processFinish function
                    Log.i("ButtonPress", "Create share button pressed");
                }
            });

            readFileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String fileSecret = readFile(getActivity().getApplicationContext());
                    Toast.makeText(getActivity(), "File content is: " + fileSecret, Toast.LENGTH_SHORT).show();
                    Log.i("ButtonPress", "Read file button pressed");
                }
            });

            connectToServerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final NetworkAsyncTask asyncTask = new NetworkAsyncTask();
                    asyncTask.delegate = thisFragment;
                    asyncTask.execute();
                    Log.i("ButtonPress", "Create to server button pressed");
                }
            });

            changeStatusButton.setOnClickListener(new View.OnClickListener() {
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

            decryptFileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AsyncTaskRetrieveKey asyncTask = new AsyncTaskRetrieveKey();
                    asyncTask.delegate = new AsyncResponse<String>() {
                        @Override
                        public void processFinish(String output) {
                            if (output == null) {
                                Toast.makeText(getActivity(), "Key missing", Toast.LENGTH_SHORT).show();
                                Log.i("Decrypt", "missing key");
                                return;
                            }
                            Encryptor e = new Encryptor();
                            String fileSecret = readFile(getActivity().getApplicationContext());
                            String temp = e.decrypt(fileSecret, Base64.decode(output, Base64.DEFAULT));
                            Toast.makeText(getActivity(), "File content is: " + temp, Toast.LENGTH_SHORT).show();
                            Log.i("Decrypt", "file is " + temp);
                        }
                    };
                    asyncTask.execute();
                    Log.i("ButtonPress", "Decrypt secret share button pressed");
                }
            });

            /*
            ** Temp test button is a placeholder for creating new features */
            final Button tempTestButton = (Button) rootView.findViewById(R.id.tempButton);
            tempTestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("ButtonPress", "temp button pressed");
                    if (pollingState){
                        pollTimerHandler.removeCallbacks(pollTimerRunnable);
                        tempTestButton.setText("Start polling");
                        pollingState = false;
                    }
                    else {
                        tempTestButton.setText("Stop polling");
                        pollTimerHandler.removeCallbacks(pollTimerRunnable);
                        pollEndTime = System.currentTimeMillis() + 4000;
                        // start the handler
                        pollTimerHandler.postDelayed(pollTimerRunnable, 0);
                        Log.i("Poll", "handler started");
                        pollingState = true;
                    }
                }
            });

            return rootView;
        }

        @Override
        public void processFinish(String output) {
            Log.i("Async task", "delegate called");
            Log.i("Async task", output);
            Toast.makeText(getActivity(), output, Toast.LENGTH_SHORT).show();
        }
    }
}
