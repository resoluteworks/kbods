package org.kbods.rdf

import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.RDFWriterRegistry
import org.eclipse.rdf4j.rio.Rio
import org.kbods.rdf.plugins.PluginRunner
import org.kbods.rdf.utils.write
import org.kbods.rdf.vocabulary.BodsVocabulary
import org.kbods.read.BodsDownload
import org.kbods.read.BodsStatement
import org.kbods.read.useBodsStatementsSequence
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
    val format = RDFFormat.matchFileName(outputFile.name, RDFWriterRegistry.getInstance().keys).get()
    outputFile.outputStream().use { outputStream ->
        PluginRunner.file(config, outputFile.parentFile, format).use { pluginRunner ->
            val rdfWriter = Rio.createWriter(format, outputStream)
            rdfWriter.startRDF()
            BodsRdf.REQUIRED_NAMESPACES
                .forEach { rdfWriter.handleNamespace(it.key, it.value) }

            if (includeVocabulary) {
                BodsVocabulary.write(rdfWriter)
            }
            sequence.chunked(config.readBatchSize).forEach { batch ->
                val rdfStatements = batch.toRdf(config)
                rdfWriter.write(rdfStatements)
                pluginRunner.runPlugins(batch)
            }
            rdfWriter.endRDF()
        }
    }
}

