package iot2020.slumber.lightsensor

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import iot2020.slumber.lightsensor.bluetooth.BluetoothAlarmHandler

/**
 * Activity that tracks light sensor value, displays it, and sends updates to alarm
 */
class SensingActivity : Activity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null

    private val LOG_TAG = "Sensing"

    private val LIGHTS_ON_THRESHOLD = 50
    private var isLightsOn: Boolean? = null

    private lateinit var valueText: TextView
    private lateinit var statusText: TextView
    private lateinit var lightIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensing)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        valueText = findViewById(R.id.sensor_value)
        statusText = findViewById(R.id.sensor_status)
        lightIcon = findViewById(R.id.img_light)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Necessary override, any reason to care about accuracy?
        Log.i(LOG_TAG, "accuracy: $accuracy")
    }

    /**
     * Called when sensor detects new value
     */
    override fun onSensorChanged(event: SensorEvent) {
        val lightValue = event.values[0].toInt()
        val newIsLightsOn = lightValue >= LIGHTS_ON_THRESHOLD

        valueText.text = String.format(resources.getString(R.string.light_value), lightValue)

        if (newIsLightsOn != isLightsOn) {
            isLightsOn = newIsLightsOn

            updateView(isLightsOn!!)
            sendUpdatedValue(isLightsOn!!)
        }
    }

    /**
     * Update activity view elements based on [isLightsOn] value
     */
    private fun updateView(isLightsOn: Boolean) {
        val text: String
        val iconColor: Int

        if (isLightsOn) {
            text = resources.getString(R.string.lights_on)
            iconColor = ContextCompat.getColor(this, R.color.lights_on)
        } else {
            text = resources.getString(R.string.lights_off)
            iconColor = ContextCompat.getColor(this, R.color.lights_off)
        }
        statusText.text = text
        lightIcon.setColorFilter(iconColor)
    }

    /**
     * Send [isLightsOn] flag to alarm
     */
    private fun sendUpdatedValue(isLightsOn: Boolean) {
        val btHandler = BluetoothAlarmHandler()
        val byteValue = if (isLightsOn) 1.toByte() else 0.toByte()
        btHandler.sendValue(this, byteValue)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    /**
     * Stop sensing when activity is put on pause
     */
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}
