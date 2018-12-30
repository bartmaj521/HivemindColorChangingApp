package com.majewski.hivemindbt.client

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.majewski.hivemindbt.client.connection.ClientConnection
import com.majewski.hivemindbt.data.SharedData

class HivemindBtClient(context: Context, clientCallbacks: ClientCallbacks? = null) {

    private val clientData = SharedData()
    private val mClientConnection = ClientConnection(context, clientData, clientCallbacks)

    fun startScan() {
        mClientConnection.startScan()
    }

    fun stopScan() {
        mClientConnection.stopScan()
    }

    fun connectDevice(device: BluetoothDevice) {
        mClientConnection.connectDevice(device)
    }

    fun addData(name: String, id: Byte) {
        clientData.addElement(name, id)
    }

    fun sendData(data: ByteArray, elementName: String) {
        val elementId = clientData.getElementId(elementName) ?: throw NoSuchElementException()
        clientData.setElementValue(elementId, data)
        mClientConnection.sendData(data, elementId)
    }

    fun getData(elementName: String, clientId: Byte): ByteArray? {
        return clientData.getElementValue(elementName, clientId)
    }
}