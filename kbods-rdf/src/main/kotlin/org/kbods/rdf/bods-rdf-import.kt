package org.kbods.rdf

import org.eclipse.rdf4j.repository.RepositoryConnection
import org.kbods.rdf.vocabulary.BodsVocabulary
import org.kbods.read.BodsDownload
import org.kbods.read.BodsStatement
import org.kbods.read.useBodsStatements
import org.rdf4k.StatementsBatch
import org.rdf4k.useBatch
import java.io.File
import java.io.InputStream

fun BodsDownload.import(
    connection: RepositoryConnection,
    batchSize: Int = 10_000,
    config: BodsRdfConfig = BodsRdfConfig()
) {
    this.useStatementSequence { sequence ->
        sequence.import(connection, batchSize, config)
    }
}

fun File.import(
    connection: RepositoryConnection,
    batchSize: Int = 10_000,
    config: BodsRdfConfig = BodsRdfConfig()
) {
    this.useBodsStatements { sequence ->
        sequence.import(connection, batchSize, config)
    }
}

fun InputStream.import(
    connection: RepositoryConnection,
    batchSize: Int = 10_000,
    config: BodsRdfConfig = BodsRdfConfig()
) {
    this.useBodsStatements { sequence ->
        sequence.import(connection, batchSize, config)
    }
}

fun Sequence<BodsStatement>.import(
    connection: RepositoryConnection,
    batchSize: Int,
    config: BodsRdfConfig = BodsRdfConfig()
) {
    connection.useBatch(batchSize) { batch ->
        BodsVocabulary.write(batch)
        forEach { bodsStatement ->
            bodsStatement.write(batch, config)
        }
    }
}

fun BodsStatement.write(
        batch: StatementsBatch,
        config: BodsRdfConfig
) {
    val rdfStatements = toRdf(config)
    batch.add(rdfStatements)
    config.runPlugins(this) { _, statements ->
        batch.add(statements)
    }
}
