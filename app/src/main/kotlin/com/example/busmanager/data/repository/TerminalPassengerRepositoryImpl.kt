package com.example.busmanager.data.repository

import com.example.busmanager.data.local.dao.TerminalPassengerRecordDao
import com.example.busmanager.data.mapper.toDomain
import com.example.busmanager.data.mapper.toEntity
import com.example.busmanager.domain.terminal.model.TerminalPassengerRecord
import com.example.busmanager.domain.terminal.repository.TerminalPassengerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TerminalPassengerRepositoryImpl(
    private val dao: TerminalPassengerRecordDao
) : TerminalPassengerRepository {

    override fun getAllRecordsFlow(): Flow<List<TerminalPassengerRecord>> {
        return dao.getAllRecords().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getRecordsByTerminalAndDateFlow(
        terminalId: String,
        day: Int,
        month: Int,
        year: Int
    ): Flow<List<TerminalPassengerRecord>> {
        return dao.getRecordsByTerminalAndDate(terminalId, day, month, year).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecordsByTerminalFlow(terminalId: String): Flow<List<TerminalPassengerRecord>> {
        return dao.getRecordsByTerminal(terminalId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun recordPassengerExchange(record: TerminalPassengerRecord) {
        dao.insertRecord(record.toEntity())
    }

    override suspend fun getAvailableDates(): List<Triple<Int, Int, Int>> {
        val all = dao.getAllRecordsSnapshot()
        return all.map { Triple(it.day, it.month, it.year) }.distinct().sortedWith(
            compareBy<Triple<Int, Int, Int>> { it.third }.thenBy { it.second }.thenBy { it.first }
        )
    }

    override suspend fun clearAllRecords() {
        dao.deleteAllRecords()
    }
}
