package com.example.busmanager.domain.simulation.repository

import com.example.busmanager.domain.simulation.model.SavedSimulationState

interface SimulationStateRepository {
    fun saveState(state: SavedSimulationState)
    fun loadState(): SavedSimulationState?
    fun clearState()
}
