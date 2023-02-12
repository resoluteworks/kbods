package org.kbods.utils

import com.beust.klaxon.JsonObject

fun JsonObject.safeDouble(name: String): Double {
    return (this[name]!! as Number).toDouble()
}
