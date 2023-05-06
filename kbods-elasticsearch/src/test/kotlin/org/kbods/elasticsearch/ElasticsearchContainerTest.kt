package org.kbods.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.CountRequest
import co.elastic.clients.elasticsearch.core.GetRequest
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.awaitility.Awaitility.await
import org.elasticsearch.client.RestClient
import org.kbods.read.BodsStatement
import org.kbods.read.BodsStatementType
import org.kbods.read.interestEndDate
import org.kbods.read.interestStartDate
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.AfterSuite
import java.time.Duration
import java.util.*


abstract class ElasticsearchContainerTest {

    val esClient: ElasticsearchClient = ContainerState.client

    @AfterSuite
    fun afterAll() {
        ContainerState.terminate()
    }

    fun randomIndex(): String {
        val index = "index-${UUID.randomUUID()}"
        println("Index is $index")
        return index
    }

    fun doc(index: String, id: String): String {
        val request = GetRequest.Builder().index(index).id(id).build()
        val response = esClient.get(request, ObjectNode::class.java)
        return response.source()!!.toString()
    }

    fun statement(index: String, id: String): BodsStatement {
        return BodsStatement(doc(index, id))
    }

    fun testStatements(index: String, testAllNames: Boolean = false) {
        await()
            .pollInterval(Duration.ofSeconds(1))
            .atMost(Duration.ofSeconds(10)).until {
                val count = esClient.count(CountRequest.Builder().index(index).build()).count()
                count == 1000L
            }

        assertEquals(statement(index, "openownership-register-9473160899263237344").statementType, BodsStatementType.ENTITY)
        assertEquals(statement(index, "openownership-register-4351875629490786609").statementType, BodsStatementType.PERSON)
        assertEquals(statement(index, "openownership-register-10949623906398784527").statementType, BodsStatementType.OWNERSHIP_CTRL)
        assertTrue(statement(index, "openownership-register-10949623906398784527").isOwnershipCtrl)

        assertEquals(
            statement(index, "openownership-register-10949623906398784527").subjectId,
            "openownership-register-12991366811691475077"
        )
        assertEquals(statement(index, "openownership-register-2452143574049712728").statementDate, "2017-08-21")
        assertEquals(statement(index, "openownership-register-13344716475440118974").sourceType, "officialRegister")
        assertEquals(statement(index, "openownership-register-17669746130836775356").jurisdictionCode, "GB")
        assertEquals(statement(index, "openownership-register-17411477780346571462").personType, "knownPerson")
        assertEquals(statement(index, "openownership-register-17507671354761443649").interests.first().interestStartDate(), "2016-07-15")
        assertEquals(statement(index, "openownership-register-17507671354761443649").interests.first().interestEndDate(), "2019-11-19")
        assertEquals(
            statement(index, "openownership-register-631006642991756856").interestedPartyId,
            "openownership-register-17411477780346571462"
        )
        assertEquals(statement(index, "openownership-register-4878504546140740426").name, "CASTLE BASTION LIMITED")
        assertEquals(statement(index, "openownership-register-13077997364453373905").name, "Brian James Wallace")
        assertEquals(statement(index, "openownership-register-12569216474294322485").identifier("GB-COH"), "09083149")

        if (testAllNames) {
            assertEquals(statement(index, "openownership-register-4878504546140740426").allNames, setOf("CASTLE BASTION LIMITED"))
            assertEquals(statement(index, "openownership-register-14062781532659537710").allNames, setOf("Charles Stuart John Barter"))
        }
    }
}

object ContainerState {
    val container: ElasticsearchContainer = ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.6.1")
        .withExposedPorts(9200)
        .withPassword("s3cret")
    val host: HttpHost
    val client: ElasticsearchClient

    init {
        container.envMap.remove("xpack.security.enabled")
        container.start()

        val credentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials("elastic", "s3cret"))
        host = HttpHost("localhost", container.getMappedPort(9200), "https")
        val builder = RestClient.builder(host)
        builder.setHttpClientConfigCallback { clientBuilder ->
            clientBuilder.setSSLContext(container.createSslContextFromCa())
            clientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            clientBuilder
        }
        builder.setNodeSelector { it.first() }

        val transport = RestClientTransport(builder.build(), JacksonJsonpMapper())
        client = ElasticsearchClient(transport)

        println("Elasticsearch host is $host")
    }

    fun terminate() {
        container.stop()
    }
}