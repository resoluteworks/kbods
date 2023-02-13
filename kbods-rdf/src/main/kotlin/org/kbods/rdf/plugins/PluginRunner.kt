package org.kbods.rdf.plugins

import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.RDFWriter
import org.eclipse.rdf4j.rio.Rio
import org.kbods.rdf.BodsRdf
import org.kbods.rdf.BodsRdfConfig
import org.kbods.rdf.utils.write
import org.kbods.read.BodsStatement
import java.io.File
import java.io.OutputStream

abstract class PluginRunner(val config: BodsRdfConfig) : AutoCloseable {

    fun runPlugins(bodsStatements: List<BodsStatement>) {
        bodsStatements.forEach { runPlugins(it) }
    }

    fun runPlugins(bodsStatement: BodsStatement) {
        val statementType = bodsStatement.statementType
        config.plugins[statementType]?.forEach { plugin ->
            val statements = plugin.generateStatements(bodsStatement)
            doWrite(plugin, statements)
        }
    }

    abstract fun doWrite(plugin: BodsConvertPlugin, statements: List<Statement>)

    companion object {
        fun connection(config: BodsRdfConfig, connection: RepositoryConnection): PluginRunner {
            return ConnectionPluginRunner(config, connection)
        }

        fun file(config: BodsRdfConfig, outputDir: File, rdfFormat: RDFFormat): PluginRunner {
            return FilePluginRunner(config, outputDir, rdfFormat)
        }
    }
}

class ConnectionPluginRunner(
    config: BodsRdfConfig,
    private val connection: RepositoryConnection
) : PluginRunner(config) {

    override fun doWrite(plugin: BodsConvertPlugin, statements: List<Statement>) {
        connection.add(statements)
    }

    override fun close() {}
}

class FilePluginRunner(
    config: BodsRdfConfig,
    private val outputDir: File,
    private val rdfFormat: RDFFormat,
) : PluginRunner(config) {

    private val contexts = mutableMapOf<String, FilePluginContext>()

    init {
        config.plugins.values.forEach { plugins ->
            plugins.forEach { plugin ->
                val fileName = "bods-rdf-${plugin.name}." + rdfFormat.fileExtensions.first()
                contexts[plugin.name] = FilePluginContext.create(outputDir, fileName, rdfFormat)
            }
        }
    }

    override fun doWrite(plugin: BodsConvertPlugin, statements: List<Statement>) {
        contexts[plugin.name]!!.rdfWriter.write(statements)
    }

    override fun close() {
        contexts.forEach { (_, context) ->
            context.close()
        }
    }
}

internal data class FilePluginContext(
    val outputStream: OutputStream,
    val rdfWriter: RDFWriter,
    val rdfFormat: RDFFormat
) {

    fun close() {
        rdfWriter.endRDF()
        outputStream.close()
    }

    companion object {
        fun create(outputDir: File, fileName: String, rdfFormat: RDFFormat): FilePluginContext {
            val outputStream = File(outputDir, fileName).outputStream()
            val rdfWriter = Rio.createWriter(rdfFormat, outputStream)

            rdfWriter.startRDF()
            BodsRdf.REQUIRED_NAMESPACES
                .forEach { rdfWriter.handleNamespace(it.key, it.value) }

            return FilePluginContext(outputStream, rdfWriter, rdfFormat)
        }
    }
}
