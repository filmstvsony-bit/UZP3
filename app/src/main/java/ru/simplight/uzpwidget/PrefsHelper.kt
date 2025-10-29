package ru.simplight.uzpwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Вспомогательный объект для сохранения и загрузки настроек виджета
 * (работает через SharedPreferences)
 */
object PrefsHelper {

    private const val PREFS_NAME = "UZPWidgetPrefs"

    /** Возвращает экземпляр SharedPreferences */
    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // -------------------------------------------------------------
    //  Сохранение / получение значений
    // -------------------------------------------------------------

    fun putString(context: Context, key: String, value: String) {
        prefs(context).edit().putString(key, value).apply()
    }

    fun getString(context: Context, key: String, defaultValue: String = ""): String {
        return prefs(context).getString(key, defaultValue) ?: defaultValue
    }

    fun putInt(context: Context, key: String, value: Int) {
        prefs(context).edit().putInt(key, value).apply()
    }

    fun getInt(context: Context, key: String, defaultValue: Int = 0): Int {
        return prefs(context).getInt(key, defaultValue)
    }

    // -------------------------------------------------------------
    //  Очистка данных
    // -------------------------------------------------------------

    /** Очистка настроек конкретного виджета */
    fun clearForWidget(context: Context, widgetId: Int) {
        val editor = prefs(context).edit()
        editor.remove("baseUrl_$widgetId")
        editor.remove("login_$widgetId")
        editor.remove("password_$widgetId")
        editor.remove("tag1_$widgetId")
        editor.remove("tag2_$widgetId")
        editor.remove("tag3_$widgetId")
        editor.remove("transparency_$widgetId")
        editor.remove("textSize_$widgetId")
        editor.apply()

        Log.d("PrefsHelper", "🧹 Очищены настройки для виджета #$widgetId")
    }

    /** Полная очистка всех сохранённых данных приложения */
    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
        Log.d("PrefsHelper", "🧹 Все настройки сброшены")
    }

    // -------------------------------------------------------------
    //  Автоматическая очистка устаревших записей
    // -------------------------------------------------------------

    /**
     * Удаляет записи для виджетов, которых больше нет на рабочем столе.
     */
    fun autoCleanup(context: Context) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val allWidgets = appWidgetManager.getAppWidgetIds(
                ComponentName(context, MySimpLightWidget::class.java)
            )

            val validIds = allWidgets.toSet()
            val editor = prefs(context).edit()
            val keys = prefs(context).all.keys

            keys.forEach { key ->
                val idPart = key.substringAfterLast("_", "")
                val idValue = idPart.toIntOrNull()

                // Если ID есть и он не входит в список активных — удалить
                if (idValue != null && idValue !in validIds) {
                    editor.remove(key)
                    Log.d("PrefsHelper", "🧽 Удалены старые данные для виджета #$idValue")
                }
            }

            editor.apply()
        } catch (e: Exception) {
            Log.e("PrefsHelper", "Ошибка при автоочистке: ${e.message}")
        }
    }
}
