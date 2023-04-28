package org.kbods.utils

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

fun resourceAsInput(classpathLocation: String): InputStream {
    val classLoader = Thread.currentThread().contextClassLoader
    return classLoader.getResourceAsStream(classpathLocation)
}

fun resourceExists(classpathLocation: String): Boolean {
    val classLoader = Thread.currentThread().contextClassLoader
    return classLoader.getResource(classpathLocation) != null
}

fun resourceAsString(classpathLocation: String): String {
    return resourceAsInput(classpathLocation)
        .use { BufferedReader(InputStreamReader(it)).readText() }
}

