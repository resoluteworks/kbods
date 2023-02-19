package org.kbods.experimental.companieshouse

import java.io.File

object ChRdfApp {
    @JvmStatic
    fun main(args: Array<String>) {
        val csvFile = File("/Users/cosmin/temp/BasicCompanyDataAsOneFile-2023-02-01.csv")
        val outpuFile = File("/Users/cosmin/temp/BasicCompanyDataAsOneFile-2023-02-01.ttl")
        ChRdfConverter.convertToRdf(csvFile, outpuFile)
    }
}
