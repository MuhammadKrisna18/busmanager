package com.example.busmanager.domain.simulation.usecase

import com.example.busmanager.domain.simulation.model.SimulationSpeed
import com.example.busmanager.domain.simulation.repository.SimulationTimeRepository
import kotlinx.coroutines.flow.StateFlow

class SetSimulationSpeedUseCase(
    private val simulationTimeRepository: SimulationTimeRepository
) {
    operator fun invoke(speed: SimulationSpeed) {
        simulationTimeRepository.setSpeed(speed)
    }

    fun getSpeed(): StateFlow<SimulationSpeed> {
        return simulationTimeRepository.currentSpeed
    }
}
