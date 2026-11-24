package com.example.busmanager.data

import com.example.busmanager.data.local.entity.TerminalPassengerRecordEntity
import com.example.busmanager.data.mapper.toDomain
import com.example.busmanager.data.mapper.toEntity
import com.example.busmanager.domain.terminal.model.TerminalPassengerRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class TerminalPassengerRecordMapperTest {

    @Test
    fun entityToDomain_mapsAllFieldsCorrectly() {
        val entity = TerminalPassengerRecordEntity(
            id = "rec-1",
            terminalId = "term_purabaya",
            terminalName = "Terminal Purabaya",
            busId = "bus-1",
            busCode = "TN-01",
            routeId = "route-1",
            routeName = "Surabaya - Malang",
            day = 24,
            month = 11,
            year = 2026,
            dateFormatted = "24/11/2026",
            timeFormatted = "08:30",
            boardedCount = 30,
            alightedCount = 5,
            timestamp = 1700000000L
        )

        val domain = entity.toDomain()

        assertEquals("rec-1", domain.id)
        assertEquals("term_purabaya", domain.terminalId)
        assertEquals("Terminal Purabaya", domain.terminalName)
        assertEquals("bus-1", domain.busId)
        assertEquals("TN-01", domain.busCode)
        assertEquals("route-1", domain.routeId)
        assertEquals("Surabaya - Malang", domain.routeName)
        assertEquals(24, domain.day)
        assertEquals(11, domain.month)
        assertEquals(2026, domain.year)
        assertEquals("24/11/2026", domain.dateFormatted)
        assertEquals("08:30", domain.timeFormatted)
        assertEquals(30, domain.boardedCount)
        assertEquals(5, domain.alightedCount)
        assertEquals(1700000000L, domain.timestamp)
    }

    @Test
    fun domainToEntity_mapsAllFieldsCorrectly() {
        val domain = TerminalPassengerRecord(
            id = "rec-2",
            terminalId = "term_arjosari",
            terminalName = "Terminal Arjosari",
            busId = "bus-2",
            busCode = "TN-02",
            routeId = "route-2",
            routeName = "Malang - Surabaya",
            day = 25,
            month = 11,
            year = 2026,
            dateFormatted = "25/11/2026",
            timeFormatted = "14:15",
            boardedCount = 18,
            alightedCount = 22,
            timestamp = 1700050000L
        )

        val entity = domain.toEntity()

        assertEquals("rec-2", entity.id)
        assertEquals("term_arjosari", entity.terminalId)
        assertEquals("Terminal Arjosari", entity.terminalName)
        assertEquals("bus-2", entity.busId)
        assertEquals("TN-02", entity.busCode)
        assertEquals("route-2", entity.routeId)
        assertEquals("Malang - Surabaya", entity.routeName)
        assertEquals(25, entity.day)
        assertEquals(11, entity.month)
        assertEquals(2026, entity.year)
        assertEquals("25/11/2026", entity.dateFormatted)
        assertEquals("14:15", entity.timeFormatted)
        assertEquals(18, entity.boardedCount)
        assertEquals(22, entity.alightedCount)
        assertEquals(1700050000L, entity.timestamp)
    }
}
