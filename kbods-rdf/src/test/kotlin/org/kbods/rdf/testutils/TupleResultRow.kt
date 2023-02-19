package org.kbods.rdf.testutils

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.query.BindingSet

data class TupleResultRow(
    val names: List<String>,
    val tuples: BindingSet
) {

    private val data = mutableMapOf<String, Value>()

    init {
        for (name in names) {
            val value = tuples.getValue(name)
            if (value != null) {
                data[name] = value
            }
        }
    }

    fun str(key: String): String {
        return data[key]!!.stringValue()
    }

    fun long(key: String): Long {
        return data[key]!!.stringValue().toLong()
    }

    fun int(key: String): Int {
        return data[key]!!.stringValue().toInt()
    }

    fun iri(key: String): IRI {
        return data[key]!! as IRI
    }

    fun localName(key: String): String {
        return iri(key).localName
    }

    fun hasKey(key: String): Boolean {
        return data.containsKey(key)
    }

    fun isIri(key: String): Boolean {
        return data[key]!!.isIRI
    }

    override fun toString(): String {
        return "[" + data.entries.joinToString(", ") { entry ->
            entry.key + "=" + entry.value
        } + "]"
    }
}
