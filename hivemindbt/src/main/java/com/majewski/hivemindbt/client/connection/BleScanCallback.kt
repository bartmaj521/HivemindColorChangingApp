package com.majewski.hivemindbt.client.connection

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log

class BleScanCallback(private val mScanResults: HashMap<String, BluetoothDevice>,
                      private val onDeviceFound: (BluetoothDevice)->Unit): ScanCallback() {

    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        super.onScanResult(callbackType, result)
        result?.let {
            addScanResult(it)
        }
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>?) {
        super.onBatchScanResults(results)

        results?.let {
            for (result in it) {
                addScanResult(result)
            }
        }
    }

    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)
        Log.e("HivemindClient", "BLE scan failed with code $errorCode")
    }

    private fun addScanResult(result: ScanResult) {
        val device = result.device
        val deviceAddress = device.address
        mScanResults[deviceAddress] = device
        Log.d("HivemindClient", "Device found")
        onDeviceFound(device)
    }


}