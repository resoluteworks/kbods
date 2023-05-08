package org.kbods.rdf.plugins

import org.eclipse.rdf4j.model.Statement
import org.kbods.rdf.BodsRdfWriter
import org.kbods.read.BodsStatement
import org.kbods.read.BodsStatementType
import org.rdf4k.fileRdfFormat
import java.io.File

interface BodsConvertPlugin {
    val name: String
    val statementType: BodsStatementType

    fun generateStatements(bodsStatement: BodsStatement): List<Statement>

    companion object {
        private val allPlugins = mutableMapOf<String, BodsConvertPlugin>()

        init {
            register(CompaniesHouseRefPlugin())
        }

        private fun register(plugin: BodsConvertPlugin) {
            allPlugins[plugin.name] = plugin
        }

        fun getPlugin(name: String): BodsConvertPlugin {
            return allPlugins[name]!!
        }
    }
}

fun BodsConvertPlugin.separateFile(referenceFile: File): BodsRdfWriter {
    val outputDir = referenceFile.parentFile
    val fileName = "${referenceFile.nameWithoutExtension}-${name}." + referenceFile.extension
    val file = File(outputDir, fileName)
    return BodsRdfWriter(file)
}
