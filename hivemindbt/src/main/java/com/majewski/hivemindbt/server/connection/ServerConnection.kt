package com.majewski.hivemindbt.server.connection

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.Intent
import android.os.ParcelUuid
import android.util.Log
import com.majewski.hivemindbt.Uuids
import com.majewski.hivemindbt.data.SharedData
import com.majewski.hivemindbt.server.ServerCallbacks
import java.util.*

internal class ServerConnection(private val mContext: Context,
                       mServerData: SharedData,
                       private val mServerCallbacks: ServerCallbacks?) {

    // Bluetooth variables
    private val mBluetoothManager = mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val mBluetoothAdapter = mBluetoothManager.adapter
    private lateinit var mBluetoothAdvertiser: BluetoothLeAdvertiser
    private lateinit var mGattServer: BluetoothGattServer

    private val maxNbOfClients = 2

    private val mConnectedDevices = ArrayList<BluetoothDevice>()
    private val mClientsAddresses = HashMap<String, Byte>()

    private val gattServerCallback = GattServerCallback(mConnectedDevices, mClientsAddresses, mServerData, mServerCallbacks)

    private val mAdvertiseCallback = object: AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d("AdvertiseCallback", "Peripheral advertising started.")
            mServerCallbacks?.onServerStarted()
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.e("AdvertiseCallback", "Peripheral advertising failed: $errorCode")
            mServerCallbacks?.onServerFailed(errorCode)
        }
    }

    fun startServer() {
        mBluetoothAdvertiser = mBluetoothAdapter.bluetoothLeAdvertiser
        mGattServer = mBluetoothManager.openGattServer(mContext, gattServerCallback)
        gattServerCallback.gattServer = mGattServer
        setupServer()
        startAdvertising()
    }

    fun stopServer() {
        mBluetoothAdvertiser.stopAdvertising(mAdvertiseCallback)
        mGattServer.close()
    }

    fun sendData(data: ByteArray, elementId: Byte) {
        val characteristic = mGattServer
            .getService(Uuids.SERVICE_PRIMARY)
            .getCharacteristic(Uuids.CHARACTERISTIC_READ_DATA)

        characteristic.value = byteArrayOf(0, elementId).plus(data)
        Log.d("HivemindServer", "Characteristic value set.")

        val devices = mConnectedDevices.filter{ mClientsAddresses.keys.contains(it.address) }

        for(device in devices) {
            mGattServer.notifyCharacteristicChanged(device,characteristic, false)
            Log.d("HivemindServer", "Notifying device ${device.name}")
        }
    }

    private fun setupServer() {
        val service = BluetoothGattService(Uuids.SERVICE_PRIMARY, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val clientIdCharacteristic = BluetoothGattCharacteristic(
            Uuids.CHARACTERISTIC_CLIENT_ID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        val nbOfClientsCharacteristic = BluetoothGattCharacteristic(
            Uuids.CHARACTERISTIC_NB_OF_CLIENTS,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        service.addCharacteristic(nbOfClientsCharacteristic)
        service.addCharacteristic(clientIdCharacteristic)

        val dataReadCharacteristic = BluetoothGattCharacteristic(
            Uuids.CHARACTERISTIC_READ_DATA,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        service.addCharacteristic(dataReadCharacteristic)

        for(i in 1..maxNbOfClients) {
            val dataWriteCharacteristic = BluetoothGattCharacteristic(
                UUID(0L, Uuids.CHARACTERISTIC_READ_DATA.leastSignificantBits + i),
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
            )
            service.addCharacteristic(dataWriteCharacteristic)
        }
        mGattServer.addService(service)
    }

    private fun startAdvertising() {
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
            .build()
        val parcelUUID = ParcelUuid(Uuids.SERVICE_PRIMARY)
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(parcelUUID)
            .build()
        mBluetoothAdvertiser.startAdvertising(settings, data, mAdvertiseCallback)
        Log.d("HivemindServer", "Started server")
    }
}