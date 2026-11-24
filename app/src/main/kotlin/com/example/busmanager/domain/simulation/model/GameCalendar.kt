package com.example.busmanager.domain.simulation.model

import java.time.DayOfWeek
import java.time.LocalDate

data class GameCalendar(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int
) : Comparable<GameCalendar> {

    init {
        require(year >= 2026) { "Year must be 2026 or later" }
        require(month in 1..12) { "Month must be between 1 and 12" }
        require(day in 1..daysInMonth(year, month)) { "Day must be valid for year $year and month $month" }
        require(hour in 0..23) { "Hour must be between 0 and 23" }
        require(minute in 0..59) { "Minute must be between 0 and 59" }
    }

    fun advanceMinutes(minutes: Int): GameCalendar {
        require(minutes >= 0) { "Minutes to advance cannot be negative" }
        if (minutes == 0) return this

        val totalMinutes = this.minute + minutes
        val additionalHours = totalMinutes / 60
        val newMinute = totalMinutes % 60

        val totalHours = this.hour + additionalHours
        val additionalDays = totalHours / 24
        val newHour = totalHours % 24

        var currentDay = this.day + additionalDays
        var currentMonth = this.month
        var currentYear = this.year

        while (true) {
            val maxDaysInCurrentMonth = daysInMonth(currentYear, currentMonth)
            if (currentDay <= maxDaysInCurrentMonth) {
                break
            }
            currentDay -= maxDaysInCurrentMonth
            currentMonth += 1
            if (currentMonth > 12) {
                currentMonth = 1
                currentYear += 1
            }
        }

        return GameCalendar(
            year = currentYear,
            month = currentMonth,
            day = currentDay,
            hour = newHour,
            minute = newMinute
        )
    }

    fun dayOfWeek(): DayOfWeek {
        return LocalDate.of(year, month, day).dayOfWeek
    }

    fun dayOfWeekName(): String {
        return when (dayOfWeek()) {
            DayOfWeek.MONDAY -> "Senin"
            DayOfWeek.TUESDAY -> "Selasa"
            DayOfWeek.WEDNESDAY -> "Rabu"
            DayOfWeek.THURSDAY -> "Kamis"
            DayOfWeek.FRIDAY -> "Jumat"
            DayOfWeek.SATURDAY -> "Sabtu"
            DayOfWeek.SUNDAY -> "Minggu"
        }
    }

    fun monthName(): String {
        return when (month) {
            1 -> "Januari"
            2 -> "Februari"
            3 -> "Maret"
            4 -> "April"
            5 -> "Mei"
            6 -> "Juni"
            7 -> "Juli"
            8 -> "Agustus"
            9 -> "September"
            10 -> "Oktober"
            11 -> "November"
            12 -> "Desember"
            else -> ""
        }
    }

    fun formatDateTime(): String {
        return "${dayOfWeekName()}, $day ${monthName()} $year • ${formatTime()}"
    }

    fun formatDate(): String {
        return "$day ${monthName()} $year"
    }

    fun formatTime(): String {
        return String.format("%02d:%02d WIB", hour, minute)
    }

    override fun compareTo(other: GameCalendar): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        if (day != other.day) return day.compareTo(other.day)
        if (hour != other.hour) return hour.compareTo(other.hour)
        return minute.compareTo(other.minute)
    }

    companion object {
        fun initial(): GameCalendar = GameCalendar(
            year = 2026,
            month = 11,
            day = 24,
            hour = 8,
            minute = 0
        )

        fun daysInMonth(year: Int, month: Int): Int {
            return when (month) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if (isLeapYear(year)) 29 else 28
                else -> 30
            }
        }

        private fun isLeapYear(year: Int): Boolean {
            return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
        }
    }
}
