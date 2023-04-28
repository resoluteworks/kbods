package org.kbods.read

import org.kbods.utils.TempDir
import org.kbods.utils.checkOk
import org.kbods.utils.currentDirectory
import org.kbods.utils.get
import org.kbods.utils.grouped
import org.kbods.utils.gunzip
import org.kbods.utils.httpClient
import org.kbods.utils.writeTo
import org.slf4j.LoggerFactory
import java.io.File

class BodsDownload(
    val bodsGzipUrl: String,
    val workingDirectory: File = currentDirectory()
) {

    private val httpClient = httpClient()

    fun readStatements(statementHandler: (BodsStatement) -> Unit) {
        useStatementSequence { sequence ->
            sequence.forEach { bodsStatement ->
                statementHandler(bodsStatement)
            }
        }
    }

    fun useStatementSequence(consumer: (Sequence<BodsStatement>) -> Unit) {
        TempDir(workingDirectory).use { tempDir ->
            val jsonlFile = downloadAndUnzip(bodsGzipUrl, tempDir)
            jsonlFile.useBodsStatementsSequence { sequence ->
                consumer(sequence)
            }
        }
    }

    private fun downloadAndUnzip(bodsGzipUrl: String, tempDir: TempDir): File {
        val archiveFile = tempDir.newFile()
        log.info("Downloading BODS register from $bodsGzipUrl to $archiveFile")
        val response = httpClient
            .get(bodsGzipUrl)
            .checkOk()
        response.writeTo(archiveFile)

        val jsonlFile = tempDir.newFile()
        log.info("GUnzipping $archiveFile to $jsonlFile")
        archiveFile.gunzip(jsonlFile)

        log.info("Finished gunzipping $archiveFile to $jsonlFile")
        log.info("Archive size: ${archiveFile.length().grouped()} bytes")
        log.info("JSONL size: ${jsonlFile.length().grouped()} bytes")

        return jsonlFile
    }

    companion object {
        private val log = LoggerFactory.getLogger(BodsDownload::class.java)
        const val URL_LATEST = "https://oo-register-production.s3-eu-west-1.amazonaws.com/public/exports/statements.latest.jsonl.gz"

        fun forUrl(bodsGzipUrl: String, workingDirectory: File = currentDirectory()): BodsDownload {
            return BodsDownload(bodsGzipUrl, workingDirectory)
        }

        fun latest(workingDirectory: File = currentDirectory()): BodsDownload {
            return forUrl(URL_LATEST, workingDirectory)
        }
    }
}
