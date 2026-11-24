package com.example.busmanager.domain.simulation.usecase

import com.example.busmanager.domain.simulation.model.SavedSimulationState
import com.example.busmanager.domain.simulation.repository.SimulationStateRepository
import com.example.busmanager.domain.simulation.repository.SimulationTimeRepository

class SaveSimulationStateUseCase(
    private val simulationStateRepository: SimulationStateRepository,
    private val simulationTimeRepository: SimulationTimeRepository
) {
    operator fun invoke() {
        val state = SavedSimulationState(
            lastWallClockMillis = System.currentTimeMillis(),
            isPlaying = simulationTimeRepository.isPlaying.value,
            speed = simulationTimeRepository.currentSpeed.value,
            gameCalendar = simulationTimeRepository.currentTime.value
        )
        simulationStateRepository.saveState(state)
    }
}
