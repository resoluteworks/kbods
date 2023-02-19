package org.kbods.rdf

import org.kbods.rdf.plugins.PluginRunner
import org.kbods.rdf.vocabulary.BodsVocabulary
import org.kbods.read.BodsDownload
import org.kbods.read.BodsStatement
import org.kbods.read.useBodsStatementsSequence
import org.rdf4k.fileRdfFormat
import org.rdf4k.useRdfWriter
import org.rdf4k.write
import java.io.File

fun BodsDownload.convert(
    outputFile: File,
    config: BodsRdfConfig = BodsRdfConfig(),
    includeVocabulary: Boolean = true
) {
    this.useStatementSequence { sequence ->
        doConvert(sequence, outputFile, config, includeVocabulary)
    }
}

fun File.convert(
    outputFile: File,
    config: BodsRdfConfig = BodsRdfConfig(),
    includeVocabulary: Boolean = true
) {
    this.useBodsStatementsSequence { sequence ->
        doConvert(sequence, outputFile, config, includeVocabulary)
    }
}

private fun doConvert(
    sequence: Sequence<BodsStatement>,
    outputFile: File,
    config: BodsRdfConfig = BodsRdfConfig(),
    includeVocabulary: Boolean = true
) {
    val format = fileRdfFormat(outputFile.name)!!
    outputFile.useRdfWriter(format, BodsRdf.REQUIRED_NAMESPACES) { rdfWriter ->
        PluginRunner.file(config, outputFile.parentFile, format).use { pluginRunner ->
            if (includeVocabulary) {
                BodsVocabulary.write(rdfWriter)
            }
            sequence.forEach { bodsStatement ->
                val rdfStatements = bodsStatement.toRdf(config)
                rdfWriter.write(rdfStatements)
                pluginRunner.runPlugins(bodsStatement)
            }
        }
    }
}

