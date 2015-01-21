package com.example.picoclient.testclient;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;

public class MainFragment extends Fragment {
    // TODO: Put this in preferences
    static protected Boolean unlockedState = false;
    static protected Boolean isPolling = false;
    Button createFileButton = null;
    Button readFileButton = null;
    Button connectToServerButton = null;
    Button changeStatusButton = null;
    Button decryptFileButton = null;
    TextView lastLog = null;
    TextView statusTextView;
    TextView secretKeyTV;
    private AlarmBroadcastReceiver alarmBroadcastReceiver;
    private String uid;

    public MainFragment() {
    }

    private void showSecretTV() {
        if (secretKeyTV != null) {
            secretKeyTV.setText("Secret key: " + getActivity().getPreferences(Context.MODE_PRIVATE).getString("SecretKey", "--"));
        } else {
            Log.e("Can't find view", "secret key text view");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(this.getClass().getSimpleName(), "onStart");
        showSecretTV();
    }

    // This will be called by MainActivity Receiver when the decrypt file intent is sent by the service
    void decryptFile(Context ctx1, String key) {
        final Context ctx = ctx1;
        Encryptor e = new Encryptor();
        try {
            String fileSecret = readFile(ctx.getApplicationContext());
            String temp = e.decrypt(fileSecret, Base64.decode(key, Base64.DEFAULT));
            Toast.makeText(ctx, "File content is: " + temp, Toast.LENGTH_SHORT).show();
            Log.i("Decrypt", "file is " + temp);
        } catch (InvalidKeyException e1) {
            Log.i("Decrypt", "Invalid key Exception: wrong key");
            Toast.makeText(ctx, "Wrong key: " + key, Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
        } catch (GeneralSecurityException e1) {
            if (e1 instanceof BadPaddingException) {
                Log.i("Decrypt", "BadPaddingException" + e1.getLocalizedMessage());
                Toast.makeText(ctx, "BadPaddingException", Toast.LENGTH_SHORT).show();
            } else if (e1 instanceof InvalidKeyException) {
                Log.i("Decrypt", "BadPaddingException" + e1.getLocalizedMessage());
                Toast.makeText(ctx, "BadPaddingException, decryption failed", Toast.LENGTH_SHORT).show();
            } else {
                Log.i("Decrypt", "General Security Exception" + e1.getLocalizedMessage());
                Toast.makeText(ctx, "General Security Exception", Toast.LENGTH_SHORT).show();
            }
        } catch (UnsupportedEncodingException e1) {
            Log.i("Decrypt", "UnsupportedEncodingException");
            e1.printStackTrace();
        } catch (FileNotFoundException e1) {
            Log.i("Decrypt", "File no available");
            Toast.makeText(ctx, "File not found", Toast.LENGTH_SHORT).show();
        }
        Log.i("ButtonPress", "Decrypt secret share button pressed");
    }

    public void togglePolling(View v) {
        Button tempTestButton = (Button) v.findViewById(R.id.tempButton);
        Log.i("Listener", "Toggle Polling");
        if (!isPolling) {
            alarmBroadcastReceiver.setPollingAlarm(getActivity().getApplicationContext());
            tempTestButton.setText("Stop polling");
            isPolling = true;
        } else {
            alarmBroadcastReceiver.cancelPollingAlarm(getActivity().getApplicationContext());
            tempTestButton.setText("Start polling");
            isPolling = false;
        }
    }

    void saveFile(Context ctx) {
        // Create a new file
        try {
            // catches IOException below
            Log.e("action", "saving file");
            final String TESTSTRING = new String("Hello Android");
            // Encrypt string
            Encryptor encryptor = new Encryptor();
            // TODO: Need to delete this from memory because it contains key
            String ciphertext = encryptor.encryptWithoutPassword(TESTSTRING);
            ServerAPIIntentService.saveKey(getActivity().getApplicationContext(), uid, new String(Base64.encodeToString(encryptor.key.getEncoded(), Base64.DEFAULT)));
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

    String readFile(Context ctx) throws FileNotFoundException {
        FileInputStream fIn = ctx.openFileInput("samplefile.txt");
        InputStreamReader isr = new InputStreamReader(fIn);
        Scanner s = new Scanner(isr);
        String readString = s.nextLine();
        Log.i("File Reading stuff", "success = " + readString);
        return readString;
    }

    public void lockApp() {
        Log.i("action", "lock App");
        getActivity().getPreferences(Context.MODE_PRIVATE).edit().putString("SecretKey", "-").commit();
        unlockedState = false;
        statusTextView.setText(R.string.app_status_locked);
        statusTextView.setBackgroundColor(Color.RED);
        showSecretTV();
    }

    public void unlockApp() {
        Log.i("action", "unlock App");
        unlockedState = true;
        statusTextView.setText(R.string.app_status_unlocked);
        statusTextView.setBackgroundColor(Color.GREEN);
        showSecretTV();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        alarmBroadcastReceiver = new AlarmBroadcastReceiver();
        uid = Settings.Secure.getString(getActivity().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        statusTextView = (TextView) rootView.findViewById(R.id.textStatus);
        createFileButton = (Button) rootView.findViewById(R.id.createFileButton);
        readFileButton = (Button) rootView.findViewById(R.id.readFileButton);
        connectToServerButton = (Button) rootView.findViewById(R.id.connectToServerButton);
        changeStatusButton = (Button) rootView.findViewById(R.id.changeStatusButton);
        decryptFileButton = (Button) rootView.findViewById(R.id.decryptFileButton);
        lastLog = (TextView) rootView.findViewById(R.id.lastLog);
        secretKeyTV = (TextView) rootView.findViewById(R.id.secret_key_text_view);
        lockApp();
        TextView tv = (TextView) rootView.findViewById(R.id.versionTextView);
        tv.setText("v" + BuildConfig.VERSION_NAME);
        return rootView;
    }
}
