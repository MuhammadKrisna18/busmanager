package com.example.busmanager.data.repository

import com.example.busmanager.data.local.dao.SimulationLogDao
import com.example.busmanager.data.mapper.toDomain
import com.example.busmanager.data.mapper.toEntity
import com.example.busmanager.domain.simulation.model.SimulationLog
import com.example.busmanager.domain.simulation.repository.SimulationLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SimulationLogRepositoryImpl(
    private val dao: SimulationLogDao
) : SimulationLogRepository {

    override fun getRecentLogsFlow(limit: Int): Flow<List<SimulationLog>> {
        return dao.getRecentLogsFlow(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRecentLogs(limit: Int): List<SimulationLog> {
        return dao.getRecentLogs(limit).map { it.toDomain() }
    }

    override suspend fun addLog(log: SimulationLog) {
        dao.insertLog(log.toEntity())
    }

    override suspend fun clearLogs() {
        dao.deleteAllLogs()
    }
}
