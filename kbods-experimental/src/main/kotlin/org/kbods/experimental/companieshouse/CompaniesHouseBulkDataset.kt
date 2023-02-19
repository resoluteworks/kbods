package org.kbods.experimental.companieshouse

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.kbods.utils.grouped
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileReader

object CompaniesHouseBulkDataset {

    fun readCsv(csvFile: File, recordHandler: (CSVRecord) -> Unit) {
        val csvFormat = CSVFormat.RFC4180
            .builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreSurroundingSpaces(true)
            .build()
        var count = 0
        CSVParser(FileReader(csvFile), csvFormat)
            .forEachIndexed { index, csvRecord ->
                recordHandler(csvRecord)
                if (index % 100_000 == 0) {
                    log.info("Processed ${index.grouped()} records")
                }
                count = index
            }
        log.info("Processed a total of ${count.grouped()} records")
    }

    private val log = LoggerFactory.getLogger(CompaniesHouseBulkDataset::class.java)
}