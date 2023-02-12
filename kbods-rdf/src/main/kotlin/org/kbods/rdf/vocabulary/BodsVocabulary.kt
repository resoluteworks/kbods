package org.kbods.rdf.vocabulary

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.impl.LinkedHashModel
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.RDFWriter
import org.eclipse.rdf4j.rio.Rio
import org.eclipse.rdf4j.rio.helpers.StatementCollector
import org.kbods.rdf.BodsRdf
import org.kbods.rdf.utils.resourceAsRdfModel
import org.kbods.utils.*
import org.slf4j.LoggerFactory
import java.io.File
import java.io.OutputStream
import java.io.StringReader

object BodsVocabulary {

    private const val CODE = "code"
    private const val TITILE = "title"
    private const val DESCRIPTION = "description"

    private val log = LoggerFactory.getLogger(BodsVocabulary::class.java)
    private val httpClient = httpClient()

    fun write(repository: Repository, schemaVersion: BodsSchemaVersion = BodsSchemaVersion.BULK_REGISTER_VERSION) {
        repository.connection.use { connection ->
            write(connection, schemaVersion)
        }
    }

    fun write(connection: RepositoryConnection, schemaVersion: BodsSchemaVersion = BodsSchemaVersion.BULK_REGISTER_VERSION) {
        connection.add(loadVocabulary(schemaVersion))
    }

    fun write(outputFile: File, schemaVersion: BodsSchemaVersion = BodsSchemaVersion.BULK_REGISTER_VERSION) {
        outputFile.outputStream().use { outputStream ->
            write(outputStream, schemaVersion)
        }
    }

    fun write(outputStream: OutputStream, schemaVersion: BodsSchemaVersion = BodsSchemaVersion.BULK_REGISTER_VERSION) {
        val writer = Rio.createWriter(RDFFormat.TURTLE, outputStream)
        writer.startRDF()
        write(writer, schemaVersion)
        writer.endRDF()
    }

    fun write(writer: RDFWriter, schemaVersion: BodsSchemaVersion = BodsSchemaVersion.BULK_REGISTER_VERSION) {
        BodsRdf.REQUIRED_NAMESPACES
            .forEach { writer.handleNamespace(it.key, it.value) }

        loadVocabulary(schemaVersion)
            .forEach { statement -> writer.handleStatement(statement) }
    }

    private fun loadVocabulary(schemaVersion: BodsSchemaVersion): Model {
        val vocabularyResource = "bods-rdf-vocabulary/bods-vocabulary-${schemaVersion.versionString}.ttl"

        if (resourceExists(vocabularyResource)) {
            return resourceAsRdfModel(vocabularyResource)

        } else {
            TempDir().use { tempDir ->
                val url = "https://github.com/openownership/data-standard/zipball/${schemaVersion.versionString}"
                log.info("Downloading schema release from $url")
                val response = httpClient.get(url).checkOk()

                // Unzip the release
                val tempZip = tempDir.newFile()
                val tempUnzipDir = tempDir.newDirectory()
                tempUnzipDir.mkdirs()
                tempZip.outputStream().use { os ->
                    response.body!!.byteStream().copyTo(os)
                }
                tempZip.unzip(tempUnzipDir)

                val packageDir = tempUnzipDir.listFiles()
                    .find { it.name.startsWith("openownership-data-standard") }!!

                val model = LinkedHashModel()
                addTopLevelDefinitions(model)
                codeList(packageDir, "entityType.csv")
                    .addToModel(model, BodsRdf.TYPE_ENTITY) { BodsRdf.entityType(it) }
                return model
            }
        }
    }

    private fun addTopLevelDefinitions(model: Model) {
        val rdfParser = Rio.createParser(RDFFormat.TURTLE)
        rdfParser.setRDFHandler(StatementCollector(model))
        rdfParser.parse(resourceAsInput("vocabulary-base.ttl"), RepositoryConfigSchema.NAMESPACE)
    }

    private fun codeList(packageDir: File, codeListCsv: String): List<SchemaCode> {
        return csvRows(packageDir, "schema/codelists/$codeListCsv")
            .map { row ->
                SchemaCode(
                    code = row[CODE]!!,
                    title = row[TITILE]!!,
                    description = row[DESCRIPTION] ?: ""
                )
            }
    }

    private fun csvRows(packageDir: File, file: String): List<Map<String, String>> {
        val csvFormat = CSVFormat.RFC4180.builder().build()
        val text = File(packageDir, file)
            .readText()
            .replace(",\\s+\"".toRegex(), ",\"") // Some CSVs are not correctly formatted
        val rows = CSVParser(StringReader(text), csvFormat)
            .toList()
        val header = rows[0]
        return rows.subList(1, rows.size)
            .map { row ->
                row.mapIndexed { index, value ->
                    header.get(index) to value
                }.toMap()
            }
    }
}
