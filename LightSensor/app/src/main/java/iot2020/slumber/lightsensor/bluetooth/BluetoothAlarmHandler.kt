package iot2020.slumber.lightsensor.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCallback
import android.content.Context


class BluetoothAlarmHandler {

    fun sendValue(context: Context, value: Byte) {
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        val deviceFinder = BleDeviceFinder(btAdapter)

        deviceFinder.scan { alarmDevice ->
            val btGatt = alarmDevice.connectGatt(context, false, gattCallback)
            // TODO send data
        }
    }

    private val gattCallback = object: BluetoothGattCallback() {
        //TODO
    }
}
