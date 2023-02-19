package org.kbods.experimental.companieshouse

import org.apache.commons.csv.CSVRecord
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.RDFWriter
import org.kbods.utils.cleanWhitespace
import org.rdf4k.iri
import org.rdf4k.literal
import org.rdf4k.useRdfWriter
import org.rdf4k.write
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ChRdfConverter {

    val FMT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun convertToRdf(inputCsv: File, outputFile: File) {
        outputFile.useRdfWriter(RDFFormat.TURTLE, ChRdf.ALL_NAMESPACES) { rdfWriter ->
            CompaniesHouseBulkDataset.readCsv(inputCsv) { csvRecord ->
                val companyUriStr = csvRecord["URI"].trim()
                if (!companyUriStr.isNullOrEmpty()) {
                    val companyIri = companyUriStr.iri()
                    rdfWriter.write(companyIri, ChRdf.PROP_COMPANY_STATUS, csvRecord.literal("CompanyStatus", true))
                    sicCodes(csvRecord).forEach {
                        rdfWriter.write(companyIri, ChRdf.PROP_SIC_CODE, it.literal())
                    }

                    appendDate(csvRecord, rdfWriter, companyIri, ChRdf.PROP_INCORPORATION_DATE, "IncorporationDate")
                    appendDate(csvRecord, rdfWriter, companyIri, ChRdf.PROP_ACCOUNTS_NEXT_DUE, "Accounts.NextDueDate")

                    val mortgages = csvRecord["Mortgages.NumMortOutstanding"].cleanWhitespace().toInt()
                    rdfWriter.write(companyIri, ChRdf.PROP_MORTG_OUTSTANDING, mortgages.literal())
                }
            }
        }
    }

    private fun appendDate(
        csvRecord: CSVRecord,
        rdfWriter: RDFWriter,
        companyIri: IRI,
        property: IRI,
        header: String
    ) {
        val dateStr = csvRecord[header].cleanWhitespace()
        if (!dateStr.isNullOrEmpty()) {
            val date = LocalDate.from(FMT_DATE.parse(dateStr))
            rdfWriter.write(companyIri, property, date.literal())
        }
    }

    private fun sicCodes(csvRecord: CSVRecord): List<String> {
        return listOf(
            csvRecord["SICCode.SicText_1"].cleanWhitespace(),
            csvRecord["SICCode.SicText_2"].cleanWhitespace(),
            csvRecord["SICCode.SicText_3"].cleanWhitespace(),
            csvRecord["SICCode.SicText_4"].cleanWhitespace()
        ).filter {
            it.isNotEmpty() && it.contains("\\d".toRegex())
        }.map {
            it.split("-")[0].cleanWhitespace()
        }
    }
}

private fun CSVRecord.literal(header: String, upperCase: Boolean = false): Value {
    val cleanWhitespace = this[header].cleanWhitespace()
    return if (upperCase) {
        cleanWhitespace.uppercase().literal()
    } else {
        cleanWhitespace.literal()
    }
}
