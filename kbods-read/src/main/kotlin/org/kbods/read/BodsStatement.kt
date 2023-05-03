package org.kbods.read

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import java.io.StringReader

class BodsStatement(val jsonString: String) {

    val json = jsonParser.parseJsonObject(StringReader(jsonString))
    val id = json.string("statementID")!!

    val isEntity: Boolean = json.string("statementType") == "entityStatement"
    val isPerson: Boolean = json.string("statementType") == "personStatement"
    val isOwnershipCtrl: Boolean = json.string("statementType") == "ownershipOrControlStatement"
    val statementType: BodsStatementType =
        if (isEntity) {
            BodsStatementType.ENTITY
        } else if (isPerson) {
            BodsStatementType.PERSON
        } else {
            BodsStatementType.OWNERSHIP_CTRL
        }

    val name: String =
        if (json.containsKey("name")) {
            json.string("name")!!
        } else {
            json.array<JsonObject>("names")?.firstOrNull()?.string("fullName") ?: "UNKNOWN"
        }

    val subjectId: String? = json.obj("subject")?.string("describedByEntityStatement")
    val sourceType: String? = json.obj("source")?.array<String>("type")?.joinToString(";")
    val jurisdictionCode: String? = json.obj("incorporatedInJurisdiction")?.string("code")
    val statementDate: String? = json.string("statementDate")
    val personType: String? = json.string("personType")
    val nationalities: List<String> = json.array<JsonObject>("nationalities")?.map { it.string("code")!! } ?: emptyList()
    val interests: List<JsonObject> = json.array<JsonObject>("interests")?.toList() ?: emptyList()

    val interestedPartyId: String?
        get() {
            val ip = json.obj("interestedParty")
            return when {
                ip == null -> null
                ip.containsKey("describedByEntityStatement") -> ip.string("describedByEntityStatement")!!
                ip.containsKey("describedByPersonStatement") -> ip.string("describedByPersonStatement")!!
                else -> {
                    null
                }
            }
        }

    fun identifier(scheme: String): String? {
        return json.array<JsonObject>("identifiers")
            ?.filter { it.string("scheme") == scheme }
            ?.map { it.string("id") }
            ?.firstOrNull()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BodsStatement

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {
        private val jsonParser = Klaxon()
    }
}