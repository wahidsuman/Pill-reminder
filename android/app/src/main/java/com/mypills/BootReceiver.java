package com.mypills;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BootReceiver extends BroadcastReceiver {
    
    private static final String CHANNEL_ID = "pill_reminders";
    private static final String CHANNEL_NAME = "Pill Reminders";
    private static final String CHANNEL_DESCRIPTION = "Notifications for pill reminders";
    private static final String ACTION_PILL_REMINDER = "com.mypills.PILL_REMINDER";
    private static final String EXTRA_PILL_NAME = "pill_name";
    private static final String EXTRA_PILL_DOSAGE = "pill_dosage";
    private static final String EXTRA_PILL_INDEX = "pill_index";
    private static final String PREFS_NAME = "MyPillsPrefs";
    private static final String PILLS_KEY = "saved_pills";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction()) ||
            Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
            
            // Reschedule all pill notifications after device restart
            rescheduleAllNotifications(context);
        }
    }
    
    private void rescheduleAllNotifications(Context context) {
        try {
            // Create notification channel
            createNotificationChannel(context);
            
            // Load saved pills
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String pillsJson = sharedPreferences.getString(PILLS_KEY, null);
            
            if (pillsJson == null || pillsJson.isEmpty()) {
                return; // No pills to schedule
            }
            
            JSONArray pillsArray = new JSONArray(pillsJson);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            
            // Schedule notifications for all pills
            for (int i = 0; i < pillsArray.length(); i++) {
                JSONObject pillObject = pillsArray.getJSONObject(i);
                String name = pillObject.getString("name");
                String dosage = pillObject.getString("dosage");
                String time = pillObject.getString("time");
                
                schedulePillNotification(context, alarmManager, i, name, dosage, time);
            }
            
        } catch (Exception e) {
            // Silent fail - notifications will be rescheduled when app opens
        }
    }
    
    private void schedulePillNotification(Context context, AlarmManager alarmManager, int pillIndex, String pillName, String pillDosage, String timeString) {
        try {
            // Parse time string
            Calendar calendar = parseTimeString(timeString);
            if (calendar == null) {
                return;
            }
            
            // Create intent for the alarm
            Intent intent = new Intent(context, PillReminderReceiver.class);
            intent.setAction(ACTION_PILL_REMINDER);
            intent.putExtra(EXTRA_PILL_NAME, pillName);
            intent.putExtra(EXTRA_PILL_DOSAGE, pillDosage);
            intent.putExtra(EXTRA_PILL_INDEX, pillIndex);
            
            // Create pending intent with unique request code
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 
                pillIndex,
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            // Schedule exact alarm for better reliability
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
                );
                // Set repeating alarm for daily notifications
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                );
            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                );
            }
            
        } catch (Exception e) {
            // Silent fail
        }
    }
    
    private Calendar parseTimeString(String timeString) {
        try {
            // Remove extra spaces and convert to standard format
            timeString = timeString.trim().toUpperCase();
            
            // Parse formats like "8:00 AM", "2:00 PM", "14:00", etc.
            SimpleDateFormat format;
            if (timeString.contains("AM") || timeString.contains("PM")) {
                format = new SimpleDateFormat("h:mm a", Locale.US);
            } else {
                format = new SimpleDateFormat("H:mm", Locale.US);
            }
            
            Date time = format.parse(timeString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);
            
            // Set to today's date
            Calendar today = Calendar.getInstance();
            calendar.set(Calendar.YEAR, today.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, today.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
            
            // If the time has already passed today, set for tomorrow
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            
            return calendar;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.setLightColor(Color.parseColor("#6366F1"));
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}