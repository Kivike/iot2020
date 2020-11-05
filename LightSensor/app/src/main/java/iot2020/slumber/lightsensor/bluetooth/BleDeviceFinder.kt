package iot2020.slumber.lightsensor.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Handler

class BleDeviceFinder (btAdapter: BluetoothAdapter) {

    private val btScanner = btAdapter.bluetoothLeScanner
    private var isScanning = false
    private val SCAN_PERIOD: Long = 10000
    private val handler = Handler()

    private lateinit var resultCallback: ((d: BluetoothDevice) -> Unit)

    /**
     * Start scanning devices, call [resultCallback] when the alarm device is found
     */
    fun scan(resultCallback: (d: BluetoothDevice) -> Unit) {
        this.resultCallback = resultCallback

        if (!isScanning) {
            handler.postDelayed({
                isScanning = false
                btScanner.stopScan(scanCallback)
            }, SCAN_PERIOD)
            isScanning = true
            btScanner.startScan(scanCallback)
        } else {
            isScanning = false
            btScanner.stopScan(scanCallback)
        }
    }

    private val scanCallback: ScanCallback = object : ScanCallback() {
        /**
         * Called for every BTE device found
         */
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            if (result != null) {
                //TODO: Select correct device
                //Simplest way would be to check for specific MAC address (result.device.address)
                //resultCallback(result.device)
            }
        }
    }
}
