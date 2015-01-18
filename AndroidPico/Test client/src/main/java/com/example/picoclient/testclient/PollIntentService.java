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
    private final String TAG = this.getClass().getSimpleName();
    public static final String ACTION = "ACTION";
    public static final String START_POLLING = "START_POLLING";
    // To determine how many times key has been asked
    public static final String GET_COUNT = "GET_COUNT";
    //
    public static final String DECRYPT_KEY = "GET_COUNT";
    public static final String UID = "UID";
    int counter = 0;
    NotificationManager nm;
    private int NotificationID = R.string.notification_id;

    public PollIntentService() {
        super("PollIntentService");
    }

    public static void startPolling(Context context, String uid) {
        Log.i("PollIntentSerive","start polling called");
        Intent intent = new Intent(context, PollIntentService.class);
        intent.setAction(START_POLLING);
        intent.putExtra(UID, uid);
        context.startService(intent);
    }
    public static void getKeyCount(Context context, String uid) {
        Log.i("PollIntentSerive","start polling called");
        Intent intent = new Intent(context, PollIntentService.class);
        intent.setAction(GET_COUNT);
        intent.putExtra(UID, uid);
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
            }
            else if (GET_COUNT.equals(action)) {
                final String uid = intent.getStringExtra(UID);
                handleGetKeyCount(uid);
            }
        }
    }

    private void handleGetKeyCount(String uid) {
        String messageToSend = "key]" + uid;
        String result = sendStringToServer(messageToSend);
        Log.i(TAG, "KeyCount " + result);
        showNotification("KeyCount", result, true);
        Intent localIntent =
                new Intent(MainActivity.GET_KEY_COUNT).putExtra("Count", result);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        Log.i(TAG, "Sending the local broadcast");
    }

    private String sendStringToServer(String s){
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

    private void handleActionPollServer(String uid) {
        Log.i(TAG, "Polling server for id " + uid);
        String messageToSend = "get]" + uid;
        String result = sendStringToServer(messageToSend);
        Log.i(TAG, "secret received is " + result);
        showNotification("Secret", result, true);
        Intent localIntent =
                new Intent(MainActivity.UNLOCK_APP);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        Log.i(TAG, "Sending the local broadcast");
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
