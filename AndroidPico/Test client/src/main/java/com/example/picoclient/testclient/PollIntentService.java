package com.example.picoclient.testclient;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class PollIntentService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION = "ACTION";
    public static final String START_POLLING = "START_POLLING";
    public static final String UID = "UID";
    int counter = 0;
    NotificationManager nm;
    private int NotificationID = R.string.notification_id;

    public PollIntentService() {
        super("PollIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Log.i("PollIntentService", "onCreate called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("PollIntentService", "onDestroy called");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("PollIntentService", "Handling intent");
        Log.i("PollIntentService", "Action is " + intent.getStringExtra(ACTION));
        Log.i("PollIntentService", "UID is " + intent.getStringExtra(UID));

        if (intent.getStringExtra(ACTION).equals(START_POLLING)) {
            final String uid = intent.getStringExtra(UID);
            handleActionPollServer(uid);
        }
    }

    private void handleActionPollServer(String param1) {
        Log.i("PollIntentService", "Polling server for id " + param1);

        try {
            InetSocketAddress addr = new InetSocketAddress(PicoConfig.serverAddr, PicoConfig.SSL_serverPort);
            Socket ss = NaiveSocketFactory.getSocketFactory().createSocket();
            ss.connect(addr, 100);
            BufferedReader br = new BufferedReader(new InputStreamReader(ss.getInputStream()));
            PrintWriter out = new PrintWriter(ss.getOutputStream());
            // welcome message ignore
            br.readLine();
            out.print("get]" + param1);
            out.flush();
            String secretShareFromServer = br.readLine();
            Log.i("PollIntentService", "secret received is " + secretShareFromServer);
            showNotification("Secret", secretShareFromServer, true);
        } catch (UnknownHostException e) {
            showNotification("Error", e.getLocalizedMessage(), false);
            Log.getStackTraceString(e);
        } catch (IOException e) {
            showNotification("Error", e.getLocalizedMessage(), false);
            Log.getStackTraceString(e);
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

}
