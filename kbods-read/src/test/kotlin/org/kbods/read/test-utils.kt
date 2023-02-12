package org.kbods.read

import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue

fun testStatements(statements: List<BodsStatement>) {
    assertEquals(statements.size, 1000)
    val map = statements.associateBy { it.id }

    assertEquals(map["openownership-register-9473160899263237344"]!!.statementType, BodsStatementType.ENTITY)
    assertEquals(map["openownership-register-4351875629490786609"]!!.statementType, BodsStatementType.PERSON)
    assertEquals(map["openownership-register-10949623906398784527"]!!.statementType, BodsStatementType.OWNERSHIP_CTRL)
    assertTrue(map["openownership-register-10949623906398784527"]!!.isOwnershipCtrl)

    assertEquals(map["openownership-register-10949623906398784527"]!!.subjectId, "openownership-register-12991366811691475077")
    assertEquals(map["openownership-register-2452143574049712728"]!!.statementDate, "2017-08-21")
    assertEquals(map["openownership-register-13344716475440118974"]!!.sourceType, "officialRegister")
    assertEquals(map["openownership-register-17669746130836775356"]!!.jurisdictionCode, "GB")
    assertEquals(map["openownership-register-17411477780346571462"]!!.personType, "knownPerson")
    assertEquals(map["openownership-register-17507671354761443649"]!!.interests.first().interestStartDate(), "2016-07-15")
    assertEquals(map["openownership-register-17507671354761443649"]!!.interests.first().interestEndDate(), "2019-11-19")
    assertEquals(map["openownership-register-631006642991756856"]!!.interestedPartyId, "openownership-register-17411477780346571462")
    assertEquals(map["openownership-register-4878504546140740426"]!!.name, "CASTLE BASTION LIMITED")
    assertEquals(map["openownership-register-13077997364453373905"]!!.name, "Brian James Wallace")
    assertEquals(map["openownership-register-12569216474294322485"]!!.identifier("GB-COH"), "09083149")
}
