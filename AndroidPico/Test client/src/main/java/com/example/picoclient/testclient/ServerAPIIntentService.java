package com.example.picoclient.testclient;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.TrafficStats;
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
//    public static final String START_POLLING = "START_POLLING";
    public static final String UNLOCK_APP = "UNLOCK_APP";
    public static final String LOCK_APP = "LOCK_APP";
    public static final String GET_COUNT = "GET_COUNT";
    public static final String CREATE_SERVER_SHARE = "CREATE_SERVER_SHARE";
    public static final String REQUEST_REV_KEY = "REQUEST_REV_KEY";
    public static final String UID = "UID";
    public static final String KEY = "KEY";
    public static final String RESPONSE = "RESPONSE";
    private final String TAG = this.getClass().getSimpleName();
    int counter = 0;
    NotificationManager nm;
    SharedPreferences prefs;
    AlarmBroadcastReceiver alarmBroadcastReceiver;
    private int NotificationID = R.string.notification_id;

    public ServerAPIIntentService() {
        super("PollIntentService");
    }

    /**
     * Creates an intent to Lock the app. This will remove the server share
     * from shared preferences and send an intent to MainActivity to update
     * status text.
     *
     * */
    public static void lockApp(Context context) {
        Intent intent = new Intent(context, ServerAPIIntentService.class);
        intent.setAction(LOCK_APP);
        context.startService(intent);
    }


