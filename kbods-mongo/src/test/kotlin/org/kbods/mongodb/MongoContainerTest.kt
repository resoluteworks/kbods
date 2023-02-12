package org.kbods.mongodb

import com.mongodb.BasicDBObject
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import org.awaitility.Awaitility.await
import org.bson.Document
import org.kbods.read.BodsStatement
import org.kbods.read.BodsStatementType
import org.kbods.read.interestEndDate
import org.kbods.read.interestStartDate
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.AfterSuite
import java.time.Duration
import java.util.*


abstract class MongoContainerTest {

    val mongoClient = ContainerState.mongoClient

    @AfterSuite
    fun afterAll() {
        ContainerState.terminate()
    }

    private fun MongoCollection<Document>.statement(id: String): BodsStatement {
        val query = BasicDBObject()
        query["_id"] = id
        return BodsStatement(this.find(query).first().toJson())
    }

    fun newCollection() = mongoClient.getDatabase("test").getCollection("bods-${UUID.randomUUID()}")

    fun testStatements(collection: MongoCollection<Document>) {
        await()
            .pollInterval(Duration.ofSeconds(1))
            .atMost(Duration.ofSeconds(10)).until {
                val count = collection.countDocuments()
                count == 1000L
            }

        assertEquals(collection.statement("openownership-register-9473160899263237344").statementType, BodsStatementType.ENTITY)
        assertEquals(collection.statement("openownership-register-4351875629490786609").statementType, BodsStatementType.PERSON)
        assertEquals(collection.statement("openownership-register-10949623906398784527").statementType, BodsStatementType.OWNERSHIP_CTRL)
        assertTrue(collection.statement("openownership-register-10949623906398784527").isOwnershipCtrl)

        assertEquals(
            collection.statement("openownership-register-10949623906398784527").subjectId,
            "openownership-register-12991366811691475077"
        )
        assertEquals(collection.statement("openownership-register-2452143574049712728").statementDate, "2017-08-21")
        assertEquals(collection.statement("openownership-register-13344716475440118974").sourceType, "officialRegister")
        assertEquals(collection.statement("openownership-register-17669746130836775356").jurisdictionCode, "GB")
        assertEquals(collection.statement("openownership-register-17411477780346571462").personType, "knownPerson")
        assertEquals(
            collection.statement("openownership-register-17507671354761443649").interests.first().interestStartDate(),
            "2016-07-15"
        )
        assertEquals(collection.statement("openownership-register-17507671354761443649").interests.first().interestEndDate(), "2019-11-19")
        assertEquals(
            collection.statement("openownership-register-631006642991756856").interestedPartyId,
            "openownership-register-17411477780346571462"
        )
        assertEquals(collection.statement("openownership-register-4878504546140740426").name, "CASTLE BASTION LIMITED")
        assertEquals(collection.statement("openownership-register-13077997364453373905").name, "Brian James Wallace")
        assertEquals(collection.statement("openownership-register-12569216474294322485").identifier("GB-COH"), "09083149")
    }

}

object ContainerState {
    val mongoContainer = MongoDBContainer(DockerImageName.parse("mongo:5"))
    val mongoClient: MongoClient

    init {
        mongoContainer.start()
        println("MongoDB connection string: ${mongoContainer.connectionString}")
        mongoClient = MongoClients.create(mongoContainer.connectionString)
    }

    fun terminate() {
        mongoContainer.stop()
    }
}
