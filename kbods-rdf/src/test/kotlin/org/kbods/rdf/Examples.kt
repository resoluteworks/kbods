package org.kbods.rdf

import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.kbods.read.BodsDownload
import org.kbods.rdf.vocabulary.BodsSchemaVersion
import org.kbods.rdf.vocabulary.BodsVocabulary
import java.io.File

class Examples {

    fun `Import latest Open Ownership register to an RDF repository`() {
        val repositoryUrl = "http://localhost:7200"
        val repositoryManager = RemoteRepositoryManager(repositoryUrl)
        val repository = repositoryManager.getRepository("bods-rdf")
        repository.connection.use { connection ->
            BodsDownload.latest()
                .import(connection)
        }
    }

    fun `Import latest Open Ownership register to an RDF repository - override settings`() {
        val repositoryUrl = "http://localhost:7200"
        val repositoryManager = RemoteRepositoryManager(repositoryUrl)
        val repository = repositoryManager.getRepository("bods-rdf")
        repository.connection.use { connection ->
            val config = BodsRdfConfig(
                relationshipsOnly = true,
                importExpiredInterests = false,
                graph = SimpleValueFactory.getInstance().createIRI("https://mydomain.com", "mygraph"),
                readBatchSize = 10_000
            )
            BodsDownload.latest()
                .import(connection, config)
        }
    }

    fun `Write the vocabulary to an RDF repository`() {
        val repositoryUrl = "http://localhost:7200"
        val repositoryManager = RemoteRepositoryManager(repositoryUrl)
        val repository = repositoryManager.getRepository("bods-rdf")
        BodsVocabulary.write(repository)
    }

    fun `Write default vocabulary to a file`() {
        BodsVocabulary.write(File("build/bods-vocabulary.ttl"))
    }

    fun `Write a specific vocabulary version to a file`() {
        BodsVocabulary.write(File("build/bods-vocabulary.ttl"), BodsSchemaVersion.V_0_2_0)
    }
}
