package org.kbods.rdf.utils

import org.eclipse.rdf4j.model.IRI

object CompaniesHouseUtils {

    val COMPANIES_HOUSE_NAMESPACE = "http://data.companieshouse.gov.uk/doc/company/"

    fun company(companyNumber: String): IRI {
        return valueFactory.createIRI(COMPANIES_HOUSE_NAMESPACE, companyNumber)
    }
}
