package com.example.busmanager.domain.simulation.model

data class SavedSimulationState(
    val lastWallClockMillis: Long,
    val isPlaying: Boolean,
    val speed: SimulationSpeed,
    val gameCalendar: GameCalendar
)
