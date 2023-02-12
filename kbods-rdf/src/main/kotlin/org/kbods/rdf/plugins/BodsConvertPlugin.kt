package org.kbods.rdf.plugins

import org.eclipse.rdf4j.model.Statement
import org.kbods.read.BodsStatement
import org.kbods.read.BodsStatementType

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
