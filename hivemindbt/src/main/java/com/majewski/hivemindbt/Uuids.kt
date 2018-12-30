package com.majewski.hivemindbt

import java.util.*

internal object Uuids {

    val SERVICE_PRIMARY = UUID(0L, 300L)

    val CHARACTERISTIC_CLIENT_ID = UUID(0L, 301L)
    val CHARACTERISTIC_NB_OF_CLIENTS = UUID(0L, 302L)

    val CHARACTERISTIC_READ_DATA = UUID(0L, 400L)
}