package org.kbods.read

import org.kbods.utils.TempDir
import org.kbods.utils.resourceAsInput
import org.kbods.utils.resourceAsString
import org.testng.annotations.Test

class BodsReadTest {

    @Test
    fun `read statements from input stream`() {
        val collector = StatementsCollector()
        resourceAsInput("statements.jsonl").readBodsStatements(collector)
        testStatements(collector.statements)
    }

    @Test
    fun `read statements from file`() {
        TempDir().use { tempDir ->
            val jsonlFile = tempDir.newFile()
            jsonlFile.writeText(resourceAsString("statements.jsonl"))
            val collector = StatementsCollector()
            jsonlFile.readBodsStatements(collector)
            testStatements(collector.statements)
        }
    }

    @Test
    fun `input stream statement sequence`() {
        val statements = mutableListOf<BodsStatement>()
        resourceAsInput("statements.jsonl").useBodsStatementsSequence { sequence ->
            sequence.forEach { statement ->
                statements.add(statement)
            }
        }
        testStatements(statements)
    }

    @Test
    fun `input stream statement sequence - chunked`() {
        val statements = mutableListOf<BodsStatement>()
        resourceAsInput("statements.jsonl").useBodsStatementsSequence { sequence ->
            sequence.chunked(15).forEach { batch ->
                statements.addAll(batch)
            }
        }
        testStatements(statements)
    }

    @Test
    fun `file statement sequence`() {
        TempDir().use { tempDir ->
            val jsonlFile = tempDir.newFile()
            jsonlFile.writeText(resourceAsString("statements.jsonl"))

            val statements = mutableListOf<BodsStatement>()
            jsonlFile.useBodsStatementsSequence { sequence ->
                sequence.forEach { statement ->
                    statements.add(statement)
                }
            }
            testStatements(statements)
        }
    }

}

