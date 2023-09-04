package org.kbods.rdf

import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.kbods.rdf.vocabulary.BodsVocabulary
import org.kbods.read.BodsDownload
import org.kbods.read.BodsStatement
import org.kbods.read.useBodsStatements
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
            batch.add(bodsStatement.toRdf(config))
        }
    }
}

fun BodsStatement.toRdf(config: BodsRdfConfig): List<Statement> {
    val statements = mutableListOf<Statement>()
    statements.addAll(coreRdfStatements(config))
    config.runPlugins(this) { _, pluginStatements ->
        statements.addAll(pluginStatements)
    }
    return statements
}

fun List<BodsStatement>.toRdf(config: BodsRdfConfig = BodsRdfConfig()): List<Statement> {
    val statements = mutableListOf<Statement>()
    forEach { statement ->
        statements.addAll(statement.toRdf(config))
    }
    return statements
}
