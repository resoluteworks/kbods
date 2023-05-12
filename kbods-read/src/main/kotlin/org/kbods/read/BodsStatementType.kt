package org.kbods.read

enum class BodsStatementType(val type: String) {
    ENTITY("entityStatement"),
    PERSON("personStatement"),
    OWNERSHIP_CTRL("ownershipOrControlStatement")
}