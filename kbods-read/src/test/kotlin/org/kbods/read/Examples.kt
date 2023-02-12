package org.kbods.read

class Examples {

    fun test() {
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
    }
}
