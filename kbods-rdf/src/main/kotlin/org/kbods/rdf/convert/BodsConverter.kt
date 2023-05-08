package org.kbods.rdf.convert

import org.kbods.rdf.BodsRdfConfig
import org.kbods.rdf.BodsRdfWriter
import org.kbods.rdf.close
import org.kbods.rdf.plugins.separateFile
import org.kbods.rdf.toRdf
import org.kbods.rdf.vocabulary.BodsVocabulary
import org.kbods.read.BodsDownload
import org.kbods.read.BodsStatement
import org.kbods.read.useBodsStatementsSequence
import java.io.File

class BodsConverter(
    val config: BodsRdfConfig,
    val outputFiles: List<File>
) {

    private val coreWriters = mutableListOf<BodsRdfWriter>()
    private val pluginWriters = mutableMapOf<PluginWriterKey, BodsRdfWriter>()

    init {
        outputFiles.forEach { coreFile ->
            coreWriters.add(BodsRdfWriter(coreFile))
            config.allPlugins.forEach { plugin ->
                val key = PluginWriterKey(coreFile, plugin.name)
                pluginWriters[key] = plugin.separateFile(coreFile)
            }
        }
    }

    fun convert(bodsDownload: BodsDownload) {
        bodsDownload.useStatementSequence { sequence ->
            convert(sequence)
        }
    }

    fun convert(bodsJsonlFile: File) {
        bodsJsonlFile.useBodsStatementsSequence { sequence ->
            convert(sequence)
        }
    }

    fun convert(statements: Sequence<BodsStatement>) {
        try {
            coreWriters.forEach { coreWriter ->
                BodsVocabulary.write(coreWriter.rdfWriter)
            }
            statements.forEach { bodsStatement ->
                val rdfStatements = bodsStatement.toRdf(config)
                coreWriters.forEach { coreWriter ->
                    coreWriter.write(rdfStatements)
                    config.runPlugins(bodsStatement) { pluginName, statements ->
                        getPluginWriter(coreWriter.outputFile, pluginName).write(statements)
                    }
                }
            }
        } finally {
            coreWriters.close()
            pluginWriters.values.close()
        }
    }

    private fun getPluginWriter(coreFile: File, pluginName: String): BodsRdfWriter {
        return pluginWriters[PluginWriterKey(coreFile, pluginName)]!!
    }
}

internal data class PluginWriterKey(
    val coreFile: File,
    val pluginName: String
)