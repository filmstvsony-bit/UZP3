
package ru.simplight.uzpwidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import kotlinx.coroutines.*
import okhttp3.Credentials
import okhttp3.Request
import org.json.JSONObject

class MySimpLightWidget : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleUpdates(context)
        PrefsHelper.autoCleanup(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (id in appWidgetIds) updateAppWidget(context, appWidgetManager, id)
        scheduleUpdates(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (id in appWidgetIds) PrefsHelper.clearForWidget(context, id)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context); PrefsHelper.clearAll(context)
        cancelUpdates(context)
    }

    private fun scheduleUpdates(context: Context) {
        val intent = Intent(context, MySimpLightWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        val pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 30_000L, 30_000L, pi)
    }

    private fun cancelUpdates(context: Context) {
        val intent = Intent(context, MySimpLightWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        val pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pi)
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_simplight_single_column)

            val baseUrl = PrefsHelper.getString(context, "baseUrl_%d".format(appWidgetId), "https://217.114.30.47:49001")
            val login = PrefsHelper.getString(context, "login_%d".format(appWidgetId), "scada")
            val password = PrefsHelper.getString(context, "password_%d".format(appWidgetId), "Uzp499111!!")
            val tag1 = PrefsHelper.getString(context, "tag1_%d".format(appWidgetId), "1729426455")
            val tag2 = PrefsHelper.getString(context, "tag2_%d".format(appWidgetId), "1729426456")
            val tag3 = PrefsHelper.getString(context, "tag3_%d".format(appWidgetId), "1729426457")
            val transparency = PrefsHelper.getInt(context, "transparency_%d".format(appWidgetId), 150)
            val textSize = PrefsHelper.getInt(context, "textSize_%d".format(appWidgetId), 14)

            views.setInt(R.id.widgetRoot, "setBackgroundColor", Color.argb(transparency, 0, 0, 0))

            val tagIds = listOf(tag1, tag2, tag3)
            val tagViews = listOf(R.id.textTag1, R.id.textTag2, R.id.textTag3)

            CoroutineScope(Dispatchers.IO).launch {
                val client = UnsafeOkHttp.client()
                tagIds.forEachIndexed { index, tagId ->
                    val value = try {
                        val url = "%s/api/live/tags/%s".format(baseUrl, tagId)
                        val request = Request.Builder()
                            .url(url)
                            .header("Authorization", Credentials.basic(login, password))
                            .build()
                        val response = client.newCall(request).execute()
                        val json = JSONObject(response.body?.string() ?: "{}")
                        json.optString("value", "Ошибка")
                    } catch (e: Exception) {
                        Log.e("MySimpLightWidget", "HTTP error: %s".format(e.message))
                        "Ошибка"
                    }
                    val text = value.ifEmpty { "Ошибка" }.replace("№", "")
                    val color = if (text == "Ошибка" || text == "401" || text == "0") Color.YELLOW else Color.GREEN
                    withContext(Dispatchers.Main) {
                        views.setTextViewText(tagViews[index], text)
                        views.setTextColor(tagViews[index], color)
                        views.setFloat(tagViews[index], "setTextSize", textSize.toFloat())
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            }

            val settingsIntent = Intent(context, WidgetSettingsActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val pendingSettings = PendingIntent.getActivity(
                context, appWidgetId, settingsIntent, PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetRoot, pendingSettings)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
