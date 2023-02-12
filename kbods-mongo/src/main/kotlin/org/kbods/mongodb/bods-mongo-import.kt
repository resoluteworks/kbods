package org.kbods.mongodb

import com.mongodb.client.MongoCollection
import org.bson.Document
import org.kbods.read.BodsDownload
import org.kbods.read.BodsStatement
import org.kbods.read.useBodsStatementsSequence
import java.io.File
import java.io.InputStream

fun BodsDownload.import(collection: MongoCollection<Document>, batchSize: Int) {
    this.useStatementSequence { sequence ->
        sequence.chunked(batchSize).forEach { batch ->
            collection.writeBodsStatements(batch)
        }
    }
}

fun MongoCollection<Document>.importBodsStatements(file: File, batchSize: Int) {
    file.inputStream().use { inputStream ->
        this.importBodsStatements(inputStream, batchSize)
    }
}

fun MongoCollection<Document>.importBodsStatements(inputStream: InputStream, batchSize: Int) {
    inputStream.useBodsStatementsSequence { sequence ->
        sequence.chunked(batchSize).forEach { batch ->
            this.writeBodsStatements(batch)
        }
    }
}

fun MongoCollection<Document>.writeBodsStatements(batch: List<BodsStatement>) {
    this.insertMany(batch.map { bodsStatement ->
        val doc = Document.parse(bodsStatement.jsonString)
        doc.append("_id", bodsStatement.id)
        doc
    })
}