package org.kbods.elasticsearch

import co.elastic.clients.elasticsearch.core.BulkResponse
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("org.kbods.elasticsearch")

internal fun BulkResponse.checkErrors() {
    if (errors()) {
        log.error("Elasticsearch bulk index error")
        for (item in items()) {
            if (item.error() != null) {
                log.error("Bulk index item error for index ${item.index()} / ${item.id()}: ${item.error()}")
            }
        }
        throw BulkIndexException(this)
    }
}