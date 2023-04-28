package org.kbods.utils

import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.util.*

class TempDir(
    val workingDir: File = currentDirectory()
) : Closeable {

    private val root = createRootDir(workingDir)

    fun newFile(): File {
        return File(root, uuid())
    }

    fun newDirectory(): File {
        val dir = File(root, uuid())
        if (!dir.mkdirs()) {
            throw IllegalStateException("Could not create directory $dir")
        }
        return dir
    }

    override fun close() {
        val deleted = try {
            FileUtils.deleteDirectory(root)
            true
        } catch (ioe: IOException) {
            log.warn("Could not delete temporary directory $root")
            false
        }

        if (!deleted) {
            log.warn("Could not delete temporary directory $root")
        } else {
            log.info("Deleted temporary directory $root")
        }
    }

    private fun createRootDir(workingDirectory: File): File {
        val tempFile = File(workingDirectory, uuid())
        if (!tempFile.mkdirs()) {
            throw IllegalStateException("Could not create temp dir $tempFile")
        }
        return tempFile
    }

    private fun uuid() = UUID.randomUUID().toString()

    companion object {
        private val log = LoggerFactory.getLogger(TempDir::class.java)
    }
}
