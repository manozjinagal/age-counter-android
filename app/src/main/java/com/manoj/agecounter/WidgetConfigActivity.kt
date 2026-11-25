package com.manoj.agecounter

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView

/**
 * Opens when you add the widget, or long-press it and choose Settings.
 * Pick a style and how see-through the background is, with a live preview.
 */
class WidgetConfigActivity : Activity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var style: WidgetStyle
    private var opacity = Prefs.DEFAULT_OPACITY
    private lateinit var previewWide: FrameLayout
    private lateinit var previewSmall: FrameLayout
    private lateinit var opacityLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        widgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        setResult(RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        setContentView(R.layout.activity_config)
        style = Prefs.style(this, widgetId)
        opacity = Prefs.opacity(this, widgetId)
        previewWide = findViewById(R.id.preview_wide)
        previewSmall = findViewById(R.id.preview_small)
        opacityLabel = findViewById(R.id.opacity_label)

        val group = findViewById<RadioGroup>(R.id.styles)
        WidgetStyle.values().forEachIndexed { i, s ->
            val rb = RadioButton(this).apply {
                id = View.generateViewId()
                text = s.title
                textSize = 16f
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(12, 18, 12, 18)
                isChecked = s == style
                setOnClickListener { style = s; refresh() }
            }
            group.addView(rb)
        }

        val seek = findViewById<SeekBar>(R.id.opacity)
        seek.progress = opacity
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, value: Int, fromUser: Boolean) { opacity = value; refresh() }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        val birthButton = findViewById<Button>(R.id.set_birth)
        birthButton.setOnClickListener { BirthPicker.show(this) { refresh() } }

        findViewById<Button>(R.id.save).setOnClickListener {
            if (Prefs.birth(this) == null) { BirthPicker.show(this) { refresh() }; return@setOnClickListener }
            Prefs.setWidget(this, widgetId, style, opacity)
            AppWidgetManager.getInstance(this).updateAppWidget(widgetId, AgeWidgetProvider.build(this, widgetId))
            AgeWidgetProvider.updateAll(this)
            setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
            finish()
        }

        if (Prefs.birth(this) == null) BirthPicker.show(this) { refresh() }
        refresh()
    }

    private fun refresh() {
        opacityLabel.text = getString(R.string.background_opacity, opacity)
        findViewById<Button>(R.id.set_birth).visibility = if (Prefs.birth(this) == null) View.VISIBLE else View.GONE
        show(previewWide, "wide")
        show(previewSmall, "small")
    }

    private fun show(frame: FrameLayout, size: String) {
        val view = AgeWidgetProvider.views(this, style, opacity, size).apply(this, frame)
        frame.removeAllViews()
        frame.addView(view)
    }
}
