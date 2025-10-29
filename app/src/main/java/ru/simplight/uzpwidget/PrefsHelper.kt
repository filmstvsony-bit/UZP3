
package ru.simplight.uzpwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object PrefsHelper {
    private const val PREFS_NAME = "UZPWidgetPrefs"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun putString(context: Context, key: String, value: String) =
        prefs(context).edit().putString(key, value).apply()

    fun putInt(context: Context, key: String, value: Int) =
        prefs(context).edit().putInt(key, value).apply()

    fun getString(context: Context, key: String, defaultValue: String = ""): String =
        prefs(context).getString(key, defaultValue) ?: defaultValue

    fun getInt(context: Context, key: String, defaultValue: Int = 0): Int =
        prefs(context).getInt(key, defaultValue)

    fun setString(context: Context, key: String, value: String) = putString(context, key, value)
    fun setInt(context: Context, key: String, value: Int) = putInt(context, key, value)

    fun clearForWidget(context: Context, widgetId: Int) {
        val e = prefs(context).edit()
        listOf("baseUrl_","login_","password_","tag1_","tag2_","tag3_","transparency_","textSize_")
            .forEach { p -> e.remove(p + widgetId) }
        e.apply()
        Log.d("PrefsHelper","Cleared prefs for widget #$widgetId")
    }

    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
        Log.d("PrefsHelper","Cleared all prefs")
    }

    fun apply(context: Context) { prefs(context).edit().apply() }

    fun autoCleanup(context: Context) {
        try {
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, MySimpLightWidget::class.java)).toSet()
            val all = prefs(context).all.keys
            val e = prefs(context).edit()
            for (k in all) {
                val id = k.substringAfterLast("_","").toIntOrNull()
                if (id != null && id not in ids) e.remove(k)
            }
            e.apply()
        } catch (t: Throwable) { Log.e("PrefsHelper","autoCleanup: ${t.message}") }
    }
}