//    public static void startPolling(Context context, String uid) {
//        Intent intent = new Intent(context, ServerAPIIntentService.class);
//        intent.setAction(UNLOCK_APP);
//        intent.putExtra(UID, uid);
//        context.startService(intent);
//    }

    public static void getKeyCount(Context context, String uid) {
        Intent intent = new Intent(context, ServerAPIIntentService.class);
        intent.setAction(GET_COUNT);
        intent.putExtra(UID, uid);
        context.startService(intent);
    }



    /**
     * Creates an intent to unlock the app polling. This will create an alarm
     * to call lockApp to forget the server share after the availability
     * duration.
     *
     * @param  uid The UID of the device
     * */
    public static void unlockApp(Context context, String uid) {
        Intent intent = new Intent(context, ServerAPIIntentService.class);
        intent.setAction(UNLOCK_APP);
        intent.putExtra(UID, uid);
        context.startService(intent);
    }

    /**
     * Creates an intent to create a disabling key.
     *
     * @param  uid The UID of the device
     * @param challenge The OTP challenge
     * */
    public static void requestRevKey(Context context, String uid, String challenge) {
        Intent intent = new Intent(context, ServerAPIIntentService.class);
        intent.setAction(REQUEST_REV_KEY);
        intent.putExtra(UID, uid);
        intent.putExtra(RESPONSE, challenge);
        context.startService(intent);
    }

    public static void saveKey(Context context, String uid, String key) {
        Intent intent = new Intent(context, ServerAPIIntentService.class);
        intent.setAction(CREATE_SERVER_SHARE);
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
        if (intent != null) {
            final String action = intent.getAction();
//            Log.i(TAG, "intent action is " + action);
            String comments = action + '\n';
            Intent stateLoggingIntent = new Intent();
            stateLoggingIntent.putExtra("poll_start_time", Long.valueOf(System.currentTimeMillis()));
            stateLoggingIntent.setAction(LoggingService.STATE_LOGGING_INTENT);
            long startTrafficReceived = TrafficStats.getTotalRxBytes();
            long startTrafficTransmitted = TrafficStats.getTotalTxBytes();
            try {
                stateLoggingIntent.putExtra("poll_status", 1);
//                if (START_POLLING.equals(action)) {
//                    final String uid = intent.getStringExtra(UID);
//                    handleUnlockApp(uid);
//                } else
                if (GET_COUNT.equals(action)) {
                    final String uid = intent.getStringExtra(UID);
                    handleGetKeyCount(uid);
                } else if (UNLOCK_APP.equals(action)) {
                    final String uid = intent.getStringExtra(UID);
                    handleUnlockApp(uid);
                } else if (CREATE_SERVER_SHARE.equals(action)) {
                    final String uid = intent.getStringExtra(UID);
                    final String key = intent.getStringExtra(KEY);
                    handleCreateServerShare(uid, key);
                } else if (LOCK_APP.equals(action)) {
                    handleLockApp();
                }
                else if (REQUEST_REV_KEY.equals(action)) {
                    final String uid = intent.getStringExtra(UID);
                    final String response = intent.getStringExtra(RESPONSE);
                    handleRequestRevKey(uid, response);
                }
            } catch (IOException e) {
                incrementFailedCount();
//                showNotification("Error", "Action is " + action + " Message is " + e.getLocalizedMessage(), false);
                Log.e(TAG, e.getMessage());
                comments = comments + e.getMessage() + "\n";
                e.printStackTrace();
                stateLoggingIntent.putExtra("poll_status", 0);
            }
            if (prefs.contains("secretKey")) {
                stateLoggingIntent.putExtra("availability_status", 1);
            } else {
                stateLoggingIntent.putExtra("availability_status", 0);
            }
            long finishTrafficReceived = TrafficStats.getTotalRxBytes();
            long finishTrafficTransmitted = TrafficStats.getTotalTxBytes();
            stateLoggingIntent.putExtra("comments", comments);
            stateLoggingIntent.putExtra("traffic_received", startTrafficReceived == TrafficStats.UNSUPPORTED ? -1 : finishTrafficReceived - startTrafficReceived);
            stateLoggingIntent.putExtra("traffic_transmitted", startTrafficReceived == TrafficStats.UNSUPPORTED ? -1 : finishTrafficTransmitted - startTrafficTransmitted);
            stateLoggingIntent.putExtra("poll_end_time", Long.valueOf(System.currentTimeMillis()));
            sendBroadcast(stateLoggingIntent);
            Log.i("Broadcast", "sending broadcast");
        } else {
            Log.e(TAG, "null intent received");
        }
    }

    /**
     * This method is called when an intent with action LOCK_APP is
     * received. It removes the server share from SharedPreferences and
     * sends an intent to the main UI to change the status text.
     *
     * */

     private void handleLockApp() {
        prefs.edit().remove("secretKey").commit();
        Intent localIntent = new Intent(MainActivity.LOCK_APP);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void handleGetKeyCount(String uid) throws IOException {
        String messageToSend = "key]" + uid;
        String result = sendStringToServer(messageToSend);
        Intent localIntent =
                new Intent(MainActivity.NOTIFY_USER)
                        .putExtra(MainActivity.NOTIFY_USER_MESSAGE, result);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    /**
     * This method is called when an intent with action CREATE_SERVER_SHARE is
     * received. It sends a message to the server to create a new share and
     * sends an intent to the main UI to notify the user.
     *
     * @param uid The UID of the device
     * @param key The Server Share.
     *
     * */
    private void handleCreateServerShare(String uid, String key) throws IOException {
        String messageToSend = "add]" + uid + "]" + key;
        String authCode = sendStringToServer(messageToSend);
        if (authCode.isEmpty()) {
            Log.i("empty", "empty");
        } else {
            prefs.edit().putString("authCode", authCode).commit();
            Intent localIntent =
                    new Intent(MainActivity.NOTIFY_USER)
                            .putExtra(MainActivity.NOTIFY_USER_MESSAGE, "Paired!");
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }

    /**
     * This method is called when an intent with action REQUEST is received.
     * encode the uid and challenge to get a OTP response. Sends an intent
     * to the UI to notify user of the response.
     *
     * @param uid The UID of the device
     * @param challenge The OTP challenge.
     *
     * */
    private void handleRequestRevKey(String uid, String challenge) throws IOException {
        String authCode = prefs.getString("authCode","");
        String messageToSend = "request]" + uid + "]" + authCode + "]" + challenge;
        String response = sendStringToServer(messageToSend);
        Intent localIntent = new Intent(MainActivity.NOTIFY_USER_ALERT);
        if (!response.isEmpty()) {
            // tell mainactivity to update UI
            localIntent.putExtra(MainActivity.NOTIFY_USER_MESSAGE, response);
        }
        localIntent.putExtra(MainActivity.NOTIFY_USER_MESSAGE, response);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    /**
     * This method is called when an intent with action UNLOCK_APP is received.
     * This tries to retrieve server share from the server. If it exists, it
     * will store it to SharedPreferences, send an intent to MainActivity
     * to update the UI and then set an alarm to forget the share in the future.
     *
     * @param uid The UID of the device
     *
     * */
    private void handleUnlockApp(String uid) throws IOException {
        String authCode = prefs.getString("authCode","");
        String messageToSend = "get]" +  uid + "]" + authCode;
        String key = sendStringToServer(messageToSend);
        if (key.isEmpty() || key.startsWith("Revoked")) {
//            showNotification("Error: revoked/missing key", "Key is not found on server", false);
        } else {
            incrementSuccessCount();
            prefs.edit().putString("secretKey", key).commit();
            Intent localIntent = new Intent(MainActivity.UNLOCK_APP);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            alarmBroadcastReceiver.setLockingAlarm(this);
        }
    }

    // The following are helper functions
    private String sendStringToServer(String s) throws IOException {
        try {
            String serverAddr = prefs.getString("pref_sync_addr", "dlow.me");
            int serverPort = Integer.parseInt(prefs.getString("pref_sync_port", "8001"));
            InetSocketAddress addr = new InetSocketAddress(serverAddr, serverPort);
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
            if (res == null){
                return "No response from server";
            }
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
            prefs.edit().putInt("failedAttempts",
                    prefs.getInt("failedAttempts", 0) + 1).commit();
        } else {
            prefs.edit().putInt("failedAttempts", 1).commit();
        }
    }

    private void incrementSuccessCount() {
        // Increment successful count in app
        if (prefs.contains("successfulAttempts")) {
            prefs.edit().putInt("successfulAttempts",
                    prefs.getInt("successfulAttempts", 0) + 1).commit();
        } else {
            prefs.edit().putInt("successfulAttempts", 1).commit();
        }
    }
}
