package com.example.picoclient.testclient;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class PollingBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        // Put here YOUR code.
        PollIntentService.startPolling(context, Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        Log.i("Polling", "Polling activated");
        Toast.makeText(context, "Polling!", Toast.LENGTH_LONG).show(); // For example
        wl.release();
    }

    public void SetAlarm(Context context)
    {
        Log.i("Polling", "Alarm set");
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, PollingBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        // Inexact reduces battery drain associated with waking the device to perform polling.
        // However, the frequencies are INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR,
        // INTERVAL_HALF_DAY, INTERVAL_DAY
        am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), 1000 * 10, pi); // Millisec * Second * Minute
    }

    public void CancelAlarm(Context context)
    {
        Log.i("Polling", "Alarm cancelled");
        Intent intent = new Intent(context, PollingBroadcastReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
