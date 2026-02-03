package com.braintrainer.app.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.braintrainer.app.MainActivity
import com.braintrainer.app.R

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        
        // Create Channel (Safe to call repeatedly)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                context.getString(R.string.notification_channel_name),
                context.getString(R.string.notification_channel_desc),
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = context.getString(R.string.notification_channel_desc)
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create Intent for tap
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context, 0, tapIntent, android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build Notification
        val builder = androidx.core.app.NotificationCompat.Builder(context, context.getString(R.string.notification_channel_name))
            .setSmallIcon(R.drawable.ic_brain_up)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_content))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            
        notificationManager.notify(1001, builder.build())
    }
}
