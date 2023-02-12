package org.kbods.mongodb

import org.kbods.utils.TempDir
import org.kbods.utils.resourceAsInput
import org.kbods.utils.resourceAsString
import org.testng.annotations.Test

class MongoImportTest : MongoContainerTest() {

    @Test
    fun `import from input stream`() {
        val collection = newCollection()
        collection.importBodsStatements(resourceAsInput("statements.jsonl"), 100)
        testStatements(collection)
    }

    @Test
    fun `import from file`() {
        TempDir().use { tempDir ->
            val jsonlFile = tempDir.newFile()
            jsonlFile.writeText(resourceAsString("statements.jsonl"))
            val collection = newCollection()
            collection.importBodsStatements(jsonlFile, 100)
            testStatements(collection)
        }
    }

    @Test
    fun `import from input stream - various batch sizes`() {
        fun testBatch(batchSize: Int) {
            val collection = newCollection()
            collection.importBodsStatements(resourceAsInput("statements.jsonl"), batchSize)
            testStatements(collection)
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
