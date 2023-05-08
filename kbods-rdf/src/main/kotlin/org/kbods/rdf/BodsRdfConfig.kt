package org.kbods.rdf

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Statement
import org.kbods.rdf.plugins.BodsConvertPlugin
import org.kbods.read.BodsStatement
import org.kbods.read.BodsStatementType

data class BodsRdfConfig(
    /**
     * Will configure the importer to only process entity IDs, their types and relationships. Names,
     * interest details, jurisdictions and other data is ignored. Defaults to `false`.
     */
    val relationshipsOnly: Boolean = false,

    /**
     * Import expired interests, defaults to `true`.
     */
    val importExpiredInterests: Boolean = true,

    /**
     * An `IRI` to specify the RDF graph where the statements will be imported. Defaults to `null` and when not
     * specified the statements will be imported in the default graph.
     */
    val graph: IRI? = null,

    val plugins: Map<BodsStatementType, List<BodsConvertPlugin>> = emptyMap()
) {

    val allPlugins: List<BodsConvertPlugin>

    init {
        allPlugins = mutableListOf()
        plugins.forEach { (type, typePlugins) ->
            allPlugins.addAll(typePlugins)
        }
    }

    fun runPlugins(bodsStatement: BodsStatement, writeStatements: (String, List<Statement>) -> Unit) {
        plugins[bodsStatement.statementType]?.forEach { plugin ->
            val statements = plugin.generateStatements(bodsStatement)
            writeStatements(plugin.name, statements)
        }
    }

    fun withPlugins(pluginNames: List<String>): BodsRdfConfig {
        val plugins = pluginNames.map { BodsConvertPlugin.getPlugin(it) }
        return withPlugins(*plugins.toTypedArray())
    }

    fun withPlugins(vararg plugins: BodsConvertPlugin): BodsRdfConfig {
        val pluginMap = mutableMapOf<BodsStatementType, MutableList<BodsConvertPlugin>>()
        plugins.forEach { plugin ->
            pluginMap.putIfAbsent(plugin.statementType, mutableListOf())
            pluginMap[plugin.statementType]!!.add(plugin)
        }
        return this.copy(plugins = pluginMap)
    }
}
