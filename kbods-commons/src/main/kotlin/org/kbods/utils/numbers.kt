package org.kbods.utils

import java.text.DecimalFormat

internal val FMT_GROUPED = DecimalFormat("#,###")

fun Number.grouped(): String {
    return FMT_GROUPED.format(this)
}

