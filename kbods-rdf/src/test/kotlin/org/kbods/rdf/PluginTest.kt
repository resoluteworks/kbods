package org.kbods.rdf

import org.eclipse.rdf4j.model.Statement
import org.kbods.RdfContainerTest
import org.kbods.rdf.plugins.BodsConvertPlugin
import org.kbods.read.BodsStatement
import org.kbods.read.BodsStatementType
import org.kbods.utils.resourceAsInput
import org.rdf4k.statement
import org.testng.annotations.Test

class PluginTest : RdfContainerTest() {

    @Test
    fun `entity plugin`() {
        val repository = rdfHelper.repository
        repository.connection.use { connection ->
            val config = BodsRdfConfig()
            config.addPlugin(TestPlugin())
            resourceAsInput("statements.jsonl").import(connection, config)
        }
        assertCount("count-plugin-test-query.sparql", 30)
    }
}

class TestPlugin : BodsConvertPlugin {

    override val name: String = "test"
    override val statementType: BodsStatementType = BodsStatementType.ENTITY

    override fun generateStatements(bodsStatement: BodsStatement): List<Statement> {
        return listOf(statement(bodsStatement.iri(), PREDICATE, OBJECT, null))
    }

    companion object {
        val PREDICATE = valueFactory.createIRI("http://test.com/", "plugin-test-predicate")
        val OBJECT = valueFactory.createIRI("http://test.com/", "1")
    }
}
