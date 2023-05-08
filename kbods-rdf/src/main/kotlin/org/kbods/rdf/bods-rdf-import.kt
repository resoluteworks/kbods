package org.kbods.rdf

import org.eclipse.rdf4j.repository.RepositoryConnection
import org.kbods.rdf.vocabulary.BodsVocabulary
import org.kbods.read.BodsDownload
import org.kbods.read.BodsStatement
import org.kbods.read.useBodsStatementsSequence
import java.io.File
import java.io.InputStream

fun BodsDownload.import(
    connection: RepositoryConnection,
    config: BodsRdfConfig = BodsRdfConfig()
) {
    this.useStatementSequence { sequence ->
        doImport(sequence, connection, config)
    }
}

fun File.import(
    connection: RepositoryConnection,
    config: BodsRdfConfig = BodsRdfConfig()
) {
    this.useBodsStatementsSequence { sequence ->
        doImport(sequence, connection, config)
    }
}

fun InputStream.import(
    connection: RepositoryConnection,
    config: BodsRdfConfig = BodsRdfConfig()
) {
    this.useBodsStatementsSequence { sequence ->
        doImport(sequence, connection, config)
    }
}

private fun doImport(
    sequence: Sequence<BodsStatement>,
    connection: RepositoryConnection,
    config: BodsRdfConfig = BodsRdfConfig()
) {
    BodsVocabulary.write(connection)
    sequence.forEach { bodsStatement ->
        val rdfStatements = bodsStatement.toRdf(config)
        connection.add(rdfStatements)
        config.runPlugins(bodsStatement) { _, statements ->
            connection.add(statements)
        }
    }
}
