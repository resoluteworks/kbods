package org.kbods.utils

import java.io.File

fun workingDirectory(): File {
    return File(System.getProperty("user.dir"))
}
