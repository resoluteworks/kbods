package org.kbods.rdf

import org.eclipse.rdf4j.repository.RepositoryConnection
import org.kbods.rdf.vocabulary.BodsVocabulary
import org.kbods.read.BodsDownload
import org.kbods.read.BodsStatement
import org.kbods.read.useBodsStatementsSequence
import org.rdf4k.repository.useBatch
import java.io.File
import java.io.InputStream

fun BodsDownload.import(
    connection: RepositoryConnection,
    batchSize: Int = 10_000,
    config: BodsRdfConfig = BodsRdfConfig()
) {
    this.useStatementSequence { sequence ->
        doImport(sequence, connection, batchSize, config)
    }
}

fun File.import(
    connection: RepositoryConnection,
    batchSize: Int = 10_000,
    config: BodsRdfConfig = BodsRdfConfig()
) {
    this.useBodsStatementsSequence { sequence ->
        doImport(sequence, connection, batchSize, config)
    }
}

fun InputStream.import(
    connection: RepositoryConnection,
    batchSize: Int = 10_000,
    config: BodsRdfConfig = BodsRdfConfig()
) {
    this.useBodsStatementsSequence { sequence ->
        doImport(sequence, connection, batchSize, config)
    }
}

private fun doImport(
    sequence: Sequence<BodsStatement>,
    connection: RepositoryConnection,
    batchSize: Int,
    config: BodsRdfConfig = BodsRdfConfig()
) {
    connection.useBatch(batchSize) { batch ->
        BodsVocabulary.write(batch)
        sequence.forEach { bodsStatement ->
            val rdfStatements = bodsStatement.toRdf(config)
            batch.add(rdfStatements)
            config.runPlugins(bodsStatement) { _, statements ->
                batch.add(statements)
            }
        }
    }
}
