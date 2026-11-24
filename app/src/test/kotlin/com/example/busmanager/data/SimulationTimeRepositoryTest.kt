package com.example.busmanager.data

import com.example.busmanager.data.repository.SimulationTimeRepositoryImpl
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.SimulationSpeed
import com.example.busmanager.domain.simulation.usecase.GetSimulationTimeUseCase
import com.example.busmanager.domain.simulation.usecase.SetSimulationSpeedUseCase
import com.example.busmanager.domain.simulation.usecase.ToggleSimulationPlayUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimulationTimeRepositoryTest {

    @Test
    fun initialState_defaultsToGivenTimeAndPaused() {
        val initial = GameCalendar(2026, 11, 24, 8, 0)
        val repository = SimulationTimeRepositoryImpl(initialTime = initial)

        assertEquals(initial, repository.currentTime.value)
        assertFalse(repository.isPlaying.value)
        assertEquals(SimulationSpeed.NORMAL, repository.currentSpeed.value)
    }

    @Test
    fun advanceMinutes_manuallyAdvancesTime() {
        val initial = GameCalendar(2026, 11, 24, 8, 0)
        val repository = SimulationTimeRepositoryImpl(initialTime = initial)

        repository.advanceMinutes(45)

        assertEquals(8, repository.currentTime.value.hour)
        assertEquals(45, repository.currentTime.value.minute)
    }

    @Test
    fun setTime_overwritesCurrentTime() {
        val initial = GameCalendar(2026, 11, 24, 8, 0)
        val target = GameCalendar(2027, 1, 1, 12, 0)
        val repository = SimulationTimeRepositoryImpl(initialTime = initial)

        repository.setTime(target)

        assertEquals(target, repository.currentTime.value)
    }

    @Test
    fun playAndPause_togglesIsPlaying() {
        val repository = SimulationTimeRepositoryImpl()

        assertFalse(repository.isPlaying.value)

        repository.play()
        assertTrue(repository.isPlaying.value)

        repository.pause()
        assertFalse(repository.isPlaying.value)

        repository.togglePlay()
        assertTrue(repository.isPlaying.value)

        repository.togglePlay()
        assertFalse(repository.isPlaying.value)
    }

    @Test
    fun setSpeed_updatesCurrentSpeed() {
        val repository = SimulationTimeRepositoryImpl()

        assertEquals(SimulationSpeed.NORMAL, repository.currentSpeed.value)

        repository.setSpeed(SimulationSpeed.FAST)
        assertEquals(SimulationSpeed.FAST, repository.currentSpeed.value)

        repository.setSpeed(SimulationSpeed.VERY_FAST)
        assertEquals(SimulationSpeed.VERY_FAST, repository.currentSpeed.value)

        repository.setSpeed(SimulationSpeed.NORMAL)
        assertEquals(SimulationSpeed.NORMAL, repository.currentSpeed.value)
    }

    @Test
    fun ticker_whenPlaying_advancesTimeAtIntervals() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)
        val initial = GameCalendar(2026, 11, 24, 8, 0)

        val repository = SimulationTimeRepositoryImpl(
            initialTime = initial,
            scope = testScope,
            tickIntervalMillis = 1000L,
            minutesPerTick = 10
        )

        repository.play()
        testScope.advanceTimeBy(1001L)

        assertEquals(10, repository.currentTime.value.minute)

        testScope.advanceTimeBy(2000L)
        assertEquals(30, repository.currentTime.value.minute)

        repository.pause()
        testScope.advanceTimeBy(2000L)
        assertEquals(30, repository.currentTime.value.minute)
    }

    @Test
    fun ticker_speedFast_advancesTimeFaster() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)
        val initial = GameCalendar(2026, 11, 24, 8, 0)

        val repository = SimulationTimeRepositoryImpl(
            initialTime = initial,
            scope = testScope,
            tickIntervalMillis = 1000L,
            minutesPerTick = 10
        )

        repository.setSpeed(SimulationSpeed.FAST)
        repository.play()

        testScope.advanceTimeBy(350L)
        assertEquals(10, repository.currentTime.value.minute)

        testScope.advanceTimeBy(350L)
        assertEquals(20, repository.currentTime.value.minute)

        testScope.advanceTimeBy(350L)
        assertEquals(30, repository.currentTime.value.minute)

        repository.pause()
    }

    @Test
    fun useCases_wireCorrectly() {
        val initial = GameCalendar(2026, 11, 24, 8, 0)
        val repository = SimulationTimeRepositoryImpl(initialTime = initial)
        val getTimeUseCase = GetSimulationTimeUseCase(repository)
        val togglePlayUseCase = ToggleSimulationPlayUseCase(repository)
        val setSpeedUseCase = SetSimulationSpeedUseCase(repository)

        assertEquals(initial, getTimeUseCase().value)
        assertFalse(togglePlayUseCase.isPlaying().value)
        assertEquals(SimulationSpeed.NORMAL, setSpeedUseCase.getSpeed().value)

        togglePlayUseCase()
        assertTrue(togglePlayUseCase.isPlaying().value)

        togglePlayUseCase()
        assertFalse(togglePlayUseCase.isPlaying().value)

        setSpeedUseCase(SimulationSpeed.VERY_FAST)
        assertEquals(SimulationSpeed.VERY_FAST, setSpeedUseCase.getSpeed().value)
    }
}
