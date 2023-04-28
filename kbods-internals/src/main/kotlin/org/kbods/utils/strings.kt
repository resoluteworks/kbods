package org.kbods.utils

fun String.cleanWhitespace(): String {
    return replace("\\s+".toRegex(), " ").trim()
}
