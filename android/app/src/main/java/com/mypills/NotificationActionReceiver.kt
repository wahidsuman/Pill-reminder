package com.mypills

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class NotificationActionReceiver : BroadcastReceiver() {
    
    companion object {
        private const val ACTION_TAKE_PILL = "com.mypills.TAKE_PILL"
        private const val ACTION_SNOOZE_PILL = "com.mypills.SNOOZE_PILL"
        private const val ACTION_PILL_REMINDER = "com.mypills.PILL_REMINDER"
        private const val PREFS_NAME = "MyPillsPrefs"
        private const val PILLS_KEY = "saved_pills"
        private const val CHANNEL_ID = "pill_reminders"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TAKE_PILL -> handleTakePill(context, intent)
            ACTION_SNOOZE_PILL -> handleSnoozePill(context, intent)
        }
    }
    
    private fun handleTakePill(context: Context, intent: Intent) {
        try {
            val pillName = intent.getStringExtra("pill_name") ?: return
            val pillIndex = intent.getIntExtra("pill_index", -1)
            
            if (pillIndex >= 0) {
                // Update pill status in SharedPreferences
                updatePillStatus(context, pillIndex, true)
                
                // Cancel the notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(pillIndex + 1000)
                
                // Show confirmation notification
                showConfirmationNotification(context, pillName, "taken")
                
                // Send broadcast to update MainActivity if it's running
                val updateIntent = Intent("com.mypills.PILL_TAKEN")
                updateIntent.putExtra("pill_index", pillIndex)
                context.sendBroadcast(updateIntent)
            }
        } catch (e: Exception) {
            // Silent fail
        }
    }
    
    private fun handleSnoozePill(context: Context, intent: Intent) {
        try {
            val pillName = intent.getStringExtra("pill_name") ?: return
            val pillDosage = intent.getStringExtra("pill_dosage") ?: return
            val pillIndex = intent.getIntExtra("pill_index", -1)
            
            if (pillIndex >= 0) {
                // Cancel the current notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(pillIndex + 1000)
                
                // Schedule a new notification for 10 minutes from now
                scheduleSnoozeNotification(context, pillName, pillDosage, pillIndex)
                
                // Show snooze confirmation
                showConfirmationNotification(context, pillName, "snoozed")
            }
        } catch (e: Exception) {
            // Silent fail
        }
    }
    
    private fun updatePillStatus(context: Context, pillIndex: Int, taken: Boolean) {
        try {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val pillsJson = sharedPreferences.getString(PILLS_KEY, null) ?: return
            
            val pillsArray = JSONArray(pillsJson)
            
            if (pillIndex < pillsArray.length()) {
                val pillObject = pillsArray.getJSONObject(pillIndex)
                pillObject.put("taken", taken)
                
                sharedPreferences.edit()
                    .putString(PILLS_KEY, pillsArray.toString())
                    .apply()
            }
        } catch (e: Exception) {
            // Silent fail
        }
    }
    
    private fun scheduleSnoozeNotification(context: Context, pillName: String, pillDosage: String, pillIndex: Int) {
        try {
            // Calculate time 10 minutes from now
            val snoozeTime = Calendar.getInstance()
            snoozeTime.add(Calendar.MINUTE, 10)
            
            // Create intent for snooze notification
            val snoozeIntent = Intent(context, PillReminderReceiver::class.java)
            snoozeIntent.action = ACTION_PILL_REMINDER
            snoozeIntent.putExtra("pill_name", pillName)
            snoozeIntent.putExtra("pill_dosage", pillDosage)
            snoozeIntent.putExtra("pill_index", pillIndex)
            
            val snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                pillIndex + 2000, // Different request code for snooze
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Schedule the snooze notification
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime.timeInMillis,
                    snoozePendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime.timeInMillis,
                    snoozePendingIntent
                )
            }
        } catch (e: Exception) {
            // Silent fail
        }
    }
    
    private fun showConfirmationNotification(context: Context, pillName: String, action: String) {
        try {
            // Create notification channel if needed
            createNotificationChannel(context)
            
            val title = if (action == "taken") "✅ Pill Taken!" else "⏰ Pill Snoozed"
            val text = if (action == "taken") {
                "$pillName marked as taken"
            } else {
                "$pillName reminder in 10 minutes"
            }
            
            // Create intent to open app
            val appIntent = Intent(context, MainActivity::class.java)
            appIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent = PendingIntent.getActivity(
                context, 0, appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Create confirmation notification
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(
                    if (action == "taken") {
                        android.graphics.Color.parseColor("#10B981")
                    } else {
                        android.graphics.Color.parseColor("#F59E0B")
                    }
                )
                .setDefaults(NotificationCompat.DEFAULT_ALL)
            
            // Show notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(9999, builder.build()) // Use unique ID for confirmations
        } catch (e: Exception) {
            // Silent fail
        }
    }
    
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pill Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notifications for pill reminders"
            channel.enableLights(true)
            channel.lightColor = android.graphics.Color.parseColor("#6366F1")
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(0, 500, 200, 500)
            channel.setShowBadge(true)
            channel.lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}