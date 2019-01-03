package com.majewski.hivemindcolorchangingapp

object ColorCodes {
    const val WHITE: Byte = 0
    const val RED: Byte = 1
    const val GREEN: Byte = 2
    const val BLUE: Byte = 3
    const val MAGENTA: Byte = 4

    val colorIds = IntArray(4)

    init {
        colorIds[RED.toInt()] = R.color.red
        colorIds[GREEN.toInt()] = R.color.green
        colorIds[BLUE.toInt()] = R.color.blue
        colorIds[MAGENTA.toInt()] = R.color.magenta
        colorIds[WHITE.toInt()] = R.color.white
    }
}