package com.example.picoclient.testclient;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
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

public class PollIntentService extends IntentService {
    // The following are possible actions
    public static final String START_POLLING = "START_POLLING";
    public static final String GET_COUNT = "GET_COUNT";
    public static final String GET_DECRYPTION_KEY = "GET_DECRYPTION_KEY";
    public static final String SET_DECRYPTION_KEY = "SET_DECRYPTION_KEY";
    public static final String UID = "UID";
    public static final String KEY = "KEY";
    private final String TAG = this.getClass().getSimpleName();
    int counter = 0;
    NotificationManager nm;
    private int NotificationID = R.string.notification_id;

    public PollIntentService() {
        super("PollIntentService");
    }

    public static void startPolling(Context context, String uid) {
        Intent intent = new Intent(context, PollIntentService.class);
        intent.setAction(START_POLLING);
        intent.putExtra(UID, uid);
        context.startService(intent);
    }

    public static void getKeyCount(Context context, String uid) {
        Intent intent = new Intent(context, PollIntentService.class);
        intent.setAction(GET_COUNT);
        intent.putExtra(UID, uid);
        context.startService(intent);
    }

    public static void getDecryptKey(Context context, String uid) {
        Intent intent = new Intent(context, PollIntentService.class);
        intent.setAction(GET_DECRYPTION_KEY);
        intent.putExtra(UID, uid);
        context.startService(intent);
    }

    public static void saveKey(Context context, String uid, String key) {
        Intent intent = new Intent(context, PollIntentService.class);
        intent.setAction(SET_DECRYPTION_KEY);
        intent.putExtra(UID, uid);
        intent.putExtra(KEY, key);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Log.i(TAG, "onCreate called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy called");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "intent received");
        if (intent != null) {
            final String action = intent.getAction();
            Log.i(TAG, "intent action is " + action);
            if (START_POLLING.equals(action)) {
                final String uid = intent.getStringExtra(UID);
                handleActionPollServer(uid);
            } else if (GET_COUNT.equals(action)) {
                final String uid = intent.getStringExtra(UID);
                handleGetKeyCount(uid);
            } else if (GET_DECRYPTION_KEY.equals(action)) {
                final String uid = intent.getStringExtra(UID);
                handleGetDecryptionKey(uid);
            } else if (SET_DECRYPTION_KEY.equals(action)) {
                final String uid = intent.getStringExtra(UID);
                final String key = intent.getStringExtra(KEY);
                handleSetDecryptionKey(uid, key);
            }
        }
    }

    private void handleGetDecryptionKey(String uid) {
        String messageToSend = "get]" + uid;
        String result = sendStringToServer(messageToSend);
        Intent localIntent =
                new Intent(MainActivity.DECRYPT_FILE).putExtra("decryptionKey", result);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

    }

    private void handleGetKeyCount(String uid) {
        String messageToSend = "key]" + uid;
        String result = sendStringToServer(messageToSend);
        showNotification("KeyCount", result, true);
        Intent localIntent =
                new Intent(MainActivity.NOTIFY_USER).putExtra(MainActivity.NOTIFY_USER_MESSAGE, result);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void handleSetDecryptionKey(String uid, String key) {
        String messageToSend = "add]" + uid + "]" + key;
        String result = sendStringToServer(messageToSend);
        Intent localIntent =
                new Intent(MainActivity.NOTIFY_USER).putExtra(MainActivity.NOTIFY_USER_MESSAGE, result);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void handleActionPollServer(String uid) {
        String messageToSend = "get]" + uid;
        String result = sendStringToServer(messageToSend);
        Intent localIntent =
                new Intent(MainActivity.UNLOCK_APP);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    // The following are helper functions
    private String sendStringToServer(String s) {
        try {
            InetSocketAddress addr = new InetSocketAddress(PicoConfig.serverAddr, PicoConfig.SSL_serverPort);
            Socket ss = NaiveSocketFactory.getSocketFactory().createSocket();
            ss.connect(addr, 100);
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
            Log.getStackTraceString(e);
        } catch (IOException e) {
            Log.getStackTraceString(e);
        }
        return null;
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

}
