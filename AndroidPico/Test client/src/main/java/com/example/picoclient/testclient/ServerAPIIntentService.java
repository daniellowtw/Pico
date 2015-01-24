package com.example.picoclient.testclient;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServerAPIIntentService extends IntentService {
    // The following are possible actions
    public static final String START_POLLING = "START_POLLING";
    public static final String UNLOCK_APP = "UNLOCK_APP";
    public static final String LOCK_APP = "LOCK_APP";
    public static final String GET_COUNT = "GET_COUNT";
    public static final String SET_DECRYPTION_KEY = "SET_DECRYPTION_KEY";
    public static final String UID = "UID";
    public static final String KEY = "KEY";
    private final String TAG = this.getClass().getSimpleName();
    int counter = 0;
    NotificationManager nm;
    SharedPreferences prefs;
    AlarmBroadcastReceiver alarmBroadcastReceiver;
    private int NotificationID = R.string.notification_id;

    public ServerAPIIntentService() {
        super("PollIntentService");
    }

    public static void lockApp(Context context) {
        Intent intent = new Intent(context, ServerAPIIntentService.class);
        intent.setAction(LOCK_APP);
        context.startService(intent);
    }

    public static void startPolling(Context context, String uid) {
        Intent intent = new Intent(context, ServerAPIIntentService.class);
        intent.setAction(START_POLLING);
        intent.putExtra(UID, uid);
        context.startService(intent);
    }

    public static void getKeyCount(Context context, String uid) {
        Intent intent = new Intent(context, ServerAPIIntentService.class);
        intent.setAction(GET_COUNT);
        intent.putExtra(UID, uid);
        context.startService(intent);
    }

    public static void unlockApp(Context context, String uid) {
        Intent intent = new Intent(context, ServerAPIIntentService.class);
        intent.setAction(UNLOCK_APP);
        intent.putExtra(UID, uid);
        context.startService(intent);
    }

    public static void saveKey(Context context, String uid, String key) {
        Intent intent = new Intent(context, ServerAPIIntentService.class);
        intent.setAction(SET_DECRYPTION_KEY);
        intent.putExtra(UID, uid);
        intent.putExtra(KEY, key);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        alarmBroadcastReceiver = new AlarmBroadcastReceiver();
        Log.v(TAG, "onCreate called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy called");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "intent received");
        if (intent != null) {
            final String action = intent.getAction();
            Log.i(TAG, "intent action is " + action);
            try {

                if (START_POLLING.equals(action)) {
                    final String uid = intent.getStringExtra(UID);
                    handleUnlockApp(uid);
                } else if (GET_COUNT.equals(action)) {
                    final String uid = intent.getStringExtra(UID);
                    handleGetKeyCount(uid);
                } else if (UNLOCK_APP.equals(action)) {
                    final String uid = intent.getStringExtra(UID);
                    handleUnlockApp(uid);
                } else if (SET_DECRYPTION_KEY.equals(action)) {
                    final String uid = intent.getStringExtra(UID);
                    final String key = intent.getStringExtra(KEY);
                    handleSetDecryptionKey(uid, key);
                } else if (LOCK_APP.equals(action)) {
                    handleLockApp();
                }
            } catch (IOException e) {
                incrementFailedCount();
                showNotification("Error", "Action is " + action + " Message is " + e.getLocalizedMessage(), false);
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleLockApp() {
        // Delete key from shared pref
        prefs.edit().remove("secretKey").commit();
        // tell mainactivity to update UI
        Intent localIntent = new Intent(MainActivity.LOCK_APP);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

    }

    private void handleGetKeyCount(String uid) throws IOException {
        String messageToSend = "key]" + uid;
        String result = sendStringToServer(messageToSend);
        showNotification("KeyCount", result, true);
        Intent localIntent =
                new Intent(MainActivity.NOTIFY_USER)
                        .putExtra(MainActivity.NOTIFY_USER_MESSAGE, result);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void handleSetDecryptionKey(String uid, String key) throws IOException {
        String messageToSend = "add]" + uid + "]" + key;
        String result = sendStringToServer(messageToSend);
        if (result.isEmpty()) {
            Log.i("empty", "empty");
        } else {
            Intent localIntent =
                    new Intent(MainActivity.NOTIFY_USER)
                            .putExtra(MainActivity.NOTIFY_USER_MESSAGE, result);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }

    private void handleUnlockApp(String uid) throws IOException {
        String messageToSend = "get]" + uid;
        String key = sendStringToServer(messageToSend);
        if (key.isEmpty()) {
            showNotification("Error: revoked/missing key", "Key is not found on server", false);
        } else {
            incrementSuccessCount();
            prefs.edit().putString("secretKey", key).commit();
            // tell mainactivity to update UI
            Intent localIntent = new Intent(MainActivity.UNLOCK_APP);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            // every time unlock is called, we set a timer to lock it (ie remove secret)
            alarmBroadcastReceiver.setLockingAlarm(this);
        }
    }

    // The following are helper functions
    private String sendStringToServer(String s) throws IOException {
        try {
            String serverAddr = prefs.getString("pref_sync_addr", "dlow.me");
            int serverPort = Integer.parseInt(prefs.getString("pref_sync_port", "8001"));
            InetSocketAddress addr = new InetSocketAddress(serverAddr , serverPort);
            Socket ss = NaiveSocketFactory.getSocketFactory().createSocket();
            ss.connect(addr, 1000);
            BufferedReader br = new BufferedReader(new InputStreamReader(ss.getInputStream()));
            PrintWriter out = new PrintWriter(ss.getOutputStream());
            // welcome message ignore
            br.readLine();
            out.print(s);
            out.flush();
            String res = br.readLine();
            Log.i(TAG, "received string is " + res);
            return res;
        } catch (UnknownHostException e) {
            throw e;
        } catch (IOException e) {
            Log.e("Exception", e.getMessage());
            throw e;
        }
    }

    private void showNotification(String title, String text, boolean stacked) {
        Log.i("Notification", "notification: " + text);
        // Set the icon, scrolling text and timestamp
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentText(text)
                .setContentTitle(title)
                .build();
        // Send the notification.
        if (stacked) {
            nm.notify(counter++, notification);
        } else {
            nm.notify(NotificationID, notification);
        }
    }

    private void incrementFailedCount() {
        // Increment failed count in app
        if (prefs.contains("failedAttempts")) {
            prefs.edit().putInt("failedAttempts", prefs.getInt("failedAttempts", 0) + 1).commit();
        } else {
            prefs.edit().putInt("failedAttempts", 1).commit();
        }
    }

    private void incrementSuccessCount() {
        // Increment successful count in app
        if (prefs.contains("successfulAttempts")) {
            prefs.edit().putInt("successfulAttempts", prefs.getInt("successfulAttempts", 0) + 1).commit();
        } else {
            prefs.edit().putInt("successfulAttempts", 1).commit();
        }
    }
}
