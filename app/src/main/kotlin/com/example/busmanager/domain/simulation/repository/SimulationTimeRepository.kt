package com.example.busmanager.domain.simulation.repository

import com.example.busmanager.domain.simulation.model.GameCalendar
import kotlinx.coroutines.flow.StateFlow

interface SimulationTimeRepository {
    val currentTime: StateFlow<GameCalendar>
    val isPlaying: StateFlow<Boolean>

    fun play()
    fun pause()
    fun togglePlay()
    fun advanceMinutes(minutes: Int)
    fun setTime(calendar: GameCalendar)
}
