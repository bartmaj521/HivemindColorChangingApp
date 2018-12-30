package com.majewski.hivemindcolorchangingapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_control.setOnClickListener {
            setColorButtonsVisibility(View.VISIBLE)
            setColorScreenVisibility(View.GONE)
        }

        btn_screen.setOnClickListener {
            setColorScreenVisibility(View.VISIBLE)
            setColorButtonsVisibility(View.GONE)
        }
    }

    private fun setColorButtonsVisibility(visibility: Int) {
        ib_blue.visibility = visibility
        ib_orange.visibility = visibility
        ib_purple.visibility = visibility
        ib_red.visibility = visibility
    }

    private fun setColorScreenVisibility(visibility: Int) {
        iv_color_display.visibility = visibility
    }
}
