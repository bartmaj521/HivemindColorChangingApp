package com.majewski.hivemindcolorchangingapp

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.majewski.hivemindbt.HivemindBt
import com.majewski.hivemindcolorchangingapp.fragment.ControlFragment
import com.majewski.hivemindcolorchangingapp.fragment.RemoteScreenFragment
import com.majewski.hivemindcolorchangingapp.fragment.SettingsFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SettingsFragment.OnFragmentInteractionListener {

    override var nbOfFlashes: Int = 10

    override var minDuration: Int = 2000

    override var maxDuration: Int = 5000

    override var maxNumberOfScreens: Int = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_control.setOnClickListener {
            if (!HivemindBt.requestPermissions(this)) {
                Toast.makeText(this, "Need permissions", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!HivemindBt.isBluetoothEnabled(this)) {
                HivemindBt.requestEnableBluetooth(this)
                Toast.makeText(this, "Bluetooth needs to be enabled", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            changeFragment(ControlFragment())
        }

        btn_screen.setOnClickListener {
            if (!HivemindBt.requestPermissions(this)) {
                Toast.makeText(this, "Need permissions", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!HivemindBt.isBluetoothEnabled(this)) {
                HivemindBt.requestEnableBluetooth(this)
                Toast.makeText(this, "Bluetooth needs to be enabled", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            changeFragment(RemoteScreenFragment())
        }

        btn_settings.setOnClickListener {
            changeFragment(SettingsFragment())
        }
    }

    private fun changeFragment(f: Fragment) {
        fl_fragment_container.removeAllViews()
        supportFragmentManager.beginTransaction()
            .add(R.id.fl_fragment_container, f).commit()

    }
}
