package com.example.busmanager.data.repository

import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.repository.SimulationTimeRepository
import com.example.busmanager.domain.simulation.repository.SimulationTripRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SimulationTimeRepositoryImpl(
    initialTime: GameCalendar = GameCalendar.initial(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val tickIntervalMillis: Long = 1000L,
    private val minutesPerTick: Int = 10,
    private val simulationTripRepository: SimulationTripRepository? = null
) : SimulationTimeRepository {

    private val _currentTime = MutableStateFlow(initialTime)
    override val currentTime: StateFlow<GameCalendar> = _currentTime.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var tickerJob: Job? = null

    override fun play() {
        if (_isPlaying.value) return
        _isPlaying.value = true
        startTicker()
    }

    override fun pause() {
        if (!_isPlaying.value) return
        _isPlaying.value = false
        stopTicker()
    }

    override fun togglePlay() {
        if (_isPlaying.value) pause() else play()
    }

    override fun advanceMinutes(minutes: Int) {
        _currentTime.update { it.advanceMinutes(minutes) }
        simulationTripRepository?.advanceTripsTime(minutes)
    }

    override fun setTime(calendar: GameCalendar) {
        _currentTime.value = calendar
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive && _isPlaying.value) {
                delay(tickIntervalMillis)
                advanceMinutes(minutesPerTick)
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }
}
