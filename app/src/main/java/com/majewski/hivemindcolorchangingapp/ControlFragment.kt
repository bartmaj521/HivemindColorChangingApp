package com.majewski.hivemindcolorchangingapp


import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.majewski.hivemindbt.data.ReceivedElement
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

    private val colorButtonClicks = PublishSubject.create<Byte>()
    private val disposable = CompositeDisposable()

    private val logic = ControlLogic(colorButtonClicks)

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        context?.let {
            logic.initializeBtServer(context)

            logic.communicationBus
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    onLogicEvent(it)
                }
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
            setAvgReactionTime(0f)
            setNbOfPoints(0)
            logic.startExercise()
        }
        shuffleAndMapButtonsToActions()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
        logic.onDestroy()
    }

    private fun shuffleAndMapButtonsToActions() {
        val array = ByteArray(4) {(it + 1).toByte()}.toMutableList()
        array.shuffle()

        ib_1.setColorFilter(getColorByIntCode(array[0]))
        ib_1.setOnClickListener {
            colorButtonClicks.onNext(array[0])
        }

        ib_2.setColorFilter(getColorByIntCode(array[1]))
        ib_2.setOnClickListener {
            colorButtonClicks.onNext(array[1])
        }

        ib_3.setColorFilter(getColorByIntCode(array[2]))
        ib_3.setOnClickListener {
            colorButtonClicks.onNext(array[2])
        }

        ib_4.setColorFilter(getColorByIntCode(array[3]))
        ib_4.setOnClickListener {
            colorButtonClicks.onNext(array[3])
        }
    }

    private fun getColorByIntCode(colorCode: Byte): Int {
        return when (colorCode) {
            RED -> {
                    ResourcesCompat.getColor(
                        resources,
                        R.color.red,
                        null
                    )
            }
            GREEN -> {
                    ResourcesCompat.getColor(
                        resources,
                        R.color.green,
                        null
                    )
            }
            BLUE -> {
                    ResourcesCompat.getColor(
                        resources,
                        R.color.blue,
                        null
                    )
            }
            MAGENTA -> {
                    ResourcesCompat.getColor(
                        resources,
                        R.color.magenta,
                        null
                    )
            }
            else -> {
                Color.WHITE
            }
        }
    }

    private fun setNbOfPoints(points: Int) {
        tv_points.text = resources.getString(R.string.points, points)
    }

    private fun setAvgReactionTime(avg: Float) {
        tv_avg_reaction.text = resources.getString(R.string.average_reaction, avg)
    }

    private fun onLogicEvent(event: ControlLogic.Event) {
        when(event.type) {
            ControlLogic.Event.Type.WRONG_BUTTON -> Toast.makeText(context, "Wrong button :(", Toast.LENGTH_SHORT).show()
            ControlLogic.Event.Type.CLIENT_CONNECTED -> Toast.makeText(context, "Screen connected, nb of screens: ${event.data}", Toast.LENGTH_SHORT).show()
            ControlLogic.Event.Type.CLIENT_DISCONNECTED -> Toast.makeText(context, "Screen ${event.data} disconnected", Toast.LENGTH_SHORT).show()
            ControlLogic.Event.Type.SERVER_STARTED -> Toast.makeText(context, "Server started", Toast.LENGTH_SHORT).show()
            ControlLogic.Event.Type.SERVER_FAILED -> Toast.makeText(context, "Server start failed", Toast.LENGTH_SHORT).show()
            ControlLogic.Event.Type.NEW_POINT -> setNbOfPoints(event.data as Int)
            ControlLogic.Event.Type.NEW_AVERAGE -> setAvgReactionTime(event.data as Float)
        }
    }

    companion object {
        const val RED: Byte = 1
        const val GREEN: Byte = 2
        const val BLUE: Byte = 3
        const val MAGENTA: Byte = 4
    }
}
