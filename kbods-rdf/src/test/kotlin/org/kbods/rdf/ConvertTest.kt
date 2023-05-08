package org.kbods.rdf

import org.kbods.RdfContainerTest
import org.kbods.utils.resourceAsInput
import org.testng.annotations.Test
import kotlin.test.assertContentEquals

class ConvertTest : RdfContainerTest() {

    @Test
    fun `import from input stream`() {
        val repository = rdfHelper.repository
        repository.connection.use { connection ->
            resourceAsInput("statements.jsonl").import(connection)
        }

        tuples("all-individuals-controlling-an-entity.sparql", 31)

        val names = tuples("names-controlling-8214028453689704336.sparql", 2)
            .map { it.str("name") }
        assertContentEquals(listOf("Adam Chavasse", "Paul Sean Allan"), names.sorted())
        assertCount("count-foaf-name.sparql", 62)
        assertCount("count-interest-details.sparql", 80)
    }

    @Test
    fun `import relationships only`() {
        val repository = rdfHelper.repository
        repository.connection.use { connection ->
            val config = BodsRdfConfig(
                relationshipsOnly = true,
                importExpiredInterests = false
            )
            resourceAsInput("statements.jsonl").import(
                connection = connection,
                config = config
            )
        }

        assertCount("count-foaf-name.sparql", 0)
        assertCount("count-interest-details.sparql", 0)
    }
}
