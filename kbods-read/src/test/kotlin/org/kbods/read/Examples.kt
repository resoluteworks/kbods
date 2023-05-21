package org.kbods.read

import java.io.File

class Examples {

    fun test() {
        // Download and parse the latest Open Ownership register
        BodsDownload.latest().readStatements { bodsStatement: BodsStatement ->
            // Process BodsStatement
        }


        // Read a BODS dataset from a local file (JSONL or GZ, decompressed if required)
        File("/path/to/file.jsonl.gz").useBodsStatements { statements ->
            statements.forEach { statement: BodsStatement ->
                // Process BodsStatement
            }
        }
    }
}
