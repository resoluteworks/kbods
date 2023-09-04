package org.kbods.rdf

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Literal
import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.vocabulary.FOAF
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.XSD
import org.kbods.rdf.interest.interestsToRdf
import org.kbods.read.BodsStatement
import org.kbods.read.BodsStatementType
import org.kbods.read.interestEndDate
import org.rdf4k.add
import org.rdf4k.iri
import org.rdf4k.literal

fun BodsStatement.iri(): IRI = BodsRdf.RESOURCE.iri(this.id)
fun BodsStatement.bodsEntityType(): IRI = BodsRdf.VOCABULARY.iri(json.string("entityType")!!.capitalize())
internal val valueFactory = SimpleValueFactory.getInstance()
internal fun String.literalDate(): Literal {
    return valueFactory.createLiteral(this, XSD.DATE)
}

internal fun List<BodsStatement>.coreRdfStatements(config: BodsRdfConfig = BodsRdfConfig()): List<Statement> {
    val statements = mutableListOf<Statement>()
    forEach { statement ->
        statements.addAll(statement.coreRdfStatements(config))
    }
    return statements
}

/**
 * Returns the core RDF statements for this BodsStatement. Doesn't include any plugin statements.
 * For internal use only.
 */
internal fun BodsStatement.coreRdfStatements(config: BodsRdfConfig = BodsRdfConfig()): List<Statement> {
    val statements = mutableListOf<Statement>()

    val typeStatements = when (this.statementType) {
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

    statements.addAll(typeStatements)
    replacesStatements.forEach {
        statements.add(this.iri(), BodsRdf.PROP_REPLACES_STATEMENTS, BodsRdf.RESOURCE.iri(it))
    }

    return statements
}

private fun BodsStatement.processEntity(config: BodsRdfConfig): List<Statement> {
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
        this.nationalities.forEach { nationalityCode ->
            statements.add(statementRes, BodsRdf.PROP_NATIONALITY_CODE, nationalityCode.literal(), config.graph)
        }
    }
    return statements
}

private fun BodsStatement.processOwnershipCtrlStatement(config: BodsRdfConfig, interestedPartyId: String?): List<Statement> {
    val statements = mutableListOf<Statement>()

    val totalInterests = interests.size
    val expiredInterests = interests.count { it.interestEndDate() != null }
    val nonExpiredInterests = totalInterests - expiredInterests

    // We only process this interest statement if
    // - there are zero interests (it could be an unspecified interest, we don't want to ignore the statement)
    // - OR there are non-expired interests
    // - OR config allows importing expired interests
    if (totalInterests == 0
            || nonExpiredInterests > 0
            || (nonExpiredInterests == 0 && config.importExpiredInterests)
    ) {

        val targetEntity = BodsRdf.RESOURCE.iri(subjectId!!)
        val interestedParty = if (interestedPartyId != null) BodsRdf.RESOURCE.iri(interestedPartyId) else valueFactory.createBNode()
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
