
package ru.simplight.uzpwidget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            PrefsHelper.autoCleanup(context)
            val manager = AppWidgetManager.getInstance(context)
            val cn = ComponentName(context, MySimpLightWidget::class.java)
            val ids = manager.getAppWidgetIds(cn)
            ids.forEach { MySimpLightWidget.updateAppWidget(context, manager, it) }
        }
    }
}
