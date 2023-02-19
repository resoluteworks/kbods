package org.kbods.rdf

import com.beust.klaxon.Klaxon
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.kbods.rdf.interest.sharesStatements
import org.rdf4k.literalDecimal
import org.rdf4k.statement
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import java.io.StringReader
import java.util.*
import kotlin.test.assertContentEquals

class InterestShareTest {

    @Test
    fun empty() {
        val interestJsonStr =
            """
            {
                "type": "shareholding",
                "startDate": "2013-10-11",
                "endDate": "2020-10-08"
            }
            """
        val (_, statements) = statements(interestJsonStr)
        assertTrue(statements.isEmpty())
    }

    @Test
    fun `exact shares`() {
        expectExactShares(
            """
            {
                "type": "shareholding",
                "share": {
                  "exact": 29.609999999999996,
                  "minimum": 29.609999999999996,
                  "maximum": 29.609999999999996
                },
                "startDate": "2013-10-11",
                "endDate": "2020-10-08"
            }
            """, 29.609999999999996
        )
        expectExactShares(
            """
            {
                "type": "shareholding",
                "share": {
                  "exact": 25
                },
                "startDate": "2013-10-11",
                "endDate": "2020-10-08"
            }
            """, 25.0
        )
    }

    @Test
    fun `minimum and maximum`() {
        expectMinMaxShares(
            """
          {
              "type": "shareholding",
              "details": "ownership-of-shares-75-to-100-percent",
              "share": {
                "minimum": 75,
                "maximum": 100,
                "exclusiveMinimum": false,
                "exclusiveMaximum": false
              },
              "startDate": "2016-04-06"
          }
      """, 75.0, 100.0
        )
        expectMinMaxShares(
            """
          {
              "type": "shareholding",
              "details": "ownership-of-shares-75-to-100-percent",
              "share": {
                "minimum": 19,
                "exclusiveMinimum": false,
                "exclusiveMaximum": false
              },
              "startDate": "2016-04-06"
          }
      """, 19.0, null
        )
        expectMinMaxShares(
            """
          {
              "type": "shareholding",
              "details": "ownership-of-shares-75-to-100-percent",
              "share": {
                "maximum": 23,
                "exclusiveMinimum": false,
                "exclusiveMaximum": false
              },
              "startDate": "2016-04-06"
          }
      """, null, 23.0
        )
    }

    private fun expectExactShares(interestJsonStr: String, expectExact: Double) {
        val (interestObjectIri, statements) = statements(interestJsonStr)
        assertContentEquals(
            statements,
            listOf(
                statement(interestObjectIri, BodsRdf.PROP_INTEREST_SHARES_EXACT, expectExact.literalDecimal(), null),
                statement(interestObjectIri, BodsRdf.PROP_INTEREST_SHARES_MIN, expectExact.literalDecimal(), null),
                statement(interestObjectIri, BodsRdf.PROP_INTEREST_SHARES_MAX, expectExact.literalDecimal(), null)
            )
        )
    }

    private fun expectMinMaxShares(interestJsonStr: String, expectMin: Double?, expectMax: Double?) {
        val (interestObjectIri, statements) = statements(interestJsonStr)
        if (expectMin != null && expectMax != null) {
            assertContentEquals(
                statements,
                listOf(
                    statement(interestObjectIri, BodsRdf.PROP_INTEREST_SHARES_MIN, expectMin.literalDecimal(), null),
                    statement(interestObjectIri, BodsRdf.PROP_INTEREST_SHARES_MAX, expectMax.literalDecimal(), null)
                )
            )
        } else if (expectMin != null) {
            assertContentEquals(
                statements,
                listOf(statement(interestObjectIri, BodsRdf.PROP_INTEREST_SHARES_MIN, expectMin.literalDecimal(), null))
            )
        } else if (expectMax != null) {
            assertContentEquals(
                statements,
                listOf(statement(interestObjectIri, BodsRdf.PROP_INTEREST_SHARES_MAX, expectMax.literalDecimal(), null))
            )
        }
    }

    private fun statements(interestJsonStr: String): Pair<IRI, List<Statement>> {
        val jsonParser = Klaxon()
        val interestJson = jsonParser.parseJsonObject(StringReader(interestJsonStr))
        val interestObjectIri = SimpleValueFactory.getInstance().createIRI("http://test.com/", "${UUID.randomUUID()}")
        val statements = sharesStatements(interestObjectIri, interestJson, null)
        return Pair(interestObjectIri, statements)
    }
}
