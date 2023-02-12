package org.kbods.read

import com.beust.klaxon.JsonObject

fun JsonObject.interestStartDate(): String? = string("startDate")
fun JsonObject.interestEndDate(): String? = string("endDate")
