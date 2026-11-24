package com.example.busmanager.presentation.terminal

import com.example.busmanager.data.repository.SimulationTimeRepositoryImpl
import com.example.busmanager.data.repository.TerminalCatalogRepositoryImpl
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.terminal.model.TerminalPassengerRecord
import com.example.busmanager.domain.terminal.repository.TerminalPassengerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TerminalReportViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var terminalRepository: TerminalCatalogRepositoryImpl
    private lateinit var fakePassengerRepo: FakeTerminalPassengerRepository
    private lateinit var simulationTimeRepo: SimulationTimeRepositoryImpl
    private lateinit var viewModel: TerminalReportViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        terminalRepository = TerminalCatalogRepositoryImpl()
        fakePassengerRepo = FakeTerminalPassengerRepository()
        simulationTimeRepo = SimulationTimeRepositoryImpl(
            initialTime = GameCalendar(2026, 11, 24, 10, 0)
        )
        viewModel = TerminalReportViewModel(
            terminalRepository = terminalRepository,
            terminalPassengerRepository = fakePassengerRepo,
            simulationTimeRepository = simulationTimeRepo
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsTerminalsAndInitialDate() = runTest(testDispatcher) {
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals(6, state.availableTerminals.size)
        assertNotNull(state.selectedTerminal)
        assertEquals("term_purabaya", state.selectedTerminal?.id)
        assertEquals(24, state.selectedDay)
        assertEquals(11, state.selectedMonth)
        assertEquals(2026, state.selectedYear)
        assertEquals(0, state.totalBoarded)
        assertEquals(0, state.totalAlighted)
        assertTrue(state.records.isEmpty())
        assertFalse(state.showBreakdownModal)
    }

    @Test
    fun selectTerminal_updatesSelectedTerminalAndReloads() = runTest(testDispatcher) {
        advanceUntilIdle()
        val arjosari = terminalRepository.getTerminalById("term_arjosari")!!

        viewModel.selectTerminal(arjosari)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("term_arjosari", state.selectedTerminal?.id)
    }

    @Test
    fun selectDate_updatesSelectedDate() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.selectDate(25, 11, 2026)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(25, state.selectedDay)
        assertEquals(11, state.selectedMonth)
        assertEquals(2026, state.selectedYear)
    }

    @Test
    fun previousDayAndNextDay_adjustDayCorrectly() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.nextDay()
        advanceUntilIdle()
        assertEquals(25, viewModel.uiState.value.selectedDay)

        viewModel.previousDay()
        advanceUntilIdle()
        assertEquals(24, viewModel.uiState.value.selectedDay)
    }

    @Test
    fun recordsCalculation_aggregatesBoardedAlightedAndBusBreakdowns() = runTest(testDispatcher) {
        advanceUntilIdle()

        val sampleRecords = listOf(
            TerminalPassengerRecord(
                id = "rec-1",
                terminalId = "term_purabaya",
                terminalName = "Terminal Purabaya",
                busId = "bus-1",
                busCode = "TN-01",
                routeId = "route-sub-mlg",
                routeName = "Surabaya - Malang",
                day = 24,
                month = 11,
                year = 2026,
                dateFormatted = "24/11/2026",
                timeFormatted = "08:30 WIB",
                boardedCount = 28,
                alightedCount = 0
            ),
            TerminalPassengerRecord(
                id = "rec-2",
                terminalId = "term_purabaya",
                terminalName = "Terminal Purabaya",
                busId = "bus-2",
                busCode = "TN-02",
                routeId = "route-sub-pro",
                routeName = "Surabaya - Probolinggo",
                day = 24,
                month = 11,
                year = 2026,
                dateFormatted = "24/11/2026",
                timeFormatted = "09:00 WIB",
                boardedCount = 35,
                alightedCount = 12
            ),
            TerminalPassengerRecord(
                id = "rec-3",
                terminalId = "term_purabaya",
                terminalName = "Terminal Purabaya",
                busId = "bus-1",
                busCode = "TN-01",
                routeId = "route-sub-mlg",
                routeName = "Surabaya - Malang",
                day = 24,
                month = 11,
                year = 2026,
                dateFormatted = "24/11/2026",
                timeFormatted = "14:15 WIB",
                boardedCount = 15,
                alightedCount = 20
            )
        )

        fakePassengerRepo.setRecords(sampleRecords)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.records.size)
        assertEquals(78, state.totalBoarded)
        assertEquals(32, state.totalAlighted)

        assertEquals(2, state.busBreakdowns.size)
        val bus1Breakdown = state.busBreakdowns.find { it.busId == "bus-1" }
        assertNotNull(bus1Breakdown)
        assertEquals("TN-01", bus1Breakdown?.busCode)
        assertEquals("Surabaya - Malang", bus1Breakdown?.routeName)
        assertEquals(43, bus1Breakdown?.totalBoarded)
        assertEquals(20, bus1Breakdown?.totalAlighted)
        assertEquals(2, bus1Breakdown?.tripCount)

        val bus2Breakdown = state.busBreakdowns.find { it.busId == "bus-2" }
        assertNotNull(bus2Breakdown)
        assertEquals("TN-02", bus2Breakdown?.busCode)
        assertEquals(35, bus2Breakdown?.totalBoarded)
        assertEquals(12, bus2Breakdown?.totalAlighted)
        assertEquals(1, bus2Breakdown?.tripCount)
    }

    @Test
    fun modalVisibility_opensAndDismissesCorrectly() = runTest(testDispatcher) {
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showBreakdownModal)

        viewModel.openBreakdownModal()
        assertTrue(viewModel.uiState.value.showBreakdownModal)

        viewModel.dismissBreakdownModal()
        assertFalse(viewModel.uiState.value.showBreakdownModal)
    }
}

class FakeTerminalPassengerRepository : TerminalPassengerRepository {
    private val records = MutableStateFlow<List<TerminalPassengerRecord>>(emptyList())

    override fun getAllRecordsFlow(): Flow<List<TerminalPassengerRecord>> = records

    override fun getRecordsByTerminalAndDateFlow(
        terminalId: String,
        day: Int,
        month: Int,
        year: Int
    ): Flow<List<TerminalPassengerRecord>> {
        return records.map { list ->
            list.filter {
                it.terminalId == terminalId &&
                    it.day == day &&
                    it.month == month &&
                    it.year == year
            }
        }
    }

    override fun getRecordsByTerminalFlow(terminalId: String): Flow<List<TerminalPassengerRecord>> {
        return records.map { list ->
            list.filter { it.terminalId == terminalId }
        }
    }

    override suspend fun recordPassengerExchange(record: TerminalPassengerRecord) {
        records.value = records.value + record
    }

    override suspend fun getAvailableDates(): List<Triple<Int, Int, Int>> {
        return records.value.map { Triple(it.day, it.month, it.year) }.distinct()
    }

    fun setRecords(newRecords: List<TerminalPassengerRecord>) {
        records.value = newRecords
    }
}
