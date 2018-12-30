package com.majewski.hivemindbt

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

object HivemindBt {

    fun isSupported(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
                && (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.isMultipleAdvertisementSupported
    }

    fun requestPermissions(activity: Activity): Boolean {
        if (!hasLocationPermissions(activity)){
            activity.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2)
            return hasLocationPermissions(activity)
        }
        return true
    }

    fun isBluetoothEnabled(activity: Activity): Boolean {
        val bluetoothAdapter: BluetoothAdapter? = (activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        return !(bluetoothAdapter == null || !bluetoothAdapter.isEnabled)
    }

    fun requestEnableBluetooth(activity: Activity) {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(enableBtIntent, 1)
    }

    private fun hasLocationPermissions(activity: Activity): Boolean {
        return activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
}
