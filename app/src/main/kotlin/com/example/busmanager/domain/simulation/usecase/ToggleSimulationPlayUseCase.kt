package com.example.busmanager.domain.simulation.usecase

import com.example.busmanager.domain.simulation.repository.SimulationTimeRepository
import kotlinx.coroutines.flow.StateFlow

class ToggleSimulationPlayUseCase(
    private val simulationTimeRepository: SimulationTimeRepository
) {
    operator fun invoke() {
        simulationTimeRepository.togglePlay()
    }

    fun isPlaying(): StateFlow<Boolean> {
        return simulationTimeRepository.isPlaying
    }
}
