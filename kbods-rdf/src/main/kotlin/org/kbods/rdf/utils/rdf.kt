package org.kbods.rdf.utils

import org.eclipse.rdf4j.model.*
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.impl.TreeModel
import org.eclipse.rdf4j.model.vocabulary.XSD
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.RDFWriter
import org.eclipse.rdf4j.rio.Rio
import org.eclipse.rdf4j.rio.helpers.StatementCollector
import org.kbods.utils.resourceAsInput

internal val valueFactory = SimpleValueFactory.getInstance()

internal fun String.literal(): Value = valueFactory.createLiteral(this)
internal fun String.literalDate(): Value = valueFactory.createLiteral(this, XSD.DATE)
internal fun Double.literal(): Value = valueFactory.createLiteral(this)
internal fun Double.literalDecimal(): Value = valueFactory.createLiteral(this.toString(), XSD.DECIMAL)

internal fun statement(s: Resource, p: IRI, o: Value, graph: IRI? = null): Statement {
    return valueFactory.createStatement(s, p, o, graph)
}

internal fun MutableList<Statement>.add(s: Resource, p: IRI, o: Value, graph: IRI? = null) {
    this.add(statement(s, p, o, graph))
}

internal fun resourceAsRdfModel(classpathLocation: String): Model {
    val model = TreeModel()
    val rdfParser = Rio.createParser(RDFFormat.TURTLE)
    rdfParser.setRDFHandler(StatementCollector(model))
    rdfParser.parse(resourceAsInput(classpathLocation), RepositoryConfigSchema.NAMESPACE)
    return model
}

internal fun RDFWriter.write(statements: List<Statement>) {
    statements.forEach { statement ->
        handleStatement(statement)
    }
}
