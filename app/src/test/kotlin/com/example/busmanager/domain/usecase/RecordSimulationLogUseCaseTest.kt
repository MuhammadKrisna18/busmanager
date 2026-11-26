package com.example.busmanager.domain.usecase

import com.example.busmanager.domain.simulation.model.SimulationLog
import com.example.busmanager.domain.simulation.model.SimulationLogCategory
import com.example.busmanager.domain.simulation.repository.SimulationLogRepository
import com.example.busmanager.domain.simulation.usecase.GetSimulationLogsUseCase
import com.example.busmanager.domain.simulation.usecase.RecordSimulationLogUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecordSimulationLogUseCaseTest {

    private lateinit var fakeRepo: FakeSimulationLogRepository
    private lateinit var recordUseCase: RecordSimulationLogUseCase
    private lateinit var getUseCase: GetSimulationLogsUseCase

    @Before
    fun setUp() {
        fakeRepo = FakeSimulationLogRepository()
        recordUseCase = RecordSimulationLogUseCase(fakeRepo)
        getUseCase = GetSimulationLogsUseCase(fakeRepo)
    }

    @Test
    fun recordDispatchLogsProperCategoryAndMessage() = runBlocking {
        recordUseCase.recordDispatch(
            busCode = "TN-01",
            routeName = "Surabaya - Malang",
            destTerminalName = "Arjosari",
            passengerCount = 35,
            timeText = "08:00",
            dateText = "Senin, 24 Nov 2026",
            busId = "bus-1",
            routeId = "route-1"
        )

        val logs = getUseCase().first()
        assertEquals(1, logs.size)
        val log = logs.first()
        assertEquals(SimulationLogCategory.DISPATCH, log.category)
        assertEquals("Armada Berangkat: TN-01", log.title)
        assertTrue(log.message.contains("35 penumpang"))
        assertEquals("bus-1", log.busId)
    }

    @Test
    fun recordArrivalLogsProperCategoryAndFormattedEarnings() = runBlocking {
        recordUseCase.recordArrival(
            busCode = "TN-02",
            routeName = "Surabaya - Madiun",
            destTerminalName = "Purboyo",
            passengerCount = 28,
            earningsAmount = 3500000L,
            timeText = "11:30",
            dateText = "Senin, 24 Nov 2026",
            busId = "bus-2",
            routeId = "route-2"
        )

        val logs = getUseCase().first()
        assertEquals(1, logs.size)
        val log = logs.first()
        assertEquals(SimulationLogCategory.ARRIVAL, log.category)
        assertEquals("Armada Tiba: TN-02", log.title)
        assertTrue(log.message.contains("3.500.000"))
        assertTrue(log.message.contains("Purboyo"))
    }

    @Test
    fun recordTransitLogsProperPassengerCount() = runBlocking {
        recordUseCase.recordTransit(
            busCode = "TN-03",
            routeName = "Surabaya - Banyuwangi",
            transitTerminalName = "Probolinggo",
            boardedCount = 10,
            alightedCount = 6,
            timeText = "10:15",
            dateText = "Senin, 24 Nov 2026",
            busId = "bus-3",
            routeId = "route-3"
        )

        val logs = getUseCase().first()
        assertEquals(1, logs.size)
        val log = logs.first()
        assertEquals(SimulationLogCategory.TRANSIT, log.category)
        assertTrue(log.message.contains("naik: 10"))
        assertTrue(log.message.contains("turun: 6"))
    }

    private class FakeSimulationLogRepository : SimulationLogRepository {
        private val _logs = MutableStateFlow<List<SimulationLog>>(emptyList())

        override fun getRecentLogsFlow(limit: Int): Flow<List<SimulationLog>> = _logs.asStateFlow()

        override suspend fun getRecentLogs(limit: Int): List<SimulationLog> = _logs.value.take(limit)

        override suspend fun addLog(log: SimulationLog) {
            _logs.value = listOf(log) + _logs.value
        }

        override suspend fun clearLogs() {
            _logs.value = emptyList()
        }
    }
}
