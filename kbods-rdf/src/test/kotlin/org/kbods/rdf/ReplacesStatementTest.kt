package org.kbods.rdf

import org.kbods.read.BodsStatement
import org.kbods.utils.resourceAsString
import org.rdf4k.iri
import org.rdf4k.statement
import org.testng.annotations.Test
import kotlin.test.assertTrue

class ReplacesStatementTest {

    @Test
    fun replacesStatements() {
        val statement = BodsStatement(resourceAsString("replaces-statements.json"))
        val statements = statement.coreRdfStatements()
        assertTrue { statements.contains(statement(statement.iri(), BodsRdf.PROP_REPLACES_STATEMENTS, BodsRdf.RESOURCE.iri("1542730339269426528"))) }
        assertTrue { statements.contains(statement(statement.iri(), BodsRdf.PROP_REPLACES_STATEMENTS, BodsRdf.RESOURCE.iri("14609729328118672241"))) }
    }
}