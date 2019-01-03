package com.majewski.hivemindcolorchangingapp.logic

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.majewski.hivemindbt.client.ClientCallbacks
import com.majewski.hivemindbt.client.HivemindBtClient
import com.majewski.hivemindbt.data.ReceivedElement
import com.majewski.hivemindcolorchangingapp.ColorCodes
import io.reactivex.subjects.PublishSubject

class RemoteScreenLogic {

    var client: HivemindBtClient? = null

    val communicationBus = PublishSubject.create<Event>()

    fun initializeBtClient(context: Context) {
        client = HivemindBtClient(context, clientCallbacks)
        client?.addData("colorClient", 0)
        client?.startScan()
    }

    private val clientCallbacks = object : ClientCallbacks {

        var clientId: Byte = 0

        override fun onServerFound(device: BluetoothDevice) {
            client?.stopScan()
            client?.connectDevice(device)
        }

        override fun onConnectedToServer(clientId: Byte) {
            communicationBus.onNext(Event(Event.Type.CONNECTED_TO_SERVER))
            this.clientId = clientId
        }

        override fun onNumberOfClientsChanged(newNumberOfClients: Byte) {
        }

        override fun onDataChanged(data: ReceivedElement) {
            when (data.name) {
                "colorClient" -> {
                    if (data.data[0] == clientId) {
                        communicationBus.onNext(Event(Event.Type.DISPLAY_COLOR, data.data[1]))
                    } else {
                        communicationBus.onNext(Event(Event.Type.DISPLAY_COLOR, ColorCodes.WHITE))
                    }
                }
            }
        }
    }

    class Event(
        val type: Type,
        val data: Any? = null
    ) {
        enum class Type {
            CONNECTED_TO_SERVER,
            DISPLAY_COLOR
        }
    }
}