package org.kbods.elasticsearch

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.kbods.rdf.toRdf
import org.kbods.read.BodsDownload
import java.io.File

class Examples : ElasticsearchContainerTest() {

    fun test() {
        // Download, unzip and import the latest Open Ownership register to Elasticsearch
        BodsDownload.latest().import(
            elasticsearchClient = esClient,
            index = "my-index",
            batchSize = 100
        )

        // Import a local JSONL file to Elasticsearch
        val jsonlFile = File("/path/to/statemenets.jsonl")
        esClient.importBodsStatements(jsonlFile, "myindex", 100)
    }

    fun `Download and import the latest Open Ownership register, import to Elasticsearch and RDF repository`() {
        val repositoryUrl = "http://localhost:7200"
        val index = "myindex"
        val repositoryManager = RemoteRepositoryManager(repositoryUrl)
        val repository = repositoryManager.getRepository("bods-rdf")
        repository.connection.use { connection ->
            BodsDownload.latest().useStatementSequence { sequence ->
                sequence.chunked(1000).forEach { batch ->
                    esClient.writeBodsStatements(batch, index)
                    connection.add(batch.toRdf())
                }
            }
        }
    }
}
