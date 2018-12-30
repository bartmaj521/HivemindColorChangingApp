package com.majewski.hivemindbt.server

import android.content.Context
import com.majewski.hivemindbt.data.SharedData
import com.majewski.hivemindbt.server.connection.ServerConnection


class HivemindBtServer(mContext: Context,
                       private val serverCallbacks: ServerCallbacks? = null) {

    private val mServerData = SharedData()
    private val mServerConnection = ServerConnection(mContext, mServerData, serverCallbacks)

    fun startServer() {
        mServerConnection.startServer()
    }

    fun stopServer() {
        mServerConnection.stopServer()
    }

    fun addData(name: String, id: Byte) {
        mServerData.addElement(name, id)
    }

    fun sendData(data: ByteArray, elementName: String) {
        val elementId = mServerData.getElementId(elementName) ?: throw NoSuchElementException()
        mServerData.setElementValue(elementId, data)
        mServerConnection.sendData(data, elementId)
    }

    fun getData(elementName: String, clientId: Byte): ByteArray? {
        return mServerData.getElementValue(elementName, clientId)
    }
}