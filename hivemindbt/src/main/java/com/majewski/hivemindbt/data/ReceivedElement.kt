package com.majewski.hivemindbt.data

class ReceivedElement(
    val from: Byte,
    val dataId: Byte,
    val name: String,
    val data: ByteArray
)