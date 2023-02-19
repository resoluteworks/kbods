package org.kbods.rdf.testutils

import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.impl.TreeModel
import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.config.RepositoryConfig
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.eclipse.rdf4j.rio.helpers.StatementCollector
import org.kbods.rdf.BodsRdf
import org.kbods.utils.resourceAsString
import org.rdf4k.iri
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

class RdfHelper(
    connectionUrl: String,
    private val repositoryName: String = "bods-rdf-" + UUID.randomUUID().toString().lowercase()
) {

    val repository: Repository
    private val repositoryManager = RemoteRepositoryManager(connectionUrl)

    init {
        repositoryManager.init()
        createRepository(repositoryName)
        repository = repositoryManager.getRepository(repositoryName)
    }

    private fun createRepository(repositoryName: String) {
        if (repositoryManager.hasRepositoryConfig(repositoryName)) {
            log.info("Repository $repositoryName exists, skipping.")
            return
        }

        log.info("Creating RDF repository $repositoryName")
        val repoConfigStatements = TreeModel()
        val rdfParser = Rio.createParser(RDFFormat.TURTLE)
        rdfParser.setRDFHandler(StatementCollector(repoConfigStatements))
        val config = resourceAsString("graphdb-repository.ttl").replace("bods-rdf", repositoryName)
        rdfParser.parse(config.byteInputStream(), RepositoryConfigSchema.NAMESPACE)
        val repositoryConfig = RepositoryConfig.create(repoConfigStatements, null)
        repositoryManager.addRepositoryConfig(repositoryConfig)
    }

    fun runTupleQuery(classPathQuery: String, bindings: Map<String, Value> = emptyMap()): List<TupleResultRow> {
        val query = resourceAsString("queries/$classPathQuery")
        println("----")
        println(query)
        println("----")
        return runTupleQueryString(query, bindings)
    }

    fun runTupleQueryFromFile(filePath: String, bindings: Map<String, Value> = emptyMap()): List<TupleResultRow> {
        val query = File(filePath).readText()
        return runTupleQueryString(query, bindings)
    }

    fun runTupleQueryString(queryString: String, bindings: Map<String, Value>): MutableList<TupleResultRow> {
        val rows = mutableListOf<TupleResultRow>()

        repository.connection.use { connection ->
            val query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString)
            bindings.forEach { (name, value) -> query.setBinding(name, value) }
            val tuples = query.evaluate()
            val names = tuples.bindingNames
            tuples.forEach { tuple ->
                rows.add(TupleResultRow(names, tuple))
            }
        }

        return rows
    }

    fun runQueryFileForTarget(queryFilePath: String, targetStr: String, fakeNames: Boolean = false) {
        val rows = runTupleQueryFromFile(
            queryFilePath,
            mapOf("target" to BodsRdf.RESOURCE.iri(targetStr))
        )
        printTuplesRows(rows, fakeNames)
        printTuplesToFile(File("$queryFilePath.csv"), rows, fakeNames)
    }

    fun printTuplesRows(rows: List<TupleResultRow>, fakeNames: Boolean = false) {
        println("===========================")
        val sb = StringBuilder()
        rows.forEachIndexed { rowIndex, row ->
            writeRow(sb, rowIndex, row, fakeNames)
        }
        println(sb.toString())
        println("===========================")
        println("Total: ${rows.size} rows")
        println("===========================")
    }

    fun printTuplesToFile(file: File, rows: List<TupleResultRow>, fakeNames: Boolean = false) {
        val sb = StringBuilder()
        rows.forEachIndexed { rowIndex, row ->
            writeRow(sb, rowIndex, row, fakeNames)
        }
        file.writeText(sb.toString())
    }

    private fun writeRow(
        sb: StringBuilder,
        rowIndex: Int,
        row: TupleResultRow,
        fakeNames: Boolean
    ) {
        if (rowIndex == 0) {
            row.names.forEachIndexed { index, name ->
                sb.append("\"" + name + "\"")
                if (index < row.names.size - 1) {
                    sb.append(",")
                } else {
                    sb.append("\n")
                }
            }
        }
        row.names.forEachIndexed { index, name ->
            sb.append("\"")
            if (row.isIri(name)) {
                sb.append(row.iri(name).localName)
            } else {
                val value = row.str(name)

                if (fakeNames) {
                    sb.append(Fake.fakeCompanyName(value))
                } else {
                    sb.append(value)
                }
            }
            sb.append("\"")
            if (index < row.names.size - 1) {
                sb.append(",")
            }
        }
        sb.append("\n")
    }

    companion object {
        private val log = LoggerFactory.getLogger(RdfHelper::class.java)
    }
}

