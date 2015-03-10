package com.example.picoclient.testclient;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

// This class will schedule the alarms for future actions.
// e.g. polling the server at regular interval, making sure app locks after certain interval
public class AlarmBroadcastReceiver extends BroadcastReceiver {
    private static int POLLING_ALARM = R.string.polling_alarm;
    private static int LOCKING_ALARM = R.string.locking_alarm;
    private static String POLLING_ALARM_ACTION = "POLLING_ALARM_ACTION";
    private static String LOCKING_ALARM_ACTION = "LOCKING_ALARM_ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (POLLING_ALARM_ACTION.equals(intent.getAction())) {
                ServerAPIIntentService.startPolling(context, Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
            } else if (LOCKING_ALARM_ACTION.equals(intent.getAction())) {
                ServerAPIIntentService.lockApp(context);
            } else {
                Log.e(this.getClass().getSimpleName(), "unknown intent action " + intent.getAction());
            }
        }
        Log.i("ALARM", "Intent action is " + intent.getAction());
    }

    public void setPollingAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmBroadcastReceiver.class);
        i.setAction(POLLING_ALARM_ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(context, POLLING_ALARM, i, 0);
        // Inexact reduces battery drain associated with waking the device to perform polling.
        // However, the frequencies are INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR,
        // INTERVAL_HALF_DAY, INTERVAL_DAY
        String interval = PreferenceManager.getDefaultSharedPreferences(context).getString("pref_interval", "0");
        am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), Integer.parseInt(interval), pi); // Millisec * Second * Minute
        Log.i("ALARM", "Polling Alarm set");
    }

    public void setLockingAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmBroadcastReceiver.class);
        i.setAction(LOCKING_ALARM_ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(context, LOCKING_ALARM, i, PendingIntent.FLAG_UPDATE_CURRENT);
        String interval = PreferenceManager.getDefaultSharedPreferences(context).getString("pref_alive_interval", "0");
        am.setExact(AlarmManager.RTC, System.currentTimeMillis() + Integer.parseInt(interval), pi); // Lock the app after pref_alive_interval sec
        Log.i("ALARM", "Locking alarm set");
    }

    public void cancelPollingAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmBroadcastReceiver.class);
        i.setAction(POLLING_ALARM_ACTION);
        PendingIntent sender = PendingIntent.getBroadcast(context, POLLING_ALARM, i, 0);
        am.cancel(sender);
        Log.i("ALARM", "Polling Alarm cancelled");
    }
}
