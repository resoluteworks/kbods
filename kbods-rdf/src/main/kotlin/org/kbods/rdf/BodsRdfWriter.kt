package org.kbods.rdf

import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.RDFWriter
import org.eclipse.rdf4j.rio.Rio
import org.rdf4k.fileRdfFormat
import org.rdf4k.write
import java.io.BufferedOutputStream
import java.io.File
import java.io.OutputStream

const val FILE_OUTPUT_BUFFER_SIZE = 16 * 1024 * 1024

class BodsRdfWriter(
    val outputFile: File,
    val bufferSize: Int = FILE_OUTPUT_BUFFER_SIZE
) : AutoCloseable {

    val outputStream: OutputStream
    val rdfWriter: RDFWriter
    val rdfFormat: RDFFormat

    init {
        outputStream = outputFile.outputStream()
        rdfFormat = fileRdfFormat(outputFile.name)!!
        rdfWriter = Rio.createWriter(rdfFormat, BufferedOutputStream(outputStream, bufferSize))
        rdfWriter.startRDF()
        BodsRdf.REQUIRED_NAMESPACES
            .forEach { rdfWriter.handleNamespace(it.prefix, it.name) }
    }

    override fun close() {
        rdfWriter.endRDF()
        outputStream.close()
    }

    fun write(statements: List<Statement>) {
        rdfWriter.write(statements)
    }
}

fun Collection<BodsRdfWriter>.close(){
    forEach { it.close() }
}
