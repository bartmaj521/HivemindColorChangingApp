package com.majewski.hivemindcolorchangingapp


import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.majewski.hivemindbt.data.ReceivedElement
import com.majewski.hivemindbt.server.HivemindBtServer
import com.majewski.hivemindbt.server.ServerCallbacks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_control.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.random.nextInt

class ControlFragment : Fragment() {

    private var server: HivemindBtServer? = null
    private val colorButtonClicks = PublishSubject.create<Byte>()
    private val disposable = CompositeDisposable()

    var fulfilled = true
    var points = 0
    var lastShowTime: Long = 0
    var lastColor = 0.toByte()
    var reactionTimes = ArrayList<Long>()

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        context?.let {
            server = HivemindBtServer(context, serverCallbacks)
            server?.addData("colorClient", 0)
            server?.startServer()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                    Toast.makeText(context, "Wrong button!!!", Toast.LENGTH_SHORT).show()
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

        mapButtonsToSubject()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    private fun mapButtonsToSubject() {
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

    private fun setNbOfPoints(points: Int) {
        tv_points.text = resources.getString(R.string.points, points)
    }

    private fun setAvgReactionTime(avg: Float) {
        tv_avg_reaction.text = resources.getString(R.string.average_reaction, avg)
    }

    private val serverCallbacks = object : ServerCallbacks {
        override fun onClientConnected(nbOfClients: Byte) {
            activity?.runOnUiThread {
                Toast.makeText(context, "Screen connected, nb of screens: $nbOfClients", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        override fun onClientDisconnected(clientId: Byte) {
            activity?.runOnUiThread {
                Toast.makeText(context, "Screen $clientId disconnected", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onDataChanged(data: ReceivedElement) {
        }

        override fun onServerStarted() {
        }

        override fun onServerFailed(errorCode: Int) {
        }
    }

    companion object {
        const val RED: Byte = 1
        const val GREEN: Byte = 2
        const val BLUE: Byte = 3
        const val MAGENTA: Byte = 4
    }
}
