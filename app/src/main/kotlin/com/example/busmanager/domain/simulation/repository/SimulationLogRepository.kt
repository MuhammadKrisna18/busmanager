package com.example.busmanager.domain.simulation.repository

import com.example.busmanager.domain.simulation.model.SimulationLog
import kotlinx.coroutines.flow.Flow

interface SimulationLogRepository {
    fun getRecentLogsFlow(limit: Int = 30): Flow<List<SimulationLog>>
    suspend fun getRecentLogs(limit: Int = 30): List<SimulationLog>
    suspend fun addLog(log: SimulationLog)
    suspend fun clearLogs()
}
