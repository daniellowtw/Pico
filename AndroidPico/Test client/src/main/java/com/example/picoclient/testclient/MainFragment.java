package com.example.picoclient.testclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
    Button createFileButton = null;
    Button readFileButton = null;
//    Button getKeyCountButton = null;
    Button lockOrUnlockButton = null;
    Button decryptFileButton = null;
    Button togglePollButton = null;
    TextView lastLog = null;
    TextView statusTextView;
    TextView secretKeyTV;
    SharedPreferences prefs;
    private String uid;

    public MainFragment() {
    }

    private void showSecretTV() {
        if (secretKeyTV != null) {
            secretKeyTV.setText("Secret key: " + prefs.getString("secretKey", "No Secret found"));
        } else {
            Log.e("Can't find view", "secret key text view");
        }
    }

    @Override
    public void onStart() {
        // onCreateView gets called before this/
        super.onStart();
        Log.v(this.getClass().getSimpleName(), "onStart");
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

    void saveFile(Context ctx) {
        // Create a new file
        try {
            // catches IOException below
            Log.v("action", "saving file");
            final String TESTSTRING = new String("Hello Android");
            // Encrypt string
            Encryptor encryptor = new Encryptor();
            // TODO: Need to delete this from memory because it contains key
            // Does storing it as a char[] change anything?
            String ciphertext = encryptor.encryptWithoutPassword(TESTSTRING);
            ServerAPIIntentService.saveKey(getActivity().getApplicationContext(), uid, new String(Base64.encodeToString(encryptor.key.getEncoded(), Base64.DEFAULT)));
            FileOutputStream fOut = ctx.openFileOutput("samplefile.txt", ctx.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            osw.write(ciphertext);
            Log.v("data", ciphertext);
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
        statusTextView.setText(R.string.app_status_locked);
        statusTextView.setBackgroundColor(Color.RED);
        lockOrUnlockButton.setText("Unlock app");
        showSecretTV();
    }

    public void unlockApp() {
        Log.i("action", "unlock App");
        statusTextView.setText(R.string.app_status_unlocked);
        statusTextView.setBackgroundColor(Color.GREEN);
        lockOrUnlockButton.setText("Lock app");
        showSecretTV();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        uid = Settings.Secure.getString(getActivity().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        statusTextView = (TextView) rootView.findViewById(R.id.textStatus);
        createFileButton = (Button) rootView.findViewById(R.id.createFileButton);
        readFileButton = (Button) rootView.findViewById(R.id.readFileButton);
//        getKeyCountButton = (Button) rootView.findViewById(R.id.getKeyCountButton);
        lockOrUnlockButton = (Button) rootView.findViewById(R.id.lockOrUnlockButton);
        decryptFileButton = (Button) rootView.findViewById(R.id.decryptFileButton);
        togglePollButton = (Button) rootView.findViewById(R.id.togglePollButton);
        lastLog = (TextView) rootView.findViewById(R.id.lastLog);
        secretKeyTV = (TextView) rootView.findViewById(R.id.secret_key_text_view);
        Log.v(this.getClass().getSimpleName(), "onCreateView called");
        if (prefs == null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        }
        int toggleButtonText = prefs.getBoolean("isPolling", false) ? R.string.stop_polling : R.string.start_polling;
        togglePollButton.setText(toggleButtonText);

        if (prefs.contains("secretKey")) {
            lockOrUnlockButton.setText(R.string.lock_app);
            unlockApp();
        } else {
            lockOrUnlockButton.setText(R.string.unlock_app);
            lockApp();
        }
        TextView tv = (TextView) rootView.findViewById(R.id.versionTextView);
        tv.setText("v" + BuildConfig.VERSION_NAME);
        return rootView;
    }
}
