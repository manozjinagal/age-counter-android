package com.manoj.agecounter

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.view.Choreographer
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import java.text.DateFormat
import java.util.Date

/**
 * Set your date and time of birth, see the counter at full speed (with milliseconds),
 * and add the widget. Also opens when you add the widget, if no birthday is set yet.
 */
class MainActivity : Activity() {

    private lateinit var counter: TextView
    private lateinit var birthLabel: TextView
    private var running = false

    private val frame = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!running) return
            drawCounter()
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        counter = findViewById(R.id.counter)
        birthLabel = findViewById(R.id.birth_label)

        findViewById<Button>(R.id.set_birth).setOnClickListener { BirthPicker.show(this) { showBirth() } }

        val addButton = findViewById<Button>(R.id.add_widget)
        val manager = AppWidgetManager.getInstance(this)
        if (manager.isRequestPinAppWidgetSupported) {
            addButton.setOnClickListener {
                manager.requestPinAppWidget(ComponentName(this, AgeWidgetProvider::class.java), null, null)
            }
        } else {
            addButton.visibility = View.GONE
        }

        val progressSwitch = findViewById<Switch>(R.id.show_progress)
        progressSwitch.isChecked = Prefs.showProgress(this)
        progressSwitch.setOnCheckedChangeListener { _, on ->
            Prefs.setShowProgress(this, on)
            AgeWidgetProvider.updateAll(this)
        }

        showBirth()
        if (Prefs.birth(this) == null) BirthPicker.show(this) { showBirth() }
    }

    override fun onResume() {
        super.onResume()
        running = true
        Choreographer.getInstance().postFrameCallback(frame)
    }

    override fun onPause() {
        super.onPause()
        running = false
    }

    private fun drawCounter() {
        val birth = Prefs.birth(this)
        val now = System.currentTimeMillis()
        if (birth == null || birth >= now) {
            counter.text = "-- : -- : -- : -- : -- : -- . ---"
            return
        }
        val p = AgeMath.parts(birth, now)
        val since = now - p.dayStartMillis
        val h = since / 3_600_000
        val m = since / 60_000 % 60
        val s = since / 1_000 % 60
        val ms = since % 1_000
        counter.text = "${AgeMath.two(p.years)} : ${AgeMath.two(p.months)} : ${AgeMath.two(p.days)} : " +
            "${AgeMath.two(h)} : ${AgeMath.two(m)} : ${AgeMath.two(s)} . ${ms.toString().padStart(3, '0')}"
    }

    private fun showBirth() {
        val birth = Prefs.birth(this)
        birthLabel.text = if (birth == null) getString(R.string.no_birth)
        else getString(R.string.born_on, DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(Date(birth)))
    }
}
