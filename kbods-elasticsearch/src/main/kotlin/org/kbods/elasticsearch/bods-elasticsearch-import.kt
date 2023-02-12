package org.kbods.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.BulkRequest
import co.elastic.clients.json.JsonData
import org.kbods.read.BodsDownload
import org.kbods.read.BodsStatement
import org.kbods.read.useBodsStatementsSequence
import java.io.File
import java.io.InputStream

fun BodsDownload.import(elasticsearchClient: ElasticsearchClient, index: String, batchSize: Int) {
    this.useStatementSequence { sequence ->
        sequence.chunked(batchSize).forEach { batch ->
            elasticsearchClient.writeBodsStatements(batch, index)
        }
    }
}

fun ElasticsearchClient.importBodsStatements(jsonlFile: File, index: String, batchSize: Int) {
    jsonlFile.inputStream().use { inputStream ->
        this.importBodsStatements(inputStream, index, batchSize)
    }
}

fun ElasticsearchClient.importBodsStatements(inputStream: InputStream, index: String, batchSize: Int) {
    inputStream.useBodsStatementsSequence { sequence ->
        sequence.chunked(batchSize).forEach { batch ->
            this.writeBodsStatements(batch, index)
        }
    }
}

fun ElasticsearchClient.writeBodsStatements(batch: List<BodsStatement>, index: String) {
    val bulkRequest = BulkRequest.Builder()
    batch.forEach { statement ->
        bulkRequest.operations { opBuilder ->
            opBuilder.index {
                it.index(index)
                    .id(statement.id)
                    .document(JsonData.fromJson(statement.jsonString))
            }
        }
    }

    this.bulk(bulkRequest.build())
        .checkErrors()
}
