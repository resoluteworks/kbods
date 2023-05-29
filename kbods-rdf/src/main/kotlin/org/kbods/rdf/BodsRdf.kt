package org.kbods.rdf

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.vocabulary.FOAF
import org.eclipse.rdf4j.model.vocabulary.OWL
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.eclipse.rdf4j.model.vocabulary.XSD
import org.kbods.rdf.plugins.CompaniesHouseRefPlugin
import org.rdf4k.iri
import org.rdf4k.namespace

object BodsRdf {
    val VOCABULARY = "http://bods.openownership.org/vocabulary/".namespace("bods")
    val RESOURCE = "http://bods.openownership.org/resource/".namespace("bodsr")

    fun entityType(bodsEntityType: String): IRI = VOCABULARY.iri(bodsEntityType.codeListToType())

    val TYPE_PERSON = VOCABULARY.iri("Person")
    val TYPE_ENTITY = VOCABULARY.iri("Entity")
    val TYPE_OWNERSHIP_CTRL_STATEMENT = VOCABULARY.iri("OwnershipCtrlStatement")

    val PROP_REPLACES_STATEMENTS = VOCABULARY.iri("replacesStatements")

    val PROP_NATIONALITY_CODE = VOCABULARY.iri("nationalityCode")

    val PROP_INTERESTED_PARTY = VOCABULARY.iri("hasInterestedParty")
    val PROP_SUBJECT = VOCABULARY.iri("hasSubject")
    val PROP_STATES_INTEREST = VOCABULARY.iri("statesInterest")
    val PROP_STATEMENT_DATE = VOCABULARY.iri("statementDate")
    val PROP_STATEMENT_ID = VOCABULARY.iri("statementId")
    val PROP_STATEMENT_SOURCE_TYPE = VOCABULARY.iri("sourceType")

    val PROP_INTEREST_TYPE = VOCABULARY.iri("interestType")
    val PROP_INTEREST_START_DATE = VOCABULARY.iri("startDate")
    val PROP_INTEREST_DETAILS = VOCABULARY.iri("interestDetails")
    val PROP_INTEREST_END_DATE = VOCABULARY.iri("endDate")
    val PROP_INTEREST_SHARES_EXACT = VOCABULARY.iri("sharesExact")
    val PROP_INTEREST_SHARES_MIN = VOCABULARY.iri("sharesMin")
    val PROP_INTEREST_SHARES_MAX = VOCABULARY.iri("sharesMax")

    val PROP_OWNS_OR_CONTROLS = VOCABULARY.iri("ownsOrControls")
    val PROP_JURISDICTION = VOCABULARY.iri("jurisdictionCode")
    val PROP_PERSON_TYPE = VOCABULARY.iri("personType")

    val REQUIRED_NAMESPACES = listOf(
        RDF.NS,
        RDFS.NS,
        OWL.NS,
        XSD.NS,
        FOAF.NS,
        VOCABULARY,
        RESOURCE,
        CompaniesHouseRefPlugin.CH_NAMESPACE
    )
}

private fun String.codeListToType(): String {
    return split("-")
        .map { it.capitalize() }
        .joinToString("")
}
