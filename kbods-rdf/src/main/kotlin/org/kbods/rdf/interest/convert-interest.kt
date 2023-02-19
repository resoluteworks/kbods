package org.kbods.rdf.interest

import com.beust.klaxon.JsonObject
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Literal
import org.eclipse.rdf4j.model.Statement
import org.kbods.rdf.BodsRdf
import org.kbods.rdf.BodsRdfConfig
import org.kbods.rdf.literalDate
import org.kbods.read.BodsStatement
import org.kbods.read.interestEndDate
import org.kbods.read.interestStartDate
import org.kbods.utils.safeDouble
import org.rdf4k.add
import org.rdf4k.iri
import org.rdf4k.literal
import org.rdf4k.literalDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal fun interestsToRdf(
    bodsStatement: BodsStatement,
    interestStatementObject: IRI,
    config: BodsRdfConfig
): List<Statement> {

    val statements = mutableListOf<Statement>()

    statements.add(interestStatementObject, BodsRdf.PROP_STATEMENT_ID, bodsStatement.id.literal(), config.graph)
    if (bodsStatement.statementDate != null) {
        statements.add(interestStatementObject, BodsRdf.PROP_STATEMENT_DATE, bodsStatement.statementDate!!.literalDate(), config.graph)
    }

    if (bodsStatement.sourceType != null) {
        statements.add(interestStatementObject, BodsRdf.PROP_STATEMENT_SOURCE_TYPE, bodsStatement.sourceType!!.literal(), config.graph)
    }

    bodsStatement.interests
        .forEachIndexed { index, interestJson ->
            val interestObject = BodsRdf.RESOURCE.iri("${bodsStatement.id}_$index")
            statements.add(interestObject, BodsRdf.PROP_INTEREST_TYPE, interestJson.string("type")!!.literal(), config.graph)

            val interestDetails = interestJson.string("details")
            if (interestDetails != null) {
                statements.add(interestObject, BodsRdf.PROP_INTEREST_DETAILS, interestDetails.literal(), config.graph)
            }

            val startDate = interestJson.interestStartDate()
            if (startDate != null) {
                statements.add(interestObject, BodsRdf.PROP_INTEREST_START_DATE, startDate.literalDate(), config.graph)
            }

            val endDate = interestJson.interestEndDate()
            if (endDate != null) {
                statements.add(interestObject, BodsRdf.PROP_INTEREST_END_DATE, endDate.literalDate(), config.graph)
            }

            statements.add(interestStatementObject, BodsRdf.PROP_STATES_INTEREST, interestObject, config.graph)
            statements.addAll(sharesStatements(interestObject, interestJson, config.graph))
        }

    return statements
}

internal fun sharesStatements(interestObject: IRI, interestJson: JsonObject, graph: IRI?): List<Statement> {
    val statements = mutableListOf<Statement>()
    if (interestJson.containsKey("share")) {
        val shareObj = interestJson.obj("share")!!
        if (shareObj.containsKey("exact")) {
            val exact = shareObj.safeDouble("exact").literalDecimal()
            statements.add(interestObject, BodsRdf.PROP_INTEREST_SHARES_EXACT, exact, graph)
            statements.add(interestObject, BodsRdf.PROP_INTEREST_SHARES_MIN, exact, graph)
            statements.add(interestObject, BodsRdf.PROP_INTEREST_SHARES_MAX, exact, graph)
        } else {
            if (shareObj.containsKey("minimum")) {
                statements.add(interestObject, BodsRdf.PROP_INTEREST_SHARES_MIN, shareObj.safeDouble("minimum").literalDecimal(), graph)
            }
            if (shareObj.containsKey("maximum")) {
                statements.add(interestObject, BodsRdf.PROP_INTEREST_SHARES_MAX, shareObj.safeDouble("maximum").literalDecimal(), graph)
            }
        }
    }
    return statements
}
