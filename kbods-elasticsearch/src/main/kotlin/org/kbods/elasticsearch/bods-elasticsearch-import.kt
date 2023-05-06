package org.kbods.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.BulkRequest
import co.elastic.clients.json.JsonData
import com.beust.klaxon.JsonObject
import org.kbods.read.BodsDownload
import org.kbods.read.BodsStatement
import org.kbods.read.useBodsStatementsSequence
import java.io.File
import java.io.InputStream

fun BodsDownload.import(
    elasticsearchClient: ElasticsearchClient,
    index: String,
    batchSize: Int,
    patchJson: ((BodsStatement, JsonObject) -> JsonObject)? = null
) {
    this.useStatementSequence { sequence ->
        sequence.chunked(batchSize).forEach { batch ->
            elasticsearchClient.writeBodsStatements(index, batch, patchJson)
        }
    }
}

fun ElasticsearchClient.importBodsStatements(
    jsonlFile: File,
    index: String, batchSize: Int,
    patchJson: ((BodsStatement, JsonObject) -> JsonObject)? = null
) {
    jsonlFile.inputStream().use { inputStream ->
        this.importBodsStatements(inputStream, index, batchSize, patchJson)
    }
}

fun ElasticsearchClient.importBodsStatements(
    inputStream: InputStream,
    index: String,
    batchSize: Int,
    patchJson: ((BodsStatement, JsonObject) -> JsonObject)? = null
) {
    inputStream.useBodsStatementsSequence { sequence ->
        sequence.chunked(batchSize).forEach { batch ->
            this.writeBodsStatements(index, batch, patchJson)
        }
    }
}

fun ElasticsearchClient.writeBodsStatements(
    index: String,
    batch: List<BodsStatement>,
    patchJson: ((BodsStatement, JsonObject) -> JsonObject)? = null
) {
    val bulkRequest = BulkRequest.Builder()
    batch.forEach { statement ->
        bulkRequest.operations { opBuilder ->
            opBuilder.index {
                val json = JsonData.fromJson(statement.jsonString(patchJson))
                it.index(index)
                    .id(statement.id)
                    .document(json)
            }
        }
    }

    this.bulk(bulkRequest.build())
        .checkErrors()
}
