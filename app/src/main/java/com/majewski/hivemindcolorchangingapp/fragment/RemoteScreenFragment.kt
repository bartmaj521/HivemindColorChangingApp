package com.majewski.hivemindcolorchangingapp.fragment


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.majewski.hivemindcolorchangingapp.ColorCodes
import com.majewski.hivemindcolorchangingapp.R
import com.majewski.hivemindcolorchangingapp.logic.RemoteScreenLogic
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_remote_screen.*


class RemoteScreenFragment : Fragment() {

    val logic = RemoteScreenLogic()

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        context?.let {
            logic.initializeBtClient(context)

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
        return inflater.inflate(R.layout.fragment_remote_screen, container, false)
    }

    private fun onLogicEvent(event: RemoteScreenLogic.Event) {

        when (event.type) {

            RemoteScreenLogic.Event.Type.CONNECTED_TO_SERVER -> Toast.makeText(
                context,
                "Connected to control device",
                Toast.LENGTH_SHORT
            ).show()
            RemoteScreenLogic.Event.Type.DISPLAY_COLOR -> TODO()
        }

    }

    private fun setDisplayedColor(colorCode: Int) {
        iv_color_display.setColorFilter(
            ResourcesCompat.getColor(
                resources,
                ColorCodes.colorIds[colorCode],
                null
            )
        )
    }
}
