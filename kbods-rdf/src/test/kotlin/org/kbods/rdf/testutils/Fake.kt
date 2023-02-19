package org.kbods.rdf.testutils

import io.github.serpro69.kfaker.faker
import org.kbods.utils.cleanWhitespace
import java.util.*

object Fake {

    fun fakeCompanyName(name: String): String {
        val clean = name.cleanCompanyName().uppercase()
        if (!fakeCompanyName.containsKey(clean)) {
            val fakeName = faker { fakerConfig { random = Random((clean + "12345").hashCode().toLong()) } }.company.name().uppercase()
            fakeCompanyName[clean] = fakeName
        }
        return fakeCompanyName[clean]!!
    }

    private fun String.cleanCompanyName(): String {
        return this
            .removeAll(Fake.commonCleaningRegexes)
            .removeAll(Fake.companyCleansingRegexes)
            .cleanWhitespace()
    }

    private val commonCleaningRegexes = mutableListOf<Regex>()
    private val companyCleansingRegexes = mutableListOf<Regex>()
    private val fakeCompanyName = mutableMapOf<String, String>()
    private val COMPANY_NAME_EXCLUDES = listOf(
        "CORP",
        "GMBH",
        "INC",
        "INC",
        "INC",
        "INCORPORATED",
        "LIMITED",
        "LLC",
        "LLLP",
        "LLP",
        "LP",
        "LTD",
        "OOO",
        "PLC",
        "PLLC",
        "SA",
        "SARL",
        "SRL",
        "TRUST"
    )
    private val COMPANY_NAME_EXCLUDES_DOTTED = COMPANY_NAME_EXCLUDES.map { it.toCharArray().joinToString("\\.") + "\\." }
    private val COMPANY_NAME_EXCLUDES_DOTTED_LESS_LAST = COMPANY_NAME_EXCLUDES.map { it.toCharArray().joinToString("\\.") }

    init {
        commonCleaningRegexes.add("(^|\\s*)(\\S*\\d+\\S*)(\\s*|$)".toRegex(RegexOption.IGNORE_CASE))
        COMPANY_NAME_EXCLUDES.forEach {
            val regex = "(^|\\s+)$it(\\.|\\s+|$)".toRegex(RegexOption.IGNORE_CASE)
            companyCleansingRegexes.add(regex)
        }
        COMPANY_NAME_EXCLUDES_DOTTED.forEach {
            val regex = "(^|\\s+)$it(\\s+|$)".toRegex(RegexOption.IGNORE_CASE)
            companyCleansingRegexes.add(regex)
        }
        COMPANY_NAME_EXCLUDES_DOTTED_LESS_LAST.forEach {
            val regex = "(^|\\s+)$it(\\s+|$)".toRegex(RegexOption.IGNORE_CASE)
            companyCleansingRegexes.add(regex)
        }
    }
}

fun String.removeAll(regexes: List<Regex>): String {
    var str = this
    regexes.forEach { regex ->
        str = str.replace(regex, " ")
    }
    return str
}

