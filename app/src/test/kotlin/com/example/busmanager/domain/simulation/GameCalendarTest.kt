package com.example.busmanager.domain.simulation

import com.example.busmanager.domain.simulation.model.GameCalendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

class GameCalendarTest {

    @Test
    fun initial_returnsExpectedDefaultDateTime() {
        val calendar = GameCalendar.initial()

        assertEquals(2026, calendar.year)
        assertEquals(11, calendar.month)
        assertEquals(24, calendar.day)
        assertEquals(8, calendar.hour)
        assertEquals(0, calendar.minute)
        assertEquals("Selasa, 24 November 2026 • 08:00 WIB", calendar.formatDateTime())
    }

    @Test
    fun advanceMinutes_withinSameHour_updatesMinuteCorrectly() {
        val calendar = GameCalendar(2026, 11, 24, 8, 15)
        val advanced = calendar.advanceMinutes(30)

        assertEquals(8, advanced.hour)
        assertEquals(45, advanced.minute)
        assertEquals(24, advanced.day)
    }

    @Test
    fun advanceMinutes_crossingHourBoundary_updatesHourAndMinute() {
        val calendar = GameCalendar(2026, 11, 24, 8, 45)
        val advanced = calendar.advanceMinutes(30)

        assertEquals(9, advanced.hour)
        assertEquals(15, advanced.minute)
        assertEquals(24, advanced.day)
    }

    @Test
    fun advanceMinutes_crossingDayBoundary_updatesDayAndResetsHour() {
        val calendar = GameCalendar(2026, 11, 24, 23, 30)
        val advanced = calendar.advanceMinutes(45)

        assertEquals(0, advanced.hour)
        assertEquals(15, advanced.minute)
        assertEquals(25, advanced.day)
        assertEquals(11, advanced.month)
        assertEquals(2026, advanced.year)
    }

    @Test
    fun advanceMinutes_crossingMonthBoundary_updatesMonthAndDay() {
        val calendar = GameCalendar(2026, 11, 30, 23, 0)
        val advanced = calendar.advanceMinutes(120)

        assertEquals(1, advanced.hour)
        assertEquals(0, advanced.minute)
        assertEquals(1, advanced.day)
        assertEquals(12, advanced.month)
        assertEquals(2026, advanced.year)
    }

    @Test
    fun advanceMinutes_crossingYearBoundary_updatesYear() {
        val calendar = GameCalendar(2026, 12, 31, 23, 30)
        val advanced = calendar.advanceMinutes(60)

        assertEquals(0, advanced.hour)
        assertEquals(30, advanced.minute)
        assertEquals(1, advanced.day)
        assertEquals(1, advanced.month)
        assertEquals(2027, advanced.year)
    }

    @Test
    fun advanceMinutes_zeroMinutes_returnsSameInstance() {
        val calendar = GameCalendar(2026, 11, 24, 8, 0)
        val advanced = calendar.advanceMinutes(0)

        assertEquals(calendar, advanced)
    }

    @Test
    fun leapYear_februaryHas29Days() {
        val leapYearCal = GameCalendar(2028, 2, 28, 23, 0)
        val advanced = leapYearCal.advanceMinutes(120)

        assertEquals(29, advanced.day)
        assertEquals(2, advanced.month)
        assertEquals(2028, advanced.year)

        val nextDay = advanced.advanceMinutes(24 * 60)
        assertEquals(1, nextDay.day)
        assertEquals(3, nextDay.month)
    }

    @Test
    fun dayOfWeek_returnsCorrectIndonesianNames() {
        val monday = GameCalendar(2026, 11, 23, 10, 0)
        assertEquals(DayOfWeek.MONDAY, monday.dayOfWeek())
        assertEquals("Senin", monday.dayOfWeekName())

        val tuesday = GameCalendar(2026, 11, 24, 10, 0)
        assertEquals("Selasa", tuesday.dayOfWeekName())

        val wednesday = GameCalendar(2026, 11, 25, 10, 0)
        assertEquals("Rabu", wednesday.dayOfWeekName())

        val thursday = GameCalendar(2026, 11, 26, 10, 0)
        assertEquals("Kamis", thursday.dayOfWeekName())

        val friday = GameCalendar(2026, 11, 27, 10, 0)
        assertEquals("Jumat", friday.dayOfWeekName())

        val saturday = GameCalendar(2026, 11, 28, 10, 0)
        assertEquals("Sabtu", saturday.dayOfWeekName())

        val sunday = GameCalendar(2026, 11, 29, 10, 0)
        assertEquals("Minggu", sunday.dayOfWeekName())
    }

    @Test
    fun monthName_returnsCorrectIndonesianNames() {
        assertEquals("Januari", GameCalendar(2026, 1, 1, 0, 0).monthName())
        assertEquals("Februari", GameCalendar(2026, 2, 1, 0, 0).monthName())
        assertEquals("Maret", GameCalendar(2026, 3, 1, 0, 0).monthName())
        assertEquals("April", GameCalendar(2026, 4, 1, 0, 0).monthName())
        assertEquals("Mei", GameCalendar(2026, 5, 1, 0, 0).monthName())
        assertEquals("Juni", GameCalendar(2026, 6, 1, 0, 0).monthName())
        assertEquals("Juli", GameCalendar(2026, 7, 1, 0, 0).monthName())
        assertEquals("Agustus", GameCalendar(2026, 8, 1, 0, 0).monthName())
        assertEquals("September", GameCalendar(2026, 9, 1, 0, 0).monthName())
        assertEquals("Oktober", GameCalendar(2026, 10, 1, 0, 0).monthName())
        assertEquals("November", GameCalendar(2026, 11, 1, 0, 0).monthName())
        assertEquals("Desember", GameCalendar(2026, 12, 1, 0, 0).monthName())
    }

    @Test
    fun formatting_returnsIndonesianFormat() {
        val calendar = GameCalendar(2026, 11, 24, 14, 5)

        assertEquals("24 November 2026", calendar.formatDate())
        assertEquals("14:05 WIB", calendar.formatTime())
        assertEquals("Selasa, 24 November 2026 • 14:05 WIB", calendar.formatDateTime())
    }

    @Test
    fun compareTo_correctlyOrdersCalendars() {
        val t1 = GameCalendar(2026, 11, 24, 8, 0)
        val t2 = GameCalendar(2026, 11, 24, 8, 30)
        val t3 = GameCalendar(2026, 11, 25, 6, 0)
        val t4 = GameCalendar(2026, 12, 1, 0, 0)
        val t5 = GameCalendar(2027, 1, 1, 0, 0)

        assertTrue(t1 < t2)
        assertTrue(t2 < t3)
        assertTrue(t3 < t4)
        assertTrue(t4 < t5)
        assertEquals(0, t1.compareTo(GameCalendar(2026, 11, 24, 8, 0)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidYear_throwsException() {
        GameCalendar(2020, 1, 1, 0, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidMonth_throwsException() {
        GameCalendar(2026, 13, 1, 0, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidDay_throwsException() {
        GameCalendar(2026, 2, 30, 0, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidHour_throwsException() {
        GameCalendar(2026, 1, 1, 24, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidMinute_throwsException() {
        GameCalendar(2026, 1, 1, 0, 60)
    }
}
