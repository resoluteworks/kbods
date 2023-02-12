package org.kbods.mongodb

import org.kbods.read.BodsDownload
import java.io.File

class Examples : MongoContainerTest() {

    fun test() {
        // Download, unzip and import the latest Open Ownership register to MongoDB
        val collection = mongoClient.getDatabase("mydb").getCollection("mycollection")
        BodsDownload.latest().import(
            collection = collection,
            batchSize = 100
        )

        // Import a local JSONL file to MongoDB
        val jsonlFile = File("/path/to/statemenets.jsonl")
        collection.importBodsStatements(jsonlFile, 100)
    }
}
