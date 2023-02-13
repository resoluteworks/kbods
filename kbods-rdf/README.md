# kbods-rdf

This is a JVM library (written in Kotlin) for processing [Open Ownership](https://www.openownership.org/) BODS data 
as [RDF](https://www.w3.org/RDF/). It is an implementation of the [BODS RDF proposal](https://docs.google.com/document/d/1vej-UkK7QtmfKrmU6aD15vceIzJDsCv1jbHCJWgn9hs)
and provides an RDF vocabulary for BODS schema (0.1.0/0.2.0/0.3.0).

The library also provides tooling for processing the [Open Ownership bulk register](https://register.openownership.org/download).
Please note that the current version of the register is only published in the [0.1.0](https://standard.openownership.org/en/0.1.0/)
schema version.

<img width="600" alt="2023-02-04_16-36-38" src="https://user-images.githubusercontent.com/2995576/216779559-64e9e754-efdb-44bd-8b9a-a1f87c643332.png">

## Downloads
Last exported 13/02/2023.
| Dataset | Turtle | BRF |
| --- | --- | --- |
| Open Ownership register data in RDF format | [bods-rdf.ttl.gz](https://bods-rdf.s3-eu-west-1.amazonaws.com/data/bods-rdf.ttl.gz) 1.3GB | [bods-rdf.brf.gz](https://bods-rdf.s3-eu-west-1.amazonaws.com/data/bods-rdf.brf.gz) 2.5GB |
| UK Companies references <sup>1</sup> | [bods-rdf-uk-company-refs.ttl.gz](https://bods-rdf.s3-eu-west-1.amazonaws.com/data/bods-rdf-uk-company-refs.ttl.gz) 130MB | [bods-rdf-uk-company-refs.brf.gz](https://bods-rdf.s3-eu-west-1.amazonaws.com/data/bods-rdf-uk-company-refs.brf.gz) 130MB |

<sup>1</sup> Maps BODS statements for UK entities to the coresponding [UK Companies Identifiers](https://www.data.gov.uk/dataset/5a33338a-e142-4f05-9458-ca7283f410b3/company-identifiers-uris) using `owl:sameAs`. Useful for data linking between Open Ownership and other sources.

## Resources
* [BODS RDF background](https://world.hey.com/cos/an-rdf-vocabulary-for-beneficial-ownership-data-7a762fe1)
* [BODS RDF Proposal](https://docs.google.com/document/d/1vej-UkK7QtmfKrmU6aD15vceIzJDsCv1jbHCJWgn9hs)
* [API Docs](https://cosmin-marginean.github.io/bods-rdf/dokka)
* [Open Ownership Tech Showcase - BODS RDF in Risk & Compliance](https://github.com/cosmin-marginean/bods-rdf/blob/main/docs/OO-TechShowcase-May2022.pdf)
* RDF Vocabulary:
[0.1.0](https://github.com/cosmin-marginean/bods-rdf/blob/main/src/main/resources/vocabulary/bods-vocabulary-0.1.0.ttl),
[0.2.0](https://github.com/cosmin-marginean/bods-rdf/blob/main/src/main/resources/vocabulary/bods-vocabulary-0.2.0.ttl),
[0.3.0](https://github.com/cosmin-marginean/bods-rdf/blob/main/src/main/resources/vocabulary/bods-vocabulary-0.3.0.ttl)

## JVM Usage
#### Gradle dependency
```groovy
repositories {
    maven {
        url "https://resoluteworks-maven.s3-eu-west-1.amazonaws.com"
    }
}

dependencies {
    implementation 'org.kbods:kbods-rdf:0.8.4'
}
```

#### Import the latest Open Ownership register to an RDF repository
This will also write the default schema vocabulary (0.1.0) to the same repository before importing the Open Ownership register.
```kotlin
val repositoryManager = RemoteRepositoryManager(repositoryUrl)
val repository = repositoryManager.getRepository("bods-rdf")
repository.connection.use { connection ->
    BodsDownload.latest()
        .import(connection)
}
```

#### Override import settings
The import behaviour can be configured to optimise the process for certain use cases.

For example, there is an option to only import ownership & control relationships,
while ignoring interest details, entity names, jurisdiction etc. This produces a smaller dataset and can be useful
when the IDs of the entities and the relationships between them are sufficient for querying purposes. Or when these need to be de-referenced from another database.

For further details please see [BodsRdfConfig]([https://cosmin-marginean.github.io/kbods/dokka/kbods-rdf/kbods-rdf/org.kbods.rdf/-bods-rdf-config/index.html)).

```kotlin
val repositoryUrl = "http://localhost:7200"
val repositoryManager = RemoteRepositoryManager(repositoryUrl)
val repository = repositoryManager.getRepository("bods-rdf")
repository.connection.use { connection ->
    val config = BodsRdfConfig(
        relationshipsOnly = true,
        importExpiredInterests = false,
        graph = SimpleValueFactory.getInstance().createIRI("https://mydomain.com", "mygraph"),
        readBatchSize = 1000
    )
    BodsDownload.latest()
        .import(connection, config)
}
```

#### Write the vocabulary to an RDF repository
```kotlin
val repositoryManager = RemoteRepositoryManager(repositoryUrl)
val repository = repositoryManager.getRepository("bods-rdf")
BodsVocabulary.write(repository)
```

## Command line usage

#### Fetch the fat JAR for the latest version
```shell
KBODS_VERSION="0.8.4"
BODS_RDF_JAR="https://resoluteworks-maven.s3-eu-west-1.amazonaws.com/org/kbods/kbods-rdf/${KBODS_VERSION}/kbods-rdf-${KBODS_VERSION}-all.jar"
curl $BODS_RDF_JAR > bods-rdf.jar
```

#### Download, unzip and convert the latest Open Ownership bulk register
```shell
java -jar bods-rdf.jar convert-latest --output=statements.ttl
```

#### Convert a local Open Ownership bulk register to TTL
If the input file has the `.gz` extenzion, this will be unzipped first, and the unpacked file will be automatically deleted at the end of the process.
```shell
java -jar bods-rdf.jar convert --input=statements.latest.jsonl --output=statements.ttl
java -jar bods-rdf.jar convert --input=statements.latest.jsonl.gz --output=statements.ttl
```

#### Exclude vocabulary from output TTL
By default, the BODS schema vocabulary is written in the output TTL file. To disable this, you can pass the `--exclude-vocabulary` flag to
either of `convert` or `convert-latest`
```shell
java -jar bods-rdf.jar convert --input=statements.latest.jsonl --output=statements.ttl --exclude-vocabulary
java -jar bods-rdf.jar convert-latest --output=statements.ttl --exclude-vocabulary
```
