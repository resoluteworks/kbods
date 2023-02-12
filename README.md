# kbods

[![](https://img.shields.io/github/v/release/cosmin-marginean/kbods?display_name=tag)](https://github.com/cosmin-marginean/kbods/releases)

This is a set of Kotlin libraries for reading and importing beneficial ownership data
in the [BODS](https://standard.openownership.org) JSON format.

The objective is to provide JVM capabilities to process BODS data, as well as enabling applications to import BODS registers with
popular storage solutions.

The [Open Ownership register](https://register.openownership.org/download) is the reference for most of the work in these libraries, and
the tools provided here support the download and ingestion of this register out of the box.

## Modules

| Module | API docs | Description|
| --- | --- | --- |
| kbods-read | [API Docs](https://cosmin-marginean.github.io/kbods/dokka/kbods-read) | Download, unpack and process BODS datasets |
| kbods-elasticsearch | [API Docs](https://cosmin-marginean.github.io/kbods/dokka/kbods-elasticsearch) | Import a BODS dataset to Elasticsearch |
| kbods-mongo | [API Docs](https://cosmin-marginean.github.io/kbods/dokka/kbods-mongo) | Import a BODS dataset to MongoDB |
| [kbods-rdf](kbods-rdf) | [API Docs](https://cosmin-marginean.github.io/kbods/dokka/kbods-rdf) | RDF vocabulary and related tooling. |

## Usage

Add the kbods repository to your Gradle configuration.

```groovy
repositories {
    maven {
        url "https://resoluteworks-maven.s3-eu-west-1.amazonaws.com"
    }
}
```

The libraries all have `kbods-read` as a transitive dependency, but otherwise they're largely independent and can be used
separately or together in the same project.

```shell

dependencies {
    implementation "org.kbods:kbods-read:${kbodsVersion}"
    implementation "org.kbods:kbods-elasticsearch:${kbodsVersion}"
    implementation "org.kbods:kbods-mongo:${kbodsVersion}"
    implementation "org.kbods:kbods-rdf:${kbodsVersion}"
}
```

Since the operations in these libraries are mostly stateless, the design is based on Kotlin extension functions. This
hopefully makes the code more readable and enables a fluent integration.  

### kbods-elasticsearch
```kotlin
// Download, unzip and import the latest Open Ownership register to Elasticsearch
BodsDownload.latest().import(
    elasticsearchClient = esClient,
    index = "my-index",
    batchSize = 100
)

// Import a local JSONL file to Elasticsearch
val jsonlFile = File("/path/to/statemenets.jsonl")
esClient.importBodsStatements(jsonlFile, "myindex", 100)
```

### kbods-mongo
```kotlin
// Download, unzip and import the latest Open Ownership register to MongoDB
val collection = mongoClient.getDatabase("mydb").getCollection("mycollection")
BodsDownload.latest().import(
    collection = collection,
    batchSize = 100
)

// Import a local JSONL file to MongoDB
val jsonlFile = File("/path/to/statemenets.jsonl")
collection.importBodsStatements(jsonlFile, 100)
```

### kbods-rdf
Please refer to the [module's](kbods-rdf) README for complete details.

### kbods-read
This is the base module for reading and loading a BODS register and the rest of the libraries
are built on top of this.

```kotlin
// Download and parse the latest Open Ownership register
BodsDownload.latest().readStatements { bodsStatement ->
    println(bodsStatement.jsonString)
    println(bodsStatement.statementType)
    if (bodsStatement.isOwnershipCtrl) {
        println(bodsStatement.interests)
    }
}

// Using a statements sequence, and optionally batching
BodsDownload.latest().useStatementSequence { sequence ->
    sequence.chunked(100).forEach { batch ->
        batch.forEach { bodsStatement ->
            println(bodsStatement.jsonString)
        }
    }
}

// Download a BODS register from a specific URL
val url = "https://oo-register-production.s3-eu-west-1.amazonaws.com/public/exports/statements.2023-02-01T14:23:22Z.jsonl.gz"
BodsDownload.forUrl(url).readStatements { bodsStatement ->
    println(bodsStatement.jsonString)
}
```

## Advanced usage
### Import the register in Elasticsearch and RDF repository
The focus for [kbods-rdf](kbods-rdf) is to capture relationship between entities and so there are BODS
schema definitions which we don't map to RDF (yet). Because of this, there are situations where the RDF storage
is used exclusively for graph-based queries, while the statements are de-referenced from a primary database.

Below is a very crude example on how to import the BODS register in a Elasticsearch and an RDF repository in the same read
of the register.

```kotlin
val repository = repositoryManager.getRepository("bods-rdf")
repository.connection.use { connection ->
    BodsDownload.latest().useStatementSequence { sequence ->
        sequence.chunked(1000).forEach { batch ->
            esClient.writeBodsStatements(batch, "myindex")
            connection.add(batch.toRdf())
        }
    }
}
```
