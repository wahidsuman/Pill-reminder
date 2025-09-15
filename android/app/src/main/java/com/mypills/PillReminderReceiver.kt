package com.mypills

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat

class PillReminderReceiver : BroadcastReceiver() {
    
    companion object {
        private const val CHANNEL_ID = "pill_reminders"
        private const val CHANNEL_NAME = "Pill Reminders"
        private const val CHANNEL_DESCRIPTION = "Notifications for pill reminders"
        private const val ACTION_TAKE_PILL = "com.mypills.TAKE_PILL"
        private const val ACTION_SNOOZE_PILL = "com.mypills.SNOOZE_PILL"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.mypills.PILL_REMINDER") {
            val pillName = intent.getStringExtra("pill_name") ?: return
            val pillDosage = intent.getStringExtra("pill_dosage") ?: return
            val pillIndex = intent.getIntExtra("pill_index", 0)
            
            // Acquire wake lock to ensure notification is shown even when device is sleeping
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "MyPills:PillReminder"
            )
            wakeLock.acquire(10000) // Hold for 10 seconds
            
            try {
                // Create notification channel if needed
                createNotificationChannel(context)
                
                // Create intent for when notification is tapped
                val appIntent = Intent(context, MainActivity::class.java)
                appIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, appIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                // Create "Take" action intent
                val takeIntent = Intent(context, NotificationActionReceiver::class.java)
                takeIntent.action = ACTION_TAKE_PILL
                takeIntent.putExtra("pill_name", pillName)
                takeIntent.putExtra("pill_dosage", pillDosage)
                takeIntent.putExtra("pill_index", pillIndex)
                val takePendingIntent = PendingIntent.getBroadcast(
                    context,
                    pillIndex * 2,
                    takeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                // Create "Snooze" action intent
                val snoozeIntent = Intent(context, NotificationActionReceiver::class.java)
                snoozeIntent.action = ACTION_SNOOZE_PILL
                snoozeIntent.putExtra("pill_name", pillName)
                snoozeIntent.putExtra("pill_dosage", pillDosage)
                snoozeIntent.putExtra("pill_index", pillIndex)
                val snoozePendingIntent = PendingIntent.getBroadcast(
                    context,
                    pillIndex * 2 + 1,
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                // Create notification with action buttons
                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("💊 Time for $pillName")
                    .setContentText("Take $pillDosage now")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setColor(Color.parseColor("#6366F1"))
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(
                                "It's time to take your $pillName ($pillDosage). " +
                                        "Tap 'Take' to mark as taken or 'Snooze' to remind later."
                            )
                    )
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setVibrate(longArrayOf(0, 500, 200, 500))
                    .setLights(Color.parseColor("#6366F1"), 1000, 1000)
                    .addAction(android.R.drawable.ic_menu_send, "✓ Take", takePendingIntent)
                    .addAction(android.R.drawable.ic_menu_recent_history, "⏰ Snooze 10m", snoozePendingIntent)
                
                // Show notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(pillIndex + 1000, builder.build())
                
            } finally {
                // Release wake lock
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
            }
        }
    }
    
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = CHANNEL_DESCRIPTION
            channel.enableLights(true)
            channel.lightColor = Color.parseColor("#6366F1")
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(0, 500, 200, 500)
            channel.setShowBadge(true)
            channel.lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}