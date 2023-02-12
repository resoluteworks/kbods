package org.kbods.rdf.vocabulary

enum class BodsSchemaVersion(val versionString: String) {
    V_0_1_0("0.1.0"),
    V_0_2_0("0.2.0"),
    V_0_3_0("0.3.0");

    companion object {
        fun fromVersionString(versionString: String): BodsSchemaVersion {
            return BodsSchemaVersion
                .values()
                .find { it.versionString == versionString }
                ?: throw IllegalArgumentException("Unknown schema version: $versionString")
        }

        val BULK_REGISTER_VERSION = V_0_1_0
    }
}
