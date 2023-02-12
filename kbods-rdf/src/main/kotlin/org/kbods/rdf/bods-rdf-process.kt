package org.kbods.rdf

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.model.vocabulary.FOAF
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.kbods.rdf.interest.interestsToRdf
import org.kbods.rdf.utils.add
import org.kbods.rdf.utils.literal
import org.kbods.rdf.utils.valueFactory
import org.kbods.read.BodsStatement
import org.kbods.read.BodsStatementType
import org.kbods.read.interestEndDate

fun BodsStatement.iri(): IRI = BodsRdf.resource(this.id)
fun BodsStatement.bodsEntityType(): IRI = BodsRdf.def(json.string("entityType")!!.capitalize())

fun List<BodsStatement>.toRdf(config: BodsRdfConfig = BodsRdfConfig()): List<Statement> {
    val statements = mutableListOf<Statement>()
    forEach { statement ->
        statements.addAll(statement.toRdf(config))
    }
    return statements
}

fun BodsStatement.toRdf(config: BodsRdfConfig = BodsRdfConfig()): List<Statement> {
    return when (this.statementType) {
        BodsStatementType.ENTITY -> processEntity(config)
        BodsStatementType.PERSON -> processPerson(config)
        BodsStatementType.OWNERSHIP_CTRL -> {
            val interestedPartyId = this.interestedPartyId
            val statements = processOwnershipCtrlStatement(config, interestedPartyId)
            if (interestedPartyId == null) {
                //TODO: Add "unknown" triples
            }
            statements
        }
    }
}

private fun BodsStatement.processEntity(config: BodsRdfConfig): MutableList<Statement> {
    val statements = mutableListOf<Statement>()
    val statementRes = this.iri()
    statements.add(statementRes, RDF.TYPE, this.bodsEntityType(), config.graph)

    if (!config.relationshipsOnly) {
        statements.add(statementRes, FOAF.NAME, this.name.literal(), config.graph)

        val jurisdiction = this.jurisdictionCode
        if (jurisdiction != null) {
            statements.add(statementRes, BodsRdf.PROP_JURISDICTION, jurisdiction.literal(), config.graph)
        }
    }
    return statements
}

private fun BodsStatement.processPerson(config: BodsRdfConfig): List<Statement> {
    val statements = mutableListOf<Statement>()
    val statementRes = this.iri()
    statements.add(statementRes, RDF.TYPE, BodsRdf.TYPE_PERSON, config.graph)
    if (!config.relationshipsOnly) {
        statements.add(statementRes, FOAF.NAME, this.name.literal(), config.graph)
        statements.add(statementRes, BodsRdf.PROP_PERSON_TYPE, this.personType!!.literal(), config.graph)
    }
    return statements
}

private fun BodsStatement.processOwnershipCtrlStatement(config: BodsRdfConfig, interestedPartyId: String?): List<Statement> {
    val statements = mutableListOf<Statement>()

    val totalInterests = interests.size
    val expiredInterests = interests.count { it.interestEndDate() != null } ?: 0
    val nonExpiredInterests = totalInterests - expiredInterests

    // We only process this interest statement if
    // - there are zero interests (it could be an unspecified interest, we don't want to ignore the statement)
    // - OR there are non-expired interests
    // - OR config allows importing expired interests
    if (totalInterests == 0
        || nonExpiredInterests > 0
        || (nonExpiredInterests == 0 && config.importExpiredInterests)
    ) {

        val targetEntity = BodsRdf.resource(subjectId!!)
        val interestedParty = if (interestedPartyId != null) BodsRdf.resource(interestedPartyId) else valueFactory.createBNode()
        val ctrlStatement = iri()

        statements.add(interestedParty, BodsRdf.PROP_OWNS_OR_CONTROLS, targetEntity, config.graph)
        statements.add(ctrlStatement, RDF.TYPE, BodsRdf.TYPE_OWNERSHIP_CTRL_STATEMENT, config.graph)
        statements.add(ctrlStatement, BodsRdf.PROP_INTERESTED_PARTY, interestedParty, config.graph)
        statements.add(ctrlStatement, BodsRdf.PROP_SUBJECT, targetEntity, config.graph)

        if (!config.relationshipsOnly) {
            statements.addAll(interestsToRdf(this, ctrlStatement, config))
        }
    }

    return statements
}
