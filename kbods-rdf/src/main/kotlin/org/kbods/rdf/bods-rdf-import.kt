package org.kbods.rdf

import org.eclipse.rdf4j.repository.RepositoryConnection
import org.kbods.rdf.plugins.PluginRunner
import org.kbods.rdf.vocabulary.BodsVocabulary
import org.kbods.read.BodsDownload
import org.kbods.read.BodsStatement
import org.kbods.read.useBodsStatementsSequence
import java.io.File
import java.io.InputStream

fun BodsDownload.import(
    connection: RepositoryConnection,
    config: BodsRdfConfig = BodsRdfConfig(),
    includeVocabulary: Boolean = true
) {
    this.useStatementSequence { sequence ->
        doImport(sequence, connection, config, includeVocabulary)
    }
}

fun File.import(
    connection: RepositoryConnection,
    config: BodsRdfConfig = BodsRdfConfig(),
    includeVocabulary: Boolean = true
) {
    this.useBodsStatementsSequence { sequence ->
        doImport(sequence, connection, config, includeVocabulary)
    }
}

fun InputStream.import(
    connection: RepositoryConnection,
    config: BodsRdfConfig = BodsRdfConfig(),
    includeVocabulary: Boolean = true
) {
    this.useBodsStatementsSequence { sequence ->
        doImport(sequence, connection, config, includeVocabulary)
    }
}

private fun doImport(
    sequence: Sequence<BodsStatement>,
    connection: RepositoryConnection,
    config: BodsRdfConfig = BodsRdfConfig(),
    includeVocabulary: Boolean = true
) {
    PluginRunner.connection(config, connection).use { pluginRunner ->
        if (includeVocabulary) {
            BodsVocabulary.write(connection)
        }
        sequence.forEach { bodsStatement ->
            val rdfStatements = bodsStatement.toRdf(config)
            connection.add(rdfStatements)
            pluginRunner.runPlugins(bodsStatement)
        }
    }
}

