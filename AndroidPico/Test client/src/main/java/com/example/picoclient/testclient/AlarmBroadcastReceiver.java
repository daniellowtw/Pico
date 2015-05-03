package com.example.picoclient.testclient;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

/**
 * This class will schedule the alarms for future actions.
 * e.g. polling the server at regular interval, making sure app locks after certain interval
 */

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    /**
     * Expose three methods: set polling alarm, set locking alarm and cancel alarm.
     * These alarms will fire an intent that is caught by the onReceive method of this class.
     * This then calls the methods of ServerAPIIntentService.
     */

    private static int POLLING_ALARM = R.string.polling_alarm;
    private static int LOCKING_ALARM = R.string.locking_alarm;
    private static String POLLING_ALARM_ACTION = "POLLING_ALARM_ACTION";
    private static String LOCKING_ALARM_ACTION = "LOCKING_ALARM_ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (POLLING_ALARM_ACTION.equals(intent.getAction())) {
                ServerAPIIntentService.unlockApp(context,
                        Settings.Secure.getString(context.getContentResolver(),
                                Settings.Secure.ANDROID_ID));
                setPollingAlarm(context);
            } else if (LOCKING_ALARM_ACTION.equals(intent.getAction())) {
                ServerAPIIntentService.lockApp(context);
            } else {
                Log.e(this.getClass().getSimpleName(),
                        "unknown intent action " + intent.getAction());
            }
        }
//        Log.i("ALARM", "Intent action is " + intent.getAction());
    }

    public void setPollingAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmBroadcastReceiver.class);
        i.setAction(POLLING_ALARM_ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(context, POLLING_ALARM, i, 0);
        String interval = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString("pref_interval", "0");
        am.setExact(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + Integer.parseInt(interval),
                pi);
        Log.i("ALARM", "Polling Alarm set");
    }

    public void setLockingAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmBroadcastReceiver.class);
        i.setAction(LOCKING_ALARM_ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(context, LOCKING_ALARM, i,
                PendingIntent.FLAG_CANCEL_CURRENT);
        String interval = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("pref_alive_interval", "0");
        am.setExact(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + Integer.parseInt(interval),
                pi);
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
