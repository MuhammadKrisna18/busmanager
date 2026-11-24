package com.example.busmanager.domain.terminal.repository

import com.example.busmanager.domain.terminal.model.TerminalPassengerRecord
import kotlinx.coroutines.flow.Flow

interface TerminalPassengerRepository {
    fun getAllRecordsFlow(): Flow<List<TerminalPassengerRecord>>
    fun getRecordsByTerminalAndDateFlow(terminalId: String, day: Int, month: Int, year: Int): Flow<List<TerminalPassengerRecord>>
    fun getRecordsByTerminalFlow(terminalId: String): Flow<List<TerminalPassengerRecord>>
    suspend fun recordPassengerExchange(record: TerminalPassengerRecord)
    suspend fun getAvailableDates(): List<Triple<Int, Int, Int>>
    suspend fun clearAllRecords() {}
}
