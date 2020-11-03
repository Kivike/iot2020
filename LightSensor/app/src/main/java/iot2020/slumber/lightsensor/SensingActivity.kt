package iot2020.slumber.lightsensor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import iot2020.slumber.lightsensor.bluetooth.BluetoothAlarmHandler

class SensingActivity : Activity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null

    private val LOG_TAG = "Sensing"

    private val LIGHTS_ON_THRESHOLD = 50
    private var isLightsOn = true

    private lateinit var valueText: TextView
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensing)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        valueText = findViewById(R.id.sensor_value)
        statusText = findViewById(R.id.sensor_status)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i(LOG_TAG, "accuracy: $accuracy")
    }

    override fun onSensorChanged(event: SensorEvent) {
        val lightValue = event.values[0]
        val newIsLightsOn = lightValue >= LIGHTS_ON_THRESHOLD

        @SuppressLint("SetTextI18n")
        valueText.text = event.values[0].toString() + " lx"

        if (newIsLightsOn != isLightsOn) {
            isLightsOn = newIsLightsOn

            if (isLightsOn) {
                statusText.text = resources.getString(R.string.lights_on)
            } else {
                statusText.text = resources.getString(R.string.lights_off)
            }
            sendUpdatedValue(isLightsOn)
        }
    }

    private fun sendUpdatedValue(value: Boolean) {
        val btHandler = BluetoothAlarmHandler()
        val byteValue = if (value) 1.toByte() else 0.toByte()
        btHandler.sendValue(this, byteValue)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}
