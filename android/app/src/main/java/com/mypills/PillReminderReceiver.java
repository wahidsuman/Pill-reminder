package com.mypills;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;

public class PillReminderReceiver extends BroadcastReceiver {
    
    private static final String CHANNEL_ID = "pill_reminders";
    private static final String CHANNEL_NAME = "Pill Reminders";
    private static final String CHANNEL_DESCRIPTION = "Notifications for pill reminders";
    
    // Action constants
    private static final String ACTION_TAKE_PILL = "com.mypills.TAKE_PILL";
    private static final String ACTION_SNOOZE_PILL = "com.mypills.SNOOZE_PILL";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.mypills.PILL_REMINDER")) {
            String pillName = intent.getStringExtra("pill_name");
            String pillDosage = intent.getStringExtra("pill_dosage");
            int pillIndex = intent.getIntExtra("pill_index", 0);
            
            // Acquire wake lock to ensure notification is shown even when device is sleeping
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            if (powerManager != null) {
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyPills:PillReminder");
                wakeLock.acquire(10000); // Hold for 10 seconds
            }
            
            try {
                // Create notification channel if needed
                createNotificationChannel(context);
            
            // Create intent for when notification is tapped
            Intent appIntent = new Intent(context, MainActivity.class);
            appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, appIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            // Create "Take" action intent
            Intent takeIntent = new Intent(context, NotificationActionReceiver.class);
            takeIntent.setAction(ACTION_TAKE_PILL);
            takeIntent.putExtra("pill_name", pillName);
            takeIntent.putExtra("pill_dosage", pillDosage);
            takeIntent.putExtra("pill_index", pillIndex);
            PendingIntent takePendingIntent = PendingIntent.getBroadcast(
                context, pillIndex * 2, takeIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            // Create "Snooze" action intent
            Intent snoozeIntent = new Intent(context, NotificationActionReceiver.class);
            snoozeIntent.setAction(ACTION_SNOOZE_PILL);
            snoozeIntent.putExtra("pill_name", pillName);
            snoozeIntent.putExtra("pill_dosage", pillDosage);
            snoozeIntent.putExtra("pill_index", pillIndex);
            PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context, pillIndex * 2 + 1, snoozeIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            // Create notification with action buttons
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("💊 Time for " + pillName)
                .setContentText("Take " + pillDosage + " now")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(Color.parseColor("#6366F1"))
                .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText("It's time to take your " + pillName + " (" + pillDosage + "). " +
                            "Tap 'Take' to mark as taken or 'Snooze' to remind later."))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setLights(Color.parseColor("#6366F1"), 1000, 1000)
                .addAction(android.R.drawable.ic_menu_send, "✓ Take", takePendingIntent)
                .addAction(android.R.drawable.ic_menu_recent_history, "⏰ Snooze 10m", snoozePendingIntent);
            
                // Show notification
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(pillIndex + 1000, builder.build()); // Use unique ID for each pill
                
            } finally {
                // Release wake lock
                if (wakeLock != null && wakeLock.isHeld()) {
                    wakeLock.release();
                }
            }
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
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}