package com.majewski.hivemindbt.server

import com.majewski.hivemindbt.data.ReceivedElement

interface ServerCallbacks {

    fun onClientConnected(nbOfClients: Byte)

    fun onClientDisconnected(clientId: Byte)

    fun onDataChanged(data: ReceivedElement)

    fun onServerStarted()

    fun onServerFailed(errorCode: Int)
}