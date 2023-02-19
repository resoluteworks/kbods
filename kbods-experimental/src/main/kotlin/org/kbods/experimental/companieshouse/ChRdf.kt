package org.kbods.experimental.companieshouse

import org.rdf4k.iri
import org.rdf4k.namespace

object ChRdf {

    val NAMESPACE_TERMS = "http://www.companieshouse.gov.uk/terms/".namespace("ch-terms")

    val PROP_COMPANY_STATUS = NAMESPACE_TERMS.iri("CompanyStatus")
    val PROP_SIC_CODE = NAMESPACE_TERMS.iri("SICCode")
    val PROP_INCORPORATION_DATE = NAMESPACE_TERMS.iri("IncorporationDate")
    val PROP_ACCOUNTS_NEXT_DUE = NAMESPACE_TERMS.iri("AccountsNextDueDate")
    val PROP_MORTG_OUTSTANDING = NAMESPACE_TERMS.iri("NumMortOutstanding")

    val ALL_NAMESPACES = listOf(NAMESPACE_TERMS)
}