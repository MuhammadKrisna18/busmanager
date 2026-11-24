package com.example.busmanager.domain.simulation.repository

import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.SimulationSpeed
import kotlinx.coroutines.flow.StateFlow

interface SimulationTimeRepository {
    val currentTime: StateFlow<GameCalendar>
    val isPlaying: StateFlow<Boolean>
    val currentSpeed: StateFlow<SimulationSpeed>

    fun play()
    fun pause()
    fun togglePlay()
    fun setSpeed(speed: SimulationSpeed)
    fun advanceMinutes(minutes: Int)
    fun setTime(calendar: GameCalendar)
}
