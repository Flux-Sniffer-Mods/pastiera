package it.palsoftware.pastiera.otp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OneTimeCodesTest {
    @Test
    fun findsCommonCodeMessages() {
        assertEquals("482913", OneTimeCodes.extract("Your verification code is 482913. It expires in 10 minutes."))
        assertEquals("123456", OneTimeCodes.extract("G-123456 is your Google verification code."))
        assertEquals("735104", OneTimeCodes.extract("Your login code: 735 104"))
        assertEquals("9921", OneTimeCodes.extract("Il tuo codice è 9921"))
        assertEquals("583012", OneTimeCodes.extract("Code 583012 – use it to sign in. © 2026 Example"))
    }

    @Test
    fun ignoresMessagesWithoutACode() {
        assertNull(OneTimeCodes.extract("Your order 55812 has shipped"))
        assertNull(OneTimeCodes.extract("Lunch at 12:30? Code review after"))
        assertNull(OneTimeCodes.extract("Your code review has 3 comments"))
    }

    @Test
    fun skipsAmountsAndPrefersSixDigits() {
        assertEquals("402817", OneTimeCodes.extract("Payment of $1500 needs code 402817"))
    }
}
