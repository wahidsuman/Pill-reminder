package com.mypills

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val CHANNEL_ID = "pill_reminders"
        private const val CHANNEL_NAME = "Pill Reminders"
        private const val CHANNEL_DESCRIPTION = "Notifications for pill reminders"
        private const val ACTION_PILL_REMINDER = "com.mypills.PILL_REMINDER"
        private const val PREFS_NAME = "MyPillsPrefs"
        private const val PILLS_KEY = "saved_pills"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == Intent.ACTION_PACKAGE_REPLACED
        ) {
            // Reschedule all pill notifications after device restart
            rescheduleAllNotifications(context)
        }
    }
    
    private fun rescheduleAllNotifications(context: Context) {
        try {
            // Create notification channel
            createNotificationChannel(context)
            
            // Load saved pills
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val pillsJson = sharedPreferences.getString(PILLS_KEY, null) ?: return
            
            val pillsArray = JSONArray(pillsJson)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // Schedule notifications for all pills
            for (i in 0 until pillsArray.length()) {
                val pillObject = pillsArray.getJSONObject(i)
                val name = pillObject.getString("name")
                val dosage = pillObject.getString("dosage")
                val time = pillObject.getString("time")
                
                schedulePillNotification(context, alarmManager, i, name, dosage, time)
            }
        } catch (e: Exception) {
            // Silent fail - notifications will be rescheduled when app opens
        }
    }
    
    private fun schedulePillNotification(
        context: Context,
        alarmManager: AlarmManager,
        pillIndex: Int,
        pillName: String,
        pillDosage: String,
        timeString: String
    ) {
        try {
            // Parse time string
            val calendar = parseTimeString(timeString) ?: return
            
            // Create intent for the alarm
            val intent = Intent(context, PillReminderReceiver::class.java)
            intent.action = ACTION_PILL_REMINDER
            intent.putExtra("pill_name", pillName)
            intent.putExtra("pill_dosage", pillDosage)
            intent.putExtra("pill_index", pillIndex)
            
            // Create pending intent with unique request code
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                pillIndex,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Schedule exact alarm for better reliability
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                // Set repeating alarm for daily notifications
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            // Silent fail
        }
    }
    
    private fun parseTimeString(timeString: String): Calendar? {
        return try {
            // Remove extra spaces and convert to standard format
            val cleanTimeString = timeString.trim().uppercase()
            
            // Parse formats like "8:00 AM", "2:00 PM", "14:00", etc.
            val format = if (cleanTimeString.contains("AM") || cleanTimeString.contains("PM")) {
                SimpleDateFormat("h:mm a", Locale.US)
            } else {
                SimpleDateFormat("H:mm", Locale.US)
            }
            
            val time = format.parse(cleanTimeString) ?: return null
            val calendar = Calendar.getInstance()
            calendar.time = time
            
            // Set to today's date
            val today = Calendar.getInstance()
            calendar.set(Calendar.YEAR, today.get(Calendar.YEAR))
            calendar.set(Calendar.MONTH, today.get(Calendar.MONTH))
            calendar.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH))
            
            // If the time has already passed today, set for tomorrow
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            
            calendar
        } catch (e: Exception) {
            null
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