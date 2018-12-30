package com.majewski.hivemindbt.server.connection

import android.bluetooth.*
import android.util.Log
import com.majewski.hivemindbt.Uuids
import com.majewski.hivemindbt.data.ReceivedElement
import com.majewski.hivemindbt.data.SharedData
import com.majewski.hivemindbt.server.ServerCallbacks
import java.util.*

internal class GattServerCallback(private val mConnectedDevices: ArrayList<BluetoothDevice>,
                         private val mClientsAddresses: HashMap<String, Byte>,
                         private val mServerData: SharedData,
                         private val mServerCallbacks: ServerCallbacks?) : BluetoothGattServerCallback() {

    private val maxNumberOfClients = 2

    var gattServer: BluetoothGattServer? = null

    private var nbOfClients: Byte = 0

    override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
        super.onConnectionStateChange(device, status, newState)
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            mConnectedDevices.add(device)
            Log.d("HivemindServer", "Device connected")
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            mConnectedDevices.remove(device)
            Log.d("HivemindServer", "Device disconnected")
            val id = mClientsAddresses[device.address]
            id?.let{
                mServerCallbacks?.onClientDisconnected(id)
            }
        }
    }

    override fun onCharacteristicReadRequest(
        device: BluetoothDevice?,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic?
    ) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

        if(characteristic?.uuid == Uuids.CHARACTERISTIC_CLIENT_ID) {
            device?.let {
                if (mClientsAddresses[device.address] == null) {
                    addNewClient(device)
                }
                Log.d("HivemindServer", "ID ReadRequest, id: ${mClientsAddresses[device.address]}")
                gattServer?.sendResponse(device, requestId, 0, 0, byteArrayOf(mClientsAddresses[device.address] ?: 0))
            }
        }
    }

    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic?,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        super.onCharacteristicWriteRequest(
            device,
            requestId,
            characteristic,
            preparedWrite,
            responseNeeded,
            offset,
            value
        )
        Log.d("HivemindServer", "Write request")

        gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)

        characteristic?.let {
            val clientId = characteristic.uuid.leastSignificantBits - Uuids.CHARACTERISTIC_READ_DATA.leastSignificantBits
            if(clientId in 1..maxNumberOfClients) {
                Log.d("HivemindServer", "Received: ${value?.get(0)}")

                val characteristicSendData = gattServer
                    ?.getService(Uuids.SERVICE_PRIMARY)
                    ?.getCharacteristic(Uuids.CHARACTERISTIC_READ_DATA)

                characteristicSendData?.value = value
                Log.d("HivemindServer", "Characteristic value set.")

                val devices = mConnectedDevices.filter{ mClientsAddresses.keys.contains(it.address) && it != device }

                for(device in devices) {
                    gattServer?.notifyCharacteristicChanged(device,characteristicSendData, false)
                    Log.d("HivemindServer", "Notifying device ${device.name}")
                }
                value?.let{
                    val recv = ReceivedElement(value[0], value[1],mServerData.getElementName(value[1]) ,value.copyOfRange(2, value.size))
                    mServerData.setElementValueFromClient(recv.dataId, recv.from, recv.data)
                    mServerCallbacks?.onDataChanged(recv)
                }
            }
        }
    }

    override fun onDescriptorWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        descriptor: BluetoothGattDescriptor?,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value)
        Log.d("HivemindServer", "Descriptor write request: ${value?.get(0)}")
        descriptor?.value = value
        gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
    }

    private fun addNewClient(device: BluetoothDevice) {
        nbOfClients++
        mClientsAddresses[device.address] = nbOfClients
        val nbOfClientsCharacteristic = gattServer?.getService(Uuids.SERVICE_PRIMARY)?.getCharacteristic(Uuids.CHARACTERISTIC_NB_OF_CLIENTS)
        nbOfClientsCharacteristic?.value = byteArrayOf(nbOfClients)
        Log.d("HivemindServer", "Number of connected devices: ${mConnectedDevices.size}")
        for(d in mConnectedDevices) {
            Log.d("HivemindServer", "Notifying device ${d.name}")
            gattServer?.notifyCharacteristicChanged(d, nbOfClientsCharacteristic, false)
        }
        mServerCallbacks?.onClientConnected(nbOfClients)
        Log.d("HivemindServer", "Client connected, number of clients = $nbOfClients")
    }
}