package com.majewski.hivemindcolorchangingapp

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.majewski.hivemindbt.HivemindBt
import com.majewski.hivemindbt.client.ClientCallbacks
import com.majewski.hivemindbt.client.HivemindBtClient
import com.majewski.hivemindbt.data.ReceivedElement
import com.majewski.hivemindbt.server.HivemindBtServer
import com.majewski.hivemindbt.server.ServerCallbacks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.random.nextInt

class MainActivity : AppCompatActivity() {

    private var server: HivemindBtServer? = null
    private var client: HivemindBtClient? = null

    private val disposable = CompositeDisposable()
    private val colorButtonClicks = PublishSubject.create<Byte>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_control.setOnClickListener {
            if(!HivemindBt.requestPermissions(this)){
                Toast.makeText(this, "Need permissions", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!HivemindBt.isBluetoothEnabled(this)) {
                HivemindBt.requestEnableBluetooth(this)
                Toast.makeText(this, "Bluetooth needs to be enabled", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            setColorButtonsVisibility(View.VISIBLE)
            setColorScreenVisibility(View.GONE)
            server = HivemindBtServer(this, serverCallbacks)
            server?.addData("colorClient", 0)
            server?.startServer()
        }

        btn_screen.setOnClickListener {
            if(!HivemindBt.requestPermissions(this)){
                Toast.makeText(this, "Need permissions", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!HivemindBt.isBluetoothEnabled(this)) {
                HivemindBt.requestEnableBluetooth(this)
                Toast.makeText(this, "Bluetooth needs to be enabled", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            setColorScreenVisibility(View.VISIBLE)
            setColorButtonsVisibility(View.GONE)
            client = HivemindBtClient(this, clientCallbacks)
            client?.addData("colorClient", 0)
            client?.startScan()
        }

        btn_start.setOnClickListener {

            points = 0
            reactionTimes.clear()

            tv_avg_reaction.text = reactionTimes.average().toString()
            tv_points.text = points.toString()

            var delay = 0L

            Observable.range(0, 10)
                .flatMap {
                    delay += Random.nextLong(2000,5000)
                    Observable.just(it).delay(delay, TimeUnit.MILLISECONDS)
                }.subscribeOn(Schedulers.computation())
                .subscribe {
                    lastColor= Random.nextInt(1..4).toByte()
                    server?.sendData(byteArrayOf(Random.nextInt(1..2).toByte(), lastColor), "colorClient")
                    lastShowTime = SystemClock.elapsedRealtime()
                    fulfilled = true
                }.addTo(disposable)
        }

        colorButtonClicks
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if(it != lastColor) {
                    fulfilled = false
                    Toast.makeText(this, "Wrong button!!!", Toast.LENGTH_SHORT).show()
                } else {
                    val newTime = SystemClock.elapsedRealtime()
                    server?.sendData(byteArrayOf(0.toByte()), "colorClient")
                    if (fulfilled) {
                        points++
                        reactionTimes.add(newTime - lastShowTime)
                        setAvgReactionTime(reactionTimes.average().toFloat())
                        setNbOfPoints(points)
                    }
                }
        }.addTo(disposable)

        ib_red.setOnClickListener {
            colorButtonClicks.onNext(RED)
        }

        ib_green.setOnClickListener {
            colorButtonClicks.onNext(GREEN)
        }

        ib_blue.setOnClickListener {
            colorButtonClicks.onNext(BLUE)
        }

        ib_magenta.setOnClickListener {
            colorButtonClicks.onNext(MAGENTA)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    var fulfilled = true
    var points = 0
    var lastShowTime: Long = 0
    var lastColor = 0.toByte()
    var reactionTimes = ArrayList<Long>()

    companion object {
        const val RED: Byte = 1
        const val GREEN: Byte = 2
        const val BLUE: Byte = 3
        const val MAGENTA: Byte = 4
    }

    private fun setNbOfPoints(points: Int) {
        tv_points.text = resources.getString(R.string.points, points)
    }

    private fun setAvgReactionTime(avg: Float) {
        tv_avg_reaction.text = resources.getString(R.string.average_reaction, avg)
    }

    private fun setColorButtonsVisibility(visibility: Int) {
        ib_blue.visibility = visibility
        ib_magenta.visibility = visibility
        ib_green.visibility = visibility
        ib_red.visibility = visibility
        btn_start.visibility = visibility
        tv_avg_reaction.visibility = visibility
        tv_points.visibility = visibility
    }

    private fun setColorScreenVisibility(visibility: Int) {
        iv_color_display.visibility = visibility
    }

    private val serverCallbacks = object : ServerCallbacks {
        override fun onClientConnected(nbOfClients: Byte) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, "Screen connected, nb of screens: $nbOfClients", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        override fun onClientDisconnected(clientId: Byte) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, "Screen $clientId disconnected", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onDataChanged(data: ReceivedElement) {
        }

        override fun onServerStarted() {
        }

        override fun onServerFailed(errorCode: Int) {
        }
    }

    private val clientCallbacks = object : ClientCallbacks {

        var clientId: Byte = 0

        override fun onServerFound(device: BluetoothDevice) {
            client?.stopScan()
            client?.connectDevice(device)

        }

        override fun onConnectedToServer(clientId: Byte) {
            runOnUiThread {
                this.clientId = clientId
                Toast.makeText(this@MainActivity, "Connected to control device", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onNumberOfClientsChanged(newNumberOfClients: Byte) {
        }

        override fun onDataChanged(data: ReceivedElement) {
            runOnUiThread {
                when (data.name) {
                    "colorClient" -> {
                        if (data.data[0] == clientId) {
                            when (data.data[1]) {
                                RED -> {
                                    Toast.makeText(this@MainActivity, "RED", Toast.LENGTH_SHORT).show()
                                    iv_color_display.setColorFilter(
                                        ResourcesCompat.getColor(
                                            resources,
                                            R.color.red,
                                            null
                                        )
                                    )
                                }
                                GREEN -> {
                                    Toast.makeText(this@MainActivity, "GREEN", Toast.LENGTH_SHORT).show()
                                    iv_color_display.setColorFilter(
                                        ResourcesCompat.getColor(
                                            resources,
                                            R.color.green,
                                            null
                                        )
                                    )
                                }
                                BLUE -> {
                                    Toast.makeText(this@MainActivity, "BLUE", Toast.LENGTH_SHORT).show()
                                    iv_color_display.setColorFilter(
                                        ResourcesCompat.getColor(
                                            resources,
                                            R.color.blue,
                                            null
                                        )
                                    )
                                }
                                MAGENTA -> {
                                    Toast.makeText(this@MainActivity, "MAGENTA", Toast.LENGTH_SHORT).show()
                                    iv_color_display.setColorFilter(
                                        ResourcesCompat.getColor(
                                            resources,
                                            R.color.magenta,
                                            null
                                        )
                                    )
                                }
                            }
                        }
                        if (data.data[0] == 0.toByte()) {
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
}
