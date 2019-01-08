package com.majewski.hivemindcolorchangingapp.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.majewski.hivemindcolorchangingapp.R
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_ok.setOnClickListener {
            if(et_number_of_flashes.text.isNotEmpty()) {
                listener?.nbOfFlashes = et_number_of_flashes.text.toString().toInt()
            }
            if(et_max_number_of_screens.text.isNotEmpty()) {
                listener?.nbOfFlashes = et_max_number_of_screens.text.toString().toInt()
            }
            if(et_min_delay.text.isNotEmpty()) {
                listener?.nbOfFlashes = et_min_delay.text.toString().toInt()
            }
            if(et_max_delay.text.isNotEmpty()) {
                listener?.nbOfFlashes = et_max_delay.text.toString().toInt()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        var nbOfFlashes: Int
        var minDuration: Int
        var maxDuration: Int
        var maxNumberOfScreens: Int
    }
}
