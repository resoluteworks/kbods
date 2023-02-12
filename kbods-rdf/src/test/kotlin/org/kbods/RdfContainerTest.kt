package org.kbods

import org.kbods.rdf.testutils.RdfHelper
import org.kbods.rdf.testutils.TupleResultRow
import org.testcontainers.containers.GenericContainer
import org.testng.Assert.assertEquals
import org.testng.annotations.AfterSuite
import org.testng.annotations.BeforeMethod

abstract class RdfContainerTest {

    lateinit var rdfHelper: RdfHelper

    @AfterSuite
    fun afterAll() {
        ContainerState.terminate()
    }

    @BeforeMethod
    fun beforeMethod() {
        rdfHelper = RdfHelper(ContainerState.repositoryUrl!!)
    }

    fun tuples(queryName: String, expectedCount: Int): List<TupleResultRow> {
        val tuples = rdfHelper.runTupleQuery(queryName)
        println("=== Found ${tuples.size} records for query $queryName")
        tuples.forEach { row ->
            println(row)
        }
        println("=================================")
        assertEquals(tuples.size, expectedCount)
        return tuples
    }

    fun assertCount(queryName: String, expectedCount: Int) {
        val tuples = tuples(queryName, 1)
        assertEquals(tuples.first().int("count"), expectedCount)
    }
}

object ContainerState {
    var repositoryUrl: String? = null
    var container: GenericContainer<*>? = null

    init {
        if (repositoryUrl == null) {
            container = GenericContainer("ontotext/graphdb:10.1.3")
                .withExposedPorts(7200)
            container!!.start()
            repositoryUrl = "http://localhost:${container!!.firstMappedPort}"
            println("RDF Repository URL is $repositoryUrl")
        }
    }

    fun terminate() {
        container!!.stop()
    }
}
