package com.example.busmanager.presentation.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.repository.TerminalRepository
import com.example.busmanager.domain.route.usecase.GetRouteOperationalStatusUseCase
import com.example.busmanager.domain.simulation.repository.SimulationTimeRepository
import com.example.busmanager.domain.terminal.model.TerminalBusBreakdown
import com.example.busmanager.domain.terminal.repository.TerminalPassengerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TerminalReportViewModel(
    private val terminalRepository: TerminalRepository,
    private val terminalPassengerRepository: TerminalPassengerRepository,
    private val simulationTimeRepository: SimulationTimeRepository? = null,
    private val getRouteOperationalStatusUseCase: GetRouteOperationalStatusUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(TerminalReportUiState())
    val uiState: StateFlow<TerminalReportUiState> = _uiState.asStateFlow()

    init {
        loadTerminalsAndCurrentTime()
        observeTime()
        observeOperationalRoutes()
        observeRecords()
    }

    private fun loadTerminalsAndCurrentTime() {
        val currentTime = simulationTimeRepository?.currentTime?.value
        val initialTerminals = if (getRouteOperationalStatusUseCase != null) {
            emptyList()
        } else {
            terminalRepository.getAllTerminals()
        }
        val initialTerminal = initialTerminals.firstOrNull()
        _uiState.update { current ->
            current.copy(
                availableTerminals = initialTerminals,
                selectedTerminal = initialTerminal,
                hasActiveTerminals = initialTerminals.isNotEmpty(),
                selectedDay = currentTime?.day ?: current.selectedDay,
                selectedMonth = currentTime?.month ?: current.selectedMonth,
                selectedYear = currentTime?.year ?: current.selectedYear
            )
        }
        loadRecordsForSelection()
    }

    private fun observeTime() {
        if (simulationTimeRepository == null) return
        viewModelScope.launch {
            simulationTimeRepository.currentTime.collect { time ->
                if (!_uiState.value.isHistoricalView) {
                    if (_uiState.value.selectedDay != time.day ||
                        _uiState.value.selectedMonth != time.month ||
                        _uiState.value.selectedYear != time.year
                    ) {
                        _uiState.update {
                            it.copy(
                                selectedDay = time.day,
                                selectedMonth = time.month,
                                selectedYear = time.year
                            )
                        }
                        loadRecordsForSelection()
                    }
                }
            }
        }
    }

    private fun observeOperationalRoutes() {
        if (getRouteOperationalStatusUseCase == null) return
        viewModelScope.launch {
            combine(
                getRouteOperationalStatusUseCase(),
                terminalPassengerRepository.getAllRecordsFlow()
            ) { routeStatuses, records ->
                val activeRoutes = routeStatuses.filter { it.isActive || it.assignedBuses.isNotEmpty() }
                val routeTerminals = activeRoutes.flatMap { operational ->
                    val r = operational.route
                    listOf(r.originTerminal, r.destinationTerminal) + r.transitTerminals
                }
                val recordedTerminalIds = records.map { it.terminalId }.toSet()
                val recordedTerminals = recordedTerminalIds.mapNotNull { terminalRepository.getTerminalById(it) }
                (routeTerminals + recordedTerminals).distinctBy { it.id }
            }.collect { targetTerminals ->
                _uiState.update { current ->
                    val currentSelectedStillValid = targetTerminals.any { it.id == current.selectedTerminal?.id }
                    val newSelected = if (currentSelectedStillValid) {
                        current.selectedTerminal
                    } else {
                        targetTerminals.firstOrNull()
                    }
                    current.copy(
                        availableTerminals = targetTerminals,
                        selectedTerminal = newSelected,
                        hasActiveTerminals = targetTerminals.isNotEmpty()
                    )
                }
                loadRecordsForSelection()
            }
        }
    }

    fun selectTerminal(terminal: Terminal) {
        _uiState.update { it.copy(selectedTerminal = terminal) }
        loadRecordsForSelection()
    }

    fun selectDate(day: Int, month: Int, year: Int) {
        _uiState.update {
            it.copy(
                selectedDay = day,
                selectedMonth = month,
                selectedYear = year
            )
        }
        loadRecordsForSelection()
    }

    fun openHistoryModal() {
        viewModelScope.launch {
            val dates = terminalPassengerRepository.getAvailableDates()
            _uiState.update {
                it.copy(
                    availableHistoryDates = dates,
                    showHistoryModal = true
                )
            }
        }
    }

    fun dismissHistoryModal() {
        _uiState.update { it.copy(showHistoryModal = false) }
    }

    fun selectHistoryDate(day: Int, month: Int, year: Int) {
        _uiState.update {
            it.copy(
                selectedDay = day,
                selectedMonth = month,
                selectedYear = year,
                isHistoricalView = true,
                showHistoryModal = false
            )
        }
        loadRecordsForSelection()
    }

    fun resetToCurrentLiveDate() {
        val currentTime = simulationTimeRepository?.currentTime?.value
        _uiState.update {
            it.copy(
                selectedDay = currentTime?.day ?: it.selectedDay,
                selectedMonth = currentTime?.month ?: it.selectedMonth,
                selectedYear = currentTime?.year ?: it.selectedYear,
                isHistoricalView = false
            )
        }
        loadRecordsForSelection()
    }

    fun openBreakdownModal() {
        _uiState.update { it.copy(showBreakdownModal = true) }
    }

    fun dismissBreakdownModal() {
        _uiState.update { it.copy(showBreakdownModal = false) }
    }

    private fun observeRecords() {
        viewModelScope.launch {
            terminalPassengerRepository.getAllRecordsFlow().collect {
                loadRecordsForSelection()
            }
        }
    }

    private fun loadRecordsForSelection() {
        val terminal = _uiState.value.selectedTerminal
        if (terminal == null) {
            _uiState.update { current ->
                current.copy(
                    records = emptyList(),
                    totalBoarded = 0,
                    totalAlighted = 0,
                    busBreakdowns = emptyList()
                )
            }
            return
        }
        val day = _uiState.value.selectedDay
        val month = _uiState.value.selectedMonth
        val year = _uiState.value.selectedYear

        viewModelScope.launch {
            terminalPassengerRepository.getRecordsByTerminalAndDateFlow(
                terminalId = terminal.id,
                day = day,
                month = month,
                year = year
            ).collect { filteredRecords ->
                val totalBoarded = filteredRecords.sumOf { it.boardedCount }
                val totalAlighted = filteredRecords.sumOf { it.alightedCount }

                val groupedByBus = filteredRecords.groupBy { it.busId }
                val breakdowns = groupedByBus.map { (busId, records) ->
                    val firstRecord = records.first()
                    TerminalBusBreakdown(
                        busId = busId,
                        busCode = firstRecord.busCode,
                        routeName = firstRecord.routeName,
                        totalBoarded = records.sumOf { it.boardedCount },
                        totalAlighted = records.sumOf { it.alightedCount },
                        tripCount = records.size
                    )
                }.sortedByDescending { it.totalBoarded + it.totalAlighted }

                _uiState.update { current ->
                    current.copy(
                        records = filteredRecords,
                        totalBoarded = totalBoarded,
                        totalAlighted = totalAlighted,
                        busBreakdowns = breakdowns
                    )
                }
            }
        }
    }
}
