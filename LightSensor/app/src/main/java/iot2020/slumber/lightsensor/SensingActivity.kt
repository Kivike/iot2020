package iot2020.slumber.lightsensor

import android.app.Activity
import android.content.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import iot2020.slumber.lightsensor.bluetooth.BleServer
import iot2020.slumber.lightsensor.bluetooth.LightSensorProfile

/**
 * Activity that tracks light sensor value, displays it, and sends updates to alarm
 */
class SensingActivity : Activity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null

    private val LOG_TAG = "Sensing"

    /**
     * How much light needs to be detected at minimum for lights
     * to be considered on
     */
    private val LIGHTS_ON_THRESHOLD_LX = 50

    /**
     * How big change sensor value needs to have between two sensor events
     * for the change to be considered quick (not gradual)
     */
    private val LIGHTS_CHANGE_THRESHOLD_LX = 20

    /**
     * How long to wait before sending lights off signal
     * Used to prevent brief blocking of the sensor from turning on the alarm
     */
    private val LIGHTS_OFF_WAIT_MILLIS: Long = 3000

    private var lastIsLightsOn: Boolean = false
    private var lastLightValue: Int? = null
    private var pendingOffSignal: Boolean = false

    private lateinit var valueText: TextView
    private lateinit var statusText: TextView

    private lateinit var lightIcon: ImageView

    private lateinit var bleServer: BleServer
    private var bleServiceBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensing)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        valueText = findViewById(R.id.sensor_value)
        statusText = findViewById(R.id.sensor_status)
        lightIcon = findViewById(R.id.img_light)

        Intent(this, BleServer::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        val intentFilter = IntentFilter(LightSensorProfile.ACTION_SENSOR_DISABLE)
        registerReceiver(btReceiver, intentFilter)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            Log.i(LOG_TAG, "BLE service connected")
            val binder = service as BleServer.LocalBinder
            bleServer = binder.getService()
            bleServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bleServiceBound = false
        }
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
        valueText.text = String.format(resources.getString(R.string.light_value), lightValue)

        if (lastLightValue == null) {
            // Initialize values on first sensor event
            lastLightValue = lightValue
            lastIsLightsOn = false
            return
        }

        val newIsLightsOn = lightValue >= LIGHTS_ON_THRESHOLD_LX
        var updateValue = false

        if ((!newIsLightsOn && lastIsLightsOn)) {
            if (!pendingOffSignal) {
                pendingOffSignal = true

                Handler().postDelayed({
                    if (pendingOffSignal) {
                        updateValue(newIsLightsOn)
                    }
                }, LIGHTS_OFF_WAIT_MILLIS)
            }
        } else if (newIsLightsOn) {
            if (!lastIsLightsOn) {
                val isChangeQuick = lightValue - lastLightValue!! > LIGHTS_CHANGE_THRESHOLD_LX

                if (isChangeQuick) {
                    updateValue = true
                }
            }
            pendingOffSignal = false
        }

        if (updateValue) {
            updateValue(newIsLightsOn)
        }

        lastLightValue = lightValue
    }

    private fun updateValue(isLightsOn: Boolean) {
        lastIsLightsOn = isLightsOn
        updateView(isLightsOn)
        sendUpdatedValue(isLightsOn)
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
        if (bleServiceBound) {
            bleServer.notifyChange(isLightsOn)
        }
    }

    override fun onResume() {
        super.onResume()
        startSensor()
    }

    /**
     * Stop sensing when activity is put on pause
     */
    override fun onPause() {
        super.onPause()
        stopSensor()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(btReceiver)
        unbindService(serviceConnection)
    }

    private fun startSensor() {
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun stopSensor() {
        sensorManager.unregisterListener(this)
    }

    private val btReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                LightSensorProfile.ACTION_SENSOR_DISABLE -> {
                    finish()
                }
            }
        }
    }
}
