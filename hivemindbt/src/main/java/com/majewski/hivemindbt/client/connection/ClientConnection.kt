package com.majewski.hivemindbt.client.connection

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import com.majewski.hivemindbt.Uuids
import com.majewski.hivemindbt.client.ClientCallbacks
import com.majewski.hivemindbt.data.SharedData
import java.util.*
import kotlin.collections.HashMap

internal class ClientConnection(private val mContext: Context,
                       private val clientData: SharedData,
                       private val clientCallbacks: ClientCallbacks?) {

    // bluetooth variables
    private val mBluetoothAdapter = (mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val mBluetoothLeScanner: BluetoothLeScanner by lazy {
        mBluetoothAdapter.bluetoothLeScanner
    }
    private var mGatt: BluetoothGatt? = null

    private val mScanResults = HashMap<String, BluetoothDevice>()
    private val mScanCallback = BleScanCallback(mScanResults) { clientCallbacks?.onServerFound(it)}
    private var gattClientCallback = GattClientCallback(clientData, clientCallbacks)

    private var mScanning = false

    fun startScan(time: Long = 10000) {
        if(mScanning) {
            return
        }

        val filters = ArrayList<ScanFilter>()
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(Uuids.SERVICE_PRIMARY))
            .build()
        filters.add(scanFilter)
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()

        mScanResults.clear()
        Log.d("HivemindClient", "attempting to start scan")


        mBluetoothLeScanner.startScan(filters, settings, mScanCallback)
        mScanning = true
        Handler().postDelayed({stopScan()}, time)
    }

    fun stopScan() {
        Log.d("HivemindClient", "Scan ended")
        if(mScanning) {
            mBluetoothLeScanner.stopScan(mScanCallback)
            mScanning = false
        }
    }

    fun connectDevice(device: BluetoothDevice) {
        stopScan()
        Log.d("HivemindClient", "Connecting device")
        mGatt = device.connectGatt(mContext, false, gattClientCallback)
    }

    fun sendData(data: ByteArray, elementId: Byte) {
        mGatt?.let {
            val characteristic = it
                .getService(Uuids.SERVICE_PRIMARY)
                .getCharacteristic(UUID(0L, Uuids.CHARACTERISTIC_READ_DATA.leastSignificantBits + clientData.clientId))

            characteristic.value = byteArrayOf(clientData.clientId, elementId).plus(data)
            it.writeCharacteristic(characteristic)
        }
    }
}