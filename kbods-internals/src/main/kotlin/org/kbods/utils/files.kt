package org.kbods.utils

import java.io.File

fun currentDirectory(): File {
    return File(System.getProperty("user.dir"))
}
