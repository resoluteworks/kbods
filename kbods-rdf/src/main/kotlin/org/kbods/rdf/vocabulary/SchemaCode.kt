package org.kbods.rdf.vocabulary

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.rdf4k.literal

internal data class SchemaCode(
    val code: String,
    val title: String,
    val description: String
)

internal fun List<SchemaCode>.addToModel(
    model: Model,
    subjectType: IRI,
    subjectCreator: (String) -> IRI
) {
    forEach { schemaCode ->
        val subject = subjectCreator(schemaCode.code)
        model.add(subject, RDF.TYPE, RDFS.CLASS)
        model.add(subject, RDFS.SUBCLASSOF, subjectType)
        model.add(subject, RDFS.LABEL, schemaCode.title.literal())
        model.add(subject, RDFS.COMMENT, schemaCode.description.literal())
    }
}
