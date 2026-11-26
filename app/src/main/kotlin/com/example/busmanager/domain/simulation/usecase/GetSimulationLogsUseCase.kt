package com.example.busmanager.domain.simulation.usecase

import com.example.busmanager.domain.simulation.model.SimulationLog
import com.example.busmanager.domain.simulation.repository.SimulationLogRepository
import kotlinx.coroutines.flow.Flow

class GetSimulationLogsUseCase(
    private val repository: SimulationLogRepository
) {
    operator fun invoke(limit: Int = 30): Flow<List<SimulationLog>> {
        return repository.getRecentLogsFlow(limit)
    }

    suspend fun getSnapshot(limit: Int = 30): List<SimulationLog> {
        return repository.getRecentLogs(limit)
    }
}
