package org.kbods.elasticsearch

import org.kbods.utils.TempDir
import org.kbods.utils.resourceAsInput
import org.kbods.utils.resourceAsString
import org.testng.annotations.Test

class ElasticsearchImportTest : ElasticsearchContainerTest() {

    @Test
    fun `import from input stream`() {
        val index = randomIndex()
        esClient.importBodsStatements(resourceAsInput("statements.jsonl"), index, 100)
        testStatements(index)
    }

    @Test
    fun `import from file`() {
        TempDir().use { tempDir ->
            val jsonlFile = tempDir.newFile()
            jsonlFile.writeText(resourceAsString("statements.jsonl"))
            val index = randomIndex()
            esClient.importBodsStatements(jsonlFile, index, 100)
            testStatements(index)
        }
    }

    @Test
    fun `import from input stream - various batch sizes`() {
        fun testBatch(batchSize: Int) {
            val index = randomIndex()
            esClient.importBodsStatements(resourceAsInput("statements.jsonl"), index, batchSize)
            testStatements(index)
        }

        testBatch(1)
        testBatch(2)
        testBatch(4)
        testBatch(5)
        testBatch(13)
        testBatch(200)
        testBatch(1000)
        testBatch(1345)
        testBatch(10_000)
    }
}
