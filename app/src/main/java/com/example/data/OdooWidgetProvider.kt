package com.example.data

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OdooWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val repository = AttendanceRepository(context)
        CoroutineScope(Dispatchers.IO).launch {
            val config = repository.getConfig()
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, config.isCheckedIn)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE_ATTENDANCE) {
            val repository = AttendanceRepository(context)
            CoroutineScope(Dispatchers.IO).launch {
                val config = repository.getConfig()
                val targetAction = if (config.isCheckedIn) "CHECK_OUT" else "CHECK_IN"
                
                repository.performOdooAttendance(
                    actionType = targetAction,
                    method = "WIDGET",
                    onSuccess = { _ -> },
                    onFailure = { _ -> }
                )
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE_ATTENDANCE = "com.example.ACTION_TOGGLE_ATTENDANCE"

        fun updateWidget(context: Context, isCheckedIn: Boolean) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, OdooWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (widgetId in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId, isCheckedIn)
            }
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            isCheckedIn: Boolean
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            if (isCheckedIn) {
                views.setTextViewText(R.id.widget_status, "Status: TER-ABSEN (IN)")
                views.setTextColor(R.id.widget_status, Color.parseColor("#00FFF0"))
                views.setTextViewText(R.id.widget_toggle_button, "TAP CHECK-OUT")
                views.setInt(R.id.widget_toggle_button, "setBackgroundResource", R.drawable.widget_button_bg_red)
            } else {
                views.setTextViewText(R.id.widget_status, "Status: TER-CHECKOUT")
                views.setTextColor(R.id.widget_status, Color.parseColor("#FF5252"))
                views.setTextViewText(R.id.widget_toggle_button, "TAP CHECK-IN")
                views.setInt(R.id.widget_toggle_button, "setBackgroundResource", R.drawable.widget_button_bg)
            }

            val intent = Intent(context, OdooWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_ATTENDANCE
            }
            
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                flags
            )

            views.setOnClickPendingIntent(R.id.widget_toggle_button, pendingIntent)
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
