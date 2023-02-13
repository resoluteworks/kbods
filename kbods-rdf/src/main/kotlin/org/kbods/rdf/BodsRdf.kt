package org.kbods.rdf

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.vocabulary.FOAF
import org.eclipse.rdf4j.model.vocabulary.OWL
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.eclipse.rdf4j.model.vocabulary.XSD
import org.eclipse.rdf4j.rio.RDFWriter
import org.kbods.rdf.utils.CompaniesHouseUtils
import org.kbods.rdf.utils.valueFactory

object BodsRdf {
    const val NAMESPACE = "http://bods.openownership.org/vocabulary/"
    const val PREFIX = "bods"
    const val RES_NAMESPACE = "http://bods.openownership.org/resource/"
    const val RES_PREFIX = "bods-res"

    fun def(value: String): IRI = valueFactory.createIRI(NAMESPACE, value)
    fun resource(value: String): IRI = valueFactory.createIRI(RES_NAMESPACE, value)

    fun entityType(bodsEntityType: String): IRI = def(bodsEntityType.codeListToType())

    val TYPE_PERSON = def("Person")
    val TYPE_ENTITY = def("Entity")
    val TYPE_OWNERSHIP_CTRL_STATEMENT = def("OwnershipCtrlStatement")

    val PROP_NATIONALITY_CODE = def("nationalityCode")

    val PROP_INTERESTED_PARTY = def("hasInterestedParty")
    val PROP_SUBJECT = def("hasSubject")
    val PROP_STATES_INTEREST = def("statesInterest")
    val PROP_STATEMENT_DATE = def("statementDate")
    val PROP_STATEMENT_ID = def("statementId")
    val PROP_STATEMENT_SOURCE_TYPE = def("sourceType")

    val PROP_INTEREST_TYPE = def("interestType")
    val PROP_INTEREST_START_DATE = def("startDate")
    val PROP_INTEREST_DETAILS = def("interestDetails")
    val PROP_INTEREST_END_DATE = def("endDate")
    val PROP_INTEREST_SHARES_EXACT = def("sharesExact")
    val PROP_INTEREST_SHARES_MIN = def("sharesMin")
    val PROP_INTEREST_SHARES_MAX = def("sharesMax")

    val PROP_OWNS_OR_CONTROLS = def("ownsOrControls")
    val PROP_JURISDICTION = def("jurisdictionCode")
    val PROP_PERSON_TYPE = def("personType")

    val REQUIRED_NAMESPACES = mapOf(
        RDF.PREFIX to RDF.NAMESPACE,
        RDFS.PREFIX to RDFS.NAMESPACE,
        OWL.PREFIX to OWL.NAMESPACE,
        XSD.PREFIX to XSD.NAMESPACE,
        FOAF.PREFIX to FOAF.NAMESPACE,
        PREFIX to NAMESPACE,
        RES_PREFIX to RES_NAMESPACE,
        "ch" to CompaniesHouseUtils.COMPANIES_HOUSE_NAMESPACE
    )
}

private fun String.codeListToType(): String {
    return split("-")
        .map { it.capitalize() }
        .joinToString("")
}
