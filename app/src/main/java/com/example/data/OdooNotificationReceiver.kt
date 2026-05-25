package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OdooNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val repository = AttendanceRepository(context)
        val action = intent.action

        if (action == ACTION_NOTIF_CHECK_IN || action == ACTION_NOTIF_CHECK_OUT) {
            val targetAction = if (action == ACTION_NOTIF_CHECK_IN) "CHECK_IN" else "CHECK_OUT"
            
            CoroutineScope(Dispatchers.IO).launch {
                repository.performOdooAttendance(
                    actionType = targetAction,
                    method = "LOCK_SCREEN",
                    onSuccess = { msg ->
                        dismissNotification(context, NOTIFICATION_ID_REMINDER)
                        showStatusNotification(context, "Presensi Sukses", msg)
                    },
                    onFailure = { errorMsg ->
                        showStatusNotification(context, "Presensi Gagal", errorMsg)
                    }
                )
            }
        }
    }

    companion object {
        const val ACTION_NOTIF_CHECK_IN = "com.example.ACTION_NOTIF_CHECK_IN"
        const val ACTION_NOTIF_CHECK_OUT = "com.example.ACTION_NOTIF_CHECK_OUT"
        
        const val NOTIFICATION_ID_REMINDER = 999
        const val NOTIFICATION_ID_STATUS = 888
        const val CHANNEL_ID = "odoo_attendance_channel"

        fun dismissNotification(context: Context, id: Int) {
            val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifManager.cancel(id)
        }

        fun showReminderNotification(context: Context, labelText: String) {
            val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Odoo Attendance Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Status and reminder notifications for Odoo"
                }
                notifManager.createNotificationChannel(channel)
            }

            val inIntent = Intent(context, OdooNotificationReceiver::class.java).apply {
                action = ACTION_NOTIF_CHECK_IN
            }
            val outIntent = Intent(context, OdooNotificationReceiver::class.java).apply {
                action = ACTION_NOTIF_CHECK_OUT
            }
            
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val inPendingIntent = PendingIntent.getBroadcast(context, 101, inIntent, flags)
            val outPendingIntent = PendingIntent.getBroadcast(context, 102, outIntent, flags)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Lupa Presensi Odoo?")
                .setContentText(labelText)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_media_play, "CHECK-IN", inPendingIntent)
                .addAction(android.R.drawable.ic_media_pause, "CHECK-OUT", outPendingIntent)

            notifManager.notify(NOTIFICATION_ID_REMINDER, builder.build())
        }

        fun showStatusNotification(context: Context, title: String, message: String) {
            val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Odoo Attendance Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Status and reminder notifications for Odoo"
                }
                notifManager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)

            notifManager.notify(NOTIFICATION_ID_STATUS, builder.build())
        }
    }
}
