package com.majewski.hivemindcolorchangingapp.fragment


import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.majewski.hivemindcolorchangingapp.ColorCodes
import com.majewski.hivemindcolorchangingapp.R
import com.majewski.hivemindcolorchangingapp.logic.ControlLogic
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_control.*

class ControlFragment : Fragment() {

    private val colorButtonClicks = PublishSubject.create<Byte>()
    private val disposable = CompositeDisposable()

    private val logic = ControlLogic(colorButtonClicks)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nbOfFlashes = arguments?.getInt(TAG_NB_FLASHES) ?: 10
        val maxNbOfScreens = arguments?.getInt(TAG_MAX_NB_SCREENS) ?: 2
        val minDelay = arguments?.getInt(TAG_MIN_DELAY) ?: 2000
        val maxDelay = arguments?.getInt(TAG_MAX_DELAY) ?: 5000

        logic.setParameters(nbOfFlashes, maxNbOfScreens, minDelay, maxDelay)
        context?.let {
            logic.initializeBtServer(it)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        context?.let {
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
        val array = ByteArray(4) { (it + 1).toByte() }.toMutableList()
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
            ColorCodes.RED -> {
                ResourcesCompat.getColor(
                    resources,
                    R.color.red,
                    null
                )
            }
            ColorCodes.GREEN -> {
                ResourcesCompat.getColor(
                    resources,
                    R.color.green,
                    null
                )
            }
            ColorCodes.BLUE -> {
                ResourcesCompat.getColor(
                    resources,
                    R.color.blue,
                    null
                )
            }
            ColorCodes.MAGENTA -> {
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
        when (event.type) {
            ControlLogic.Event.Type.WRONG_BUTTON -> Toast.makeText(
                context,
                "Wrong button :(",
                Toast.LENGTH_SHORT
            ).show()
            ControlLogic.Event.Type.CLIENT_CONNECTED -> Toast.makeText(
                context,
                "Screen connected, nb of screens: ${event.data}",
                Toast.LENGTH_SHORT
            ).show()
            ControlLogic.Event.Type.CLIENT_DISCONNECTED -> Toast.makeText(
                context,
                "Screen ${event.data} disconnected",
                Toast.LENGTH_SHORT
            ).show()
            ControlLogic.Event.Type.SERVER_STARTED -> Toast.makeText(
                context,
                "Server started",
                Toast.LENGTH_SHORT
            ).show()
            ControlLogic.Event.Type.SERVER_FAILED -> Toast.makeText(
                context,
                "Server start failed",
                Toast.LENGTH_SHORT
            ).show()
            ControlLogic.Event.Type.NEW_POINT -> setNbOfPoints(event.data as Int)
            ControlLogic.Event.Type.NEW_AVERAGE -> setAvgReactionTime(event.data as Float)
        }
    }

    companion object {
        private const val TAG_NB_FLASHES = "nbOfFlashes"
        private const val TAG_MAX_NB_SCREENS = "maxNbOfScreens"
        private const val TAG_MIN_DELAY = "minDelay"
        private const val TAG_MAX_DELAY = "maxDelay"

        fun create(nbOfFlashes: Int, maxNbOfScreens:Int, minDelay: Int, maxDelay: Int): ControlFragment {

            val f = ControlFragment()
            f.arguments = Bundle()
            f.arguments?.apply {
                putInt(TAG_NB_FLASHES, nbOfFlashes)
                putInt(TAG_MAX_NB_SCREENS, maxNbOfScreens)
                putInt(TAG_MIN_DELAY, minDelay)
                putInt(TAG_MAX_DELAY, maxDelay)
            }
            return f
        }
    }
}
