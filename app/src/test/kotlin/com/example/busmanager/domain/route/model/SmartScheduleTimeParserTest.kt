package com.example.busmanager.domain.route.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartScheduleTimeParserTest {

    @Test
    fun parseSingleDigitMorningHour() {
        val parsed = SmartScheduleTimeParser.parse("7")
        assertNotNull(parsed)
        assertEquals(7, parsed?.hour)
        assertEquals(0, parsed?.minute)
        assertEquals("07:00 WIB", parsed?.formattedTime)
        assertTrue(parsed?.descriptionText?.contains("Pagi") == true)
    }

    @Test
    fun parseTwoDigitEveningHour() {
        val parsed = SmartScheduleTimeParser.parse("19")
        assertNotNull(parsed)
        assertEquals(19, parsed?.hour)
        assertEquals(0, parsed?.minute)
        assertEquals("19:00 WIB", parsed?.formattedTime)
        assertTrue(parsed?.descriptionText?.contains("Malam") == true)
        assertTrue(parsed?.descriptionText?.contains("7.00") == true)
    }

    @Test
    fun parseEarlyAfternoonHours() {
        val parsed1 = SmartScheduleTimeParser.parse("1")
        assertEquals(13, parsed1?.hour)
        assertEquals(0, parsed1?.minute)
        assertTrue(parsed1?.descriptionText?.contains("Siang") == true)

        val parsed2 = SmartScheduleTimeParser.parse("2")
        assertEquals(14, parsed2?.hour)
        assertEquals(0, parsed2?.minute)
        assertTrue(parsed2?.descriptionText?.contains("Siang") == true)

        val parsed3 = SmartScheduleTimeParser.parse("3")
        assertEquals(15, parsed3?.hour)
        assertEquals(0, parsed3?.minute)
        assertTrue(parsed3?.descriptionText?.contains("Sore") == true)
    }

    @Test
    fun parseWithDotSeparator() {
        val parsed = SmartScheduleTimeParser.parse("7.30")
        assertNotNull(parsed)
        assertEquals(7, parsed?.hour)
        assertEquals(30, parsed?.minute)
        assertEquals("07:30 WIB", parsed?.formattedTime)
    }

    @Test
    fun parseWithColonSeparator() {
        val parsed = SmartScheduleTimeParser.parse("19:45")
        assertNotNull(parsed)
        assertEquals(19, parsed?.hour)
        assertEquals(45, parsed?.minute)
        assertEquals("19:45 WIB", parsed?.formattedTime)
    }

    @Test
    fun parseThreeDigitsCompact() {
        val parsed = SmartScheduleTimeParser.parse("730")
        assertNotNull(parsed)
        assertEquals(7, parsed?.hour)
        assertEquals(30, parsed?.minute)
        assertEquals("07:30 WIB", parsed?.formattedTime)
    }

    @Test
    fun parseFourDigitsCompact() {
        val parsed = SmartScheduleTimeParser.parse("1930")
        assertNotNull(parsed)
        assertEquals(19, parsed?.hour)
        assertEquals(30, parsed?.minute)
        assertEquals("19:30 WIB", parsed?.formattedTime)
    }

    @Test
    fun parseOperatingHoursBounds() {
        val parsedOpening = SmartScheduleTimeParser.parse("4")
        assertEquals(4, parsedOpening?.hour)
        assertEquals(0, parsedOpening?.minute)

        val parsedClosing = SmartScheduleTimeParser.parse("23.59")
        assertEquals(23, parsedClosing?.hour)
        assertEquals(59, parsedClosing?.minute)
        assertEquals("23:59 WIB", parsedClosing?.formattedTime)
    }

    @Test
    fun parseInvalidInputs() {
        assertNull(SmartScheduleTimeParser.parse(""))
        assertNull(SmartScheduleTimeParser.parse("   "))
        assertNull(SmartScheduleTimeParser.parse("abc"))
        assertNull(SmartScheduleTimeParser.parse("25:00:00:00"))
    }
}
