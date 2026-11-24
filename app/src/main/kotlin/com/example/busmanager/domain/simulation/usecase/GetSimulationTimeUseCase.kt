package com.example.busmanager.domain.simulation.usecase

import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.repository.SimulationTimeRepository
import kotlinx.coroutines.flow.StateFlow

class GetSimulationTimeUseCase(
    private val simulationTimeRepository: SimulationTimeRepository
) {
    operator fun invoke(): StateFlow<GameCalendar> {
        return simulationTimeRepository.currentTime
    }
}
