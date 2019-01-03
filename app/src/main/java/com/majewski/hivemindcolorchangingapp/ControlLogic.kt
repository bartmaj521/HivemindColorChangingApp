package com.majewski.hivemindcolorchangingapp

import android.content.Context
import android.os.SystemClock
import com.majewski.hivemindbt.data.ReceivedElement
import com.majewski.hivemindbt.server.HivemindBtServer
import com.majewski.hivemindbt.server.ServerCallbacks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.random.nextInt

class ControlLogic(private val colorButtonClicks: PublishSubject<Byte>) {

    val communicationBus = PublishSubject.create<Event>()

    private var server: HivemindBtServer? = null
    private val disposable = CompositeDisposable()

    private var fulfilled = true
    private var points = 0
    private var lastShowTime: Long = 0
    private var lastColor = 0.toByte()
    private var reactionTimes = ArrayList<Long>()

    fun initializeBtServer(context: Context) {
        server = HivemindBtServer(context, serverCallbacks)
        server?.addData("colorClient", 0)
        server?.startServer()
    }

    fun startExercise() {
        points = 0
        reactionTimes.clear()

        var delay = 0L

        Observable.range(0, 10)
            .flatMap {
                delay += Random.nextLong(2000,5000)
                Observable.just(it).delay(delay, TimeUnit.MILLISECONDS)
            }.subscribeOn(Schedulers.computation())
            .subscribe {
                lastColor = Random.nextInt(1..4).toByte()
                server?.sendData(byteArrayOf(Random.nextInt(1..2).toByte(), lastColor), "colorClient")
                lastShowTime = SystemClock.elapsedRealtime()
                fulfilled = true
            }.addTo(disposable)

        colorButtonClicks
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if(it != lastColor) {
                    fulfilled = false
                    communicationBus.onNext(Event(Event.Type.WRONG_BUTTON))
                } else {
                    val newTime = SystemClock.elapsedRealtime()
                    server?.sendData(byteArrayOf(0.toByte()), "colorClient")
                    if (fulfilled) {
                        points++
                        reactionTimes.add(newTime - lastShowTime)
                        communicationBus.onNext(Event(Event.Type.NEW_AVERAGE, reactionTimes.average().toFloat()))
                        communicationBus.onNext(Event(Event.Type.NEW_POINT, points))
                    }
                }
            }.addTo(disposable)
    }

    fun onDestroy() {
        disposable.dispose()
    }

    private val serverCallbacks = object : ServerCallbacks {
        override fun onClientConnected(nbOfClients: Byte) {
            communicationBus.onNext(Event(Event.Type.CLIENT_CONNECTED, nbOfClients))
        }

        override fun onClientDisconnected(clientId: Byte) {
            communicationBus.onNext(Event(Event.Type.CLIENT_DISCONNECTED, clientId))
        }

        override fun onDataChanged(data: ReceivedElement) {
        }

        override fun onServerStarted() {
            communicationBus.onNext(Event(Event.Type.SERVER_STARTED))
        }

        override fun onServerFailed(errorCode: Int) {
            communicationBus.onNext(Event(Event.Type.SERVER_FAILED, errorCode))
        }
    }

    class Event(
        val type: Type,
        val data: Any? = null
    ){
        enum class Type{
            CLIENT_CONNECTED,
            CLIENT_DISCONNECTED,
            SERVER_STARTED,
            SERVER_FAILED,
            NEW_POINT,
            NEW_AVERAGE,
            WRONG_BUTTON
        }
    }

}