package org.kbods.read

import org.kbods.utils.grouped
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

private val log = LoggerFactory.getLogger("org.kbods.read")
private const val DEFAULT_BUFFER_SIZE = 1024 * 1024
typealias BodsStatementHandler = (statement: BodsStatement) -> Unit

fun File.readBodsStatements(statementHandler: (statement: BodsStatement) -> Unit) {
    this.inputStream().use { jsonlInputStream ->
        jsonlInputStream.readBodsStatements(statementHandler)
    }
}

fun InputStream.readBodsStatements(statementHandler: (statement: BodsStatement) -> Unit) {
    this.useBodsStatementsSequence { sequence ->
        sequence.forEach { statement ->
            statementHandler(statement)
        }
    }
}

fun File.useBodsStatementsSequence(consumer: (Sequence<BodsStatement>) -> Unit) {
    this.inputStream().use { jsonlInputStream ->
        jsonlInputStream.useBodsStatementsSequence(consumer)
    }
}

fun InputStream.useBodsStatementsSequence(consumer: (Sequence<BodsStatement>) -> Unit) {
    var count = 0
    BufferedReader(InputStreamReader(this), DEFAULT_BUFFER_SIZE).useLines { lines ->
        consumer(lines.map { line ->
            val statement = BodsStatement(line)
            count++
            if (count % 100_000 == 0) {
                log.info("Processed ${count.grouped()} BODS statements")
            }
            statement
        })
    }

    log.info("Finished processing ${count.grouped()} BODS statements")
}
