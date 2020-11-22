package iot2020.slumber.lightsensor.bluetooth

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.util.Log
import java.util.*

object LightSensorProfile {

    /**
     * BLE service UUID
     */
    val SERVICE_UUID: UUID = UUID.fromString("e8acc040-2b01-11eb-adc1-0242ac120000")

    /**
     * BLE characteristic UUIDs
     */
    val LIGHTS_STATUS_UUID: UUID = UUID.fromString("e8acc040-2b01-11eb-adc1-0242ac120001")

    /**
     * Not decided by us
     */
    val CONFIG_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    /**
     * BLE characteristic values
     */
    val BYTES_LIGHTS_OFF: ByteArray = byteArrayOf(0x00, 0x00)
    val BYTES_LIGHTS_ON: ByteArray = byteArrayOf(0x00, 0x01)

    /**
     * Broadcast actions
     */
    const val ACTION_CLIENT_CONNECTED = "iot2020.slumber.lightsensor.ACTION_CLIENT_CONNECTED"
    const val ACTION_CLIENT_DISCONNECTED = "iot2020.slumber.lightsensor.ACTION_CLIENT_DISCONNECTED"
    const val ACTION_SENSOR_ENABLE = "iot2020.slumber.lightsensor.ACTION_SENSOR_ENABLE"
    const val ACTION_SENSOR_DISABLE = "iot2020.slumber.lightsensor.ACTION_SENSOR_DISABLE"

    fun createLightSensorService(): BluetoothGattService {
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val lightStatusChar = BluetoothGattCharacteristic(
                LIGHTS_STATUS_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ or
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ
        )

        val configDesc = BluetoothGattDescriptor(CONFIG_UUID,
                BluetoothGattDescriptor.PERMISSION_READ or
                        BluetoothGattDescriptor.PERMISSION_WRITE
        )
        lightStatusChar.addDescriptor(configDesc)

        service.addCharacteristic(lightStatusChar)
        return service
    }
}