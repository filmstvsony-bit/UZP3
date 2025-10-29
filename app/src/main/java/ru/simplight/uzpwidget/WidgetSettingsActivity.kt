
package ru.simplight.uzpwidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class WidgetSettingsActivity : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.widget_settings)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        val baseUrlField = findViewById<EditText>(R.id.editBaseUrl)
        val loginField = findViewById<EditText>(R.id.editLogin)
        val passwordField = findViewById<EditText>(R.id.editPassword)
        val tag1Field = findViewById<EditText>(R.id.editTag1)
        val tag2Field = findViewById<EditText>(R.id.editTag2)
        val tag3Field = findViewById<EditText>(R.id.editTag3)
        val transparencySeek = findViewById<SeekBar>(R.id.transparencySeek)
        val transparencyLabel = findViewById<TextView>(R.id.transparencyLabel)
        val textSizeSeek = findViewById<SeekBar>(R.id.textSizeSeek)
        val textSizeLabel = findViewById<TextView>(R.id.textSizeLabel)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val prefs = PrefsHelper

        baseUrlField.setText(prefs.getString(this, "baseUrl_${appWidgetId}", "https://217.114.30.47:49001"))
        loginField.setText(prefs.getString(this, "login_${appWidgetId}", "scada"))
        passwordField.setText(prefs.getString(this, "password_${appWidgetId}", "Uzp499111!!"))
        tag1Field.setText(prefs.getString(this, "tag1_${appWidgetId}", "1729426455"))
        tag2Field.setText(prefs.getString(this, "tag2_${appWidgetId}", "1729426456"))
        tag3Field.setText(prefs.getString(this, "tag3_${appWidgetId}", "1729426457"))

        val savedTransparency = prefs.getInt(this, "transparency_${appWidgetId}", 150)
        val savedTextSize = prefs.getInt(this, "textSize_${appWidgetId}", 14)
        transparencySeek.progress = savedTransparency
        textSizeSeek.progress = savedTextSize
        transparencyLabel.text = "Прозрачность: ${100 - (savedTransparency * 100 / 255)}%"
        textSizeLabel.text = "Размер текста: ${savedTextSize}sp"

        transparencySeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, f: Boolean) {
                transparencyLabel.text = "Прозрачность: ${100 - (p * 100 / 255)}%"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        textSizeSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, f: Boolean) {
                textSizeLabel.text = "Размер текста: ${p}sp"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        btnSave.setOnClickListener {
            prefs.clearForWidget(this, appWidgetId)
            PrefsHelper.putString(this, "baseUrl_$appWidgetId", baseUrlField.text.toString().trim())
            PrefsHelper.putString(this, "login_$appWidgetId", loginField.text.toString().trim())
            PrefsHelper.putString(this, "password_$appWidgetId", passwordField.text.toString())
            PrefsHelper.putString(this, "tag1_$appWidgetId", tag1Field.text.toString().trim())
            PrefsHelper.putString(this, "tag2_$appWidgetId", tag2Field.text.toString().trim())
            PrefsHelper.putString(this, "tag3_$appWidgetId", tag3Field.text.toString().trim())
            PrefsHelper.putInt(this, "transparency_$appWidgetId", transparencySeek.progress)
            PrefsHelper.putInt(this, "textSize_$appWidgetId", textSizeSeek.progress)


            val result = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, result)
            val manager = AppWidgetManager.getInstance(this)
            MySimpLightWidget.updateAppWidget(this, manager, appWidgetId)
            finish()
        }
    }
}
