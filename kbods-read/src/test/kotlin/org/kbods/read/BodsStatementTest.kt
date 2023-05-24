package org.kbods.read

import org.kbods.utils.resourceAsString
import kotlin.test.Test
import kotlin.test.assertEquals

class BodsStatementTest {

    @Test
    fun replacesStatements() {
        val statement = BodsStatement(resourceAsString("replaces-statements.json"))
        assertEquals(statement.replacesStatements, listOf("1542730339269426528", "14609729328118672241"))
    }
}
