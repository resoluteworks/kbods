package org.kbods.rdf

import org.eclipse.rdf4j.model.IRI
import org.kbods.rdf.plugins.BodsConvertPlugin
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
) {

    internal val plugins = mutableMapOf<BodsStatementType, MutableList<BodsConvertPlugin>>()

    fun addPlugin(plugin: BodsConvertPlugin) {
        plugins.putIfAbsent(plugin.statementType, mutableListOf())
        plugins[plugin.statementType]!!.add(plugin)
    }

    companion object {
        private const val DEFAULT_STATEMENT_BATCH_SIZE = 1000
    }
}
