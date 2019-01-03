package com.majewski.hivemindcolorchangingapp


import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.majewski.hivemindbt.client.ClientCallbacks
import com.majewski.hivemindbt.client.HivemindBtClient
import com.majewski.hivemindbt.data.ReceivedElement
import kotlinx.android.synthetic.main.fragment_remote_screen.*


class RemoteScreenFragment : Fragment() {

    var client: HivemindBtClient? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        context?.let {
            client = HivemindBtClient(context, clientCallbacks)
            client?.addData("colorClient", 0)
            client?.startScan()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_remote_screen, container, false)
    }

    private val clientCallbacks = object : ClientCallbacks {

        var clientId: Byte = 0

        override fun onServerFound(device: BluetoothDevice) {
            client?.stopScan()
            client?.connectDevice(device)

        }

        override fun onConnectedToServer(clientId: Byte) {
            activity?.runOnUiThread {
                this.clientId = clientId
                Toast.makeText(context, "Connected to control device", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onNumberOfClientsChanged(newNumberOfClients: Byte) {
        }

        override fun onDataChanged(data: ReceivedElement) {
            activity?.runOnUiThread {
                when (data.name) {
                    "colorClient" -> {
                        if (data.data[0] == clientId) {
                            when (data.data[1]) {
                                RED -> {
                                    Toast.makeText(context, "RED", Toast.LENGTH_SHORT).show()
                                    iv_color_display.setColorFilter(
                                        ResourcesCompat.getColor(
                                            resources,
                                            R.color.red,
                                            null
                                        )
                                    )
                                }
                                GREEN -> {
                                    Toast.makeText(context, "GREEN", Toast.LENGTH_SHORT).show()
                                    iv_color_display.setColorFilter(
                                        ResourcesCompat.getColor(
                                            resources,
                                            R.color.green,
                                            null
                                        )
                                    )
                                }
                                BLUE -> {
                                    Toast.makeText(context, "BLUE", Toast.LENGTH_SHORT).show()
                                    iv_color_display.setColorFilter(
                                        ResourcesCompat.getColor(
                                            resources,
                                            R.color.blue,
                                            null
                                        )
                                    )
                                }
                                MAGENTA -> {
                                    Toast.makeText(context, "MAGENTA", Toast.LENGTH_SHORT).show()
                                    iv_color_display.setColorFilter(
                                        ResourcesCompat.getColor(
                                            resources,
                                            R.color.magenta,
                                            null
                                        )
                                    )
                                }
                            }
                        } else {
                            iv_color_display.setColorFilter(
                                ResourcesCompat.getColor(
                                    resources,
                                    R.color.white,
                                    null
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val RED: Byte = 1
        const val GREEN: Byte = 2
        const val BLUE: Byte = 3
        const val MAGENTA: Byte = 4
    }
}
