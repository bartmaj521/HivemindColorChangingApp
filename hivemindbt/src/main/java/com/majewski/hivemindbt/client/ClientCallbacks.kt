package com.majewski.hivemindbt.client

import android.bluetooth.BluetoothDevice
import com.majewski.hivemindbt.data.ReceivedElement

interface ClientCallbacks {

    fun onServerFound(device: BluetoothDevice)

    fun onConnectedToServer(clientId: Byte)

    fun onNumberOfClientsChanged(newNumberOfClients: Byte)

    fun onDataChanged(data: ReceivedElement)
}