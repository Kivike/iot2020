package iot2020.slumber.lightsensor.bluetooth

import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log

/**
 * Bluetooth Lower Energy (BLE) server that can notify client about illuminance change
 *
 * @see LightSensorProfile
 */
class BleServer() : Service() {

    lateinit var btGattServer: BluetoothGattServer
    lateinit var btManager: BluetoothManager

    private val registeredDevices = mutableSetOf<BluetoothDevice>()

    private val binder = LocalBinder()

    val LOG_TAG = "LightSensorBleServer"

    override fun onCreate() {
        super.onCreate()

        btManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)
        start()
        return result
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
    }

    fun start() {
        Log.i(LOG_TAG, "BLE server started")

        startAdvertising()
        startServer()

        val btIntentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(btReceiver, btIntentFilter)
    }

    fun stop() {
        stopAdvertising()
        stopServer()

        unregisterReceiver(btReceiver)
    }

    /**
     * Notify clients about light status change
     */
    fun notifyChange(isLightsOn: Boolean) {
        Log.i(LOG_TAG, "Notify isLightsOn change")
        if (registeredDevices.isEmpty()) {
            return
        }

        val lightCharacteristic = btGattServer
                .getService(LightSensorProfile.SERVICE_UUID)
                .getCharacteristic(LightSensorProfile.LIGHTS_STATUS_UUID)

        lightCharacteristic.value =
                if (isLightsOn) LightSensorProfile.BYTES_LIGHTS_ON
                else LightSensorProfile.BYTES_LIGHTS_OFF

        for (device in registeredDevices) {
            Log.i(LOG_TAG, "Notify device %s".format(device.address))
            btGattServer.notifyCharacteristicChanged(device, lightCharacteristic, false)
        }
    }

    private fun startAdvertising() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val leAdvertiser = adapter.bluetoothLeAdvertiser

        leAdvertiser.let {
            val settings = AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                    .setConnectable(true)
                    .setTimeout(0)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                    .build()

            val data = AdvertiseData.Builder()
                    .setIncludeDeviceName(false)
                    .setIncludeTxPowerLevel(true)
                    .addServiceUuid(ParcelUuid(LightSensorProfile.SERVICE_UUID))
                    .build()

            it.startAdvertising(settings, data, advertiseCallback)
        }
    }

    private fun stopAdvertising() {
        val leAdvertiser = BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser
        leAdvertiser.stopAdvertising(advertiseCallback)
    }

    private fun startServer() {
        btGattServer = btManager.openGattServer(this, gattServerCallback)
        btGattServer.addService(LightSensorProfile.createLightSensorService())

        Log.i(LOG_TAG, "GATT server started")
    }

    private fun stopServer() {
        btGattServer.close()

        Log.i(LOG_TAG, "GATT server stopped")
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            Log.e(LOG_TAG, "BLE advertise failed with error code $errorCode")
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.i(LOG_TAG, "BLE advertise started")
        }
    }

    /**
     * Handle device registration
     *
     * Forces single client mode
     */
    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            Log.i(LOG_TAG, "Connection state change")

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(LOG_TAG, "Client connected (${device.name})")

                    broadcastUpdate(LightSensorProfile.ACTION_CLIENT_CONNECTED)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(LOG_TAG, "Client disconnected")

                    if (registeredDevices.contains(device)) {
                        registeredDevices.remove(device)
                    }
                    broadcastUpdate(LightSensorProfile.ACTION_CLIENT_DISCONNECTED)
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    Log.i(LOG_TAG, "CLient connecting")
                }
                BluetoothProfile.STATE_DISCONNECTING -> {
                    Log.i(LOG_TAG, "Client disconnecting")
                }
            }
        }

        override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic) {
            Log.i(LOG_TAG, "Read characteristic %s".format(characteristic.uuid.toString()))
        }
        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            Log.i(LOG_TAG, "Write characteristic %s".format(characteristic.uuid.toString()))
        }

        override fun onDescriptorReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor) {
            Log.i(LOG_TAG, "Read descriptor %s".format(descriptor.uuid.toString()))

        }

        /**
         * Handle enabling/disabling of the light sensor
         */
        override fun onDescriptorWriteRequest(
                device: BluetoothDevice,
                requestId: Int,
                descriptor: BluetoothGattDescriptor,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray
        ) {
            Log.i(LOG_TAG, "Write descriptor %s".format(descriptor.uuid.toString()))

            if (LightSensorProfile.CONFIG_UUID == descriptor.uuid) {
                if (BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE contentEquals value) {
                    registeredDevices.add(device)
                    broadcastUpdate(LightSensorProfile.ACTION_SENSOR_ENABLE)
                } else if (BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE contentEquals value) {
                    registeredDevices.remove(device)
                    broadcastUpdate(LightSensorProfile.ACTION_SENSOR_DISABLE)
                }
                if (responseNeeded) {
                    btGattServer.sendResponse(
                            device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            null
                    )
                }
            } else {
                btGattServer.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null
                )
            }
        }
    }

    /**
     * Stop server when bluetooth is disabled
     */
    private val btReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)) {
                BluetoothAdapter.STATE_OFF -> {
                    Log.i(LOG_TAG, "Bluetooth disabled")
                    stop()
                }
                BluetoothAdapter.STATE_ON -> {
                    Log.i(LOG_TAG, "Bluetooth enabled")
                    start()
                }
            }
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): BleServer = this@BleServer
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }
}