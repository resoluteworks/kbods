package org.kbods.utils

import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.util.*

class TempDir(workingDir: File = workingDirectory()) : Closeable {

    val directory = createRootDir(workingDir)

    fun newFile(extension: String? = null): File {
        val ext = extension?.let { "." + extension.removePrefix(".") } ?: ""
        return File(directory, UUID.randomUUID().toString() + ext)
    }

    fun newDirectory(): File {
        val dir = File(directory, UUID.randomUUID().toString())
        if (!dir.mkdirs()) {
            throw IllegalStateException("Could not create directory $dir")
        }
        return dir
    }

    override fun close() {
        val deleted = try {
            directory.deleteRecursively()
            true
        } catch (ioe: IOException) {
            log.warn("Could not delete temporary directory $directory")
            false
        }

        if (!deleted) {
            log.warn("Could not delete temporary directory $directory")
        } else {
            log.info("Deleted temporary directory $directory")
        }
    }

    private fun createRootDir(workingDirectory: File): File {
        val tempFile = File(workingDirectory, UUID.randomUUID().toString())
        if (!tempFile.mkdirs()) {
            throw IllegalStateException("Could not create temp dir $tempFile")
        }
        return tempFile
    }

    companion object {
        private val log = LoggerFactory.getLogger(TempDir::class.java)
    }
}

fun <T> withTempDir(
        workingDir: File = workingDirectory(),
        block: (TempDir) -> T
): T {
    return TempDir(workingDir).use(block)
}