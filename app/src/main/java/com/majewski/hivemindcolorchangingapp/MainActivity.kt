package com.majewski.hivemindcolorchangingapp

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.majewski.hivemindbt.HivemindBt
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

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

            changeFragment(ControlFragment())
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

            changeFragment(RemoteScreenFragment())
        }
    }

    private fun changeFragment(f: Fragment){
        fl_fragment_container.removeAllViews()
        supportFragmentManager.beginTransaction()
            .add(R.id.fl_fragment_container, f).commit()

    }
}
