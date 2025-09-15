package com.mypills;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationActionReceiver extends BroadcastReceiver {
    
    private static final String ACTION_TAKE_PILL = "com.mypills.TAKE_PILL";
    private static final String ACTION_SNOOZE_PILL = "com.mypills.SNOOZE_PILL";
    private static final String ACTION_PILL_REMINDER = "com.mypills.PILL_REMINDER";
    private static final String PREFS_NAME = "MyPillsPrefs";
    private static final String PILLS_KEY = "saved_pills";
    private static final String CHANNEL_ID = "pill_reminders";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (ACTION_TAKE_PILL.equals(action)) {
            handleTakePill(context, intent);
        } else if (ACTION_SNOOZE_PILL.equals(action)) {
            handleSnoozePill(context, intent);
        }
    }
    
    private void handleTakePill(Context context, Intent intent) {
        try {
            String pillName = intent.getStringExtra("pill_name");
            int pillIndex = intent.getIntExtra("pill_index", -1);
            
            if (pillIndex >= 0) {
                // Update pill status in SharedPreferences
                updatePillStatus(context, pillIndex, true);
                
                // Cancel the notification
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(pillIndex + 1000);
                
                // Show confirmation notification
                showConfirmationNotification(context, pillName, "taken");
                
                // Send broadcast to update MainActivity if it's running
                Intent updateIntent = new Intent("com.mypills.PILL_TAKEN");
                updateIntent.putExtra("pill_index", pillIndex);
                context.sendBroadcast(updateIntent);
            }
        } catch (Exception e) {
            // Silent fail
        }
    }
    
    private void handleSnoozePill(Context context, Intent intent) {
        try {
            String pillName = intent.getStringExtra("pill_name");
            String pillDosage = intent.getStringExtra("pill_dosage");
            int pillIndex = intent.getIntExtra("pill_index", -1);
            
            if (pillIndex >= 0) {
                // Cancel the current notification
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(pillIndex + 1000);
                
                // Schedule a new notification for 10 minutes from now
                scheduleSnoozeNotification(context, pillName, pillDosage, pillIndex);
                
                // Show snooze confirmation
                showConfirmationNotification(context, pillName, "snoozed");
            }
        } catch (Exception e) {
            // Silent fail
        }
    }
    
    private void updatePillStatus(Context context, int pillIndex, boolean taken) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String pillsJson = sharedPreferences.getString(PILLS_KEY, null);
            
            if (pillsJson != null && !pillsJson.isEmpty()) {
                JSONArray pillsArray = new JSONArray(pillsJson);
                
                if (pillIndex < pillsArray.length()) {
                    JSONObject pillObject = pillsArray.getJSONObject(pillIndex);
                    pillObject.put("taken", taken);
                    
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(PILLS_KEY, pillsArray.toString());
                    editor.apply();
                }
            }
        } catch (Exception e) {
            // Silent fail
        }
    }
    
    private void scheduleSnoozeNotification(Context context, String pillName, String pillDosage, int pillIndex) {
        try {
            // Calculate time 10 minutes from now
            Calendar snoozeTime = Calendar.getInstance();
            snoozeTime.add(Calendar.MINUTE, 10);
            
            // Create intent for snooze notification
            Intent snoozeIntent = new Intent(context, PillReminderReceiver.class);
            snoozeIntent.setAction(ACTION_PILL_REMINDER);
            snoozeIntent.putExtra("pill_name", pillName);
            snoozeIntent.putExtra("pill_dosage", pillDosage);
            snoozeIntent.putExtra("pill_index", pillIndex);
            
            PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context, 
                pillIndex + 2000, // Different request code for snooze
                snoozeIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            // Schedule the snooze notification
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime.getTimeInMillis(),
                    snoozePendingIntent
                );
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime.getTimeInMillis(),
                    snoozePendingIntent
                );
            }
        } catch (Exception e) {
            // Silent fail
        }
    }
    
    private void showConfirmationNotification(Context context, String pillName, String action) {
        try {
            // Create notification channel if needed
            createNotificationChannel(context);
            
            String title = action.equals("taken") ? "✅ Pill Taken!" : "⏰ Pill Snoozed";
            String text = action.equals("taken") ? 
                pillName + " marked as taken" : 
                pillName + " reminder in 10 minutes";
            
            // Create intent to open app
            Intent appIntent = new Intent(context, MainActivity.class);
            appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, appIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            // Create confirmation notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(action.equals("taken") ? 
                    android.graphics.Color.parseColor("#10B981") : 
                    android.graphics.Color.parseColor("#F59E0B"))
                .setDefaults(NotificationCompat.DEFAULT_ALL);
            
            // Show notification
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(9999, builder.build()); // Use unique ID for confirmations
        } catch (Exception e) {
            // Silent fail
        }
    }
    
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                CHANNEL_ID,
                "Pill Reminders",
                android.app.NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for pill reminders");
            channel.enableLights(true);
            channel.setLightColor(android.graphics.Color.parseColor("#6366F1"));
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            
            android.app.NotificationManager notificationManager = context.getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}