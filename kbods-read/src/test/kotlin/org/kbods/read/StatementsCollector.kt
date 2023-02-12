package org.kbods.read

class StatementsCollector : BodsStatementHandler {

    val statements = mutableListOf<BodsStatement>()

    override fun invoke(statement: BodsStatement) {
        statements.add(statement)
    }
}
