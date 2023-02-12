package org.kbods.elasticsearch

import co.elastic.clients.elasticsearch.core.BulkResponse


class BulkIndexException(val docErrors: Collection<IndexErrorDocRef>) : RuntimeException("Elasticsearch bulk index error") {

    constructor(bulkResponse: BulkResponse) : this(errors(bulkResponse))

    data class IndexErrorDocRef(
        val index: String,
        val id: String,
        val reason: String
    )

    companion object {
        private fun errors(bulkResponse: BulkResponse): Collection<IndexErrorDocRef> {
            return bulkResponse.items()
                .filter { it.error() != null }
                .map { item ->
                    IndexErrorDocRef(item.index(), item.id()!!, item.error()!!.reason()!!)
                }
                .toList()
        }
    }
}