package com.example.busmanager.presentation.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.repository.TerminalRepository
import com.example.busmanager.domain.simulation.repository.SimulationTimeRepository
import com.example.busmanager.domain.terminal.model.TerminalBusBreakdown
import com.example.busmanager.domain.terminal.repository.TerminalPassengerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TerminalReportViewModel(
    private val terminalRepository: TerminalRepository,
    private val terminalPassengerRepository: TerminalPassengerRepository,
    private val simulationTimeRepository: SimulationTimeRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(TerminalReportUiState())
    val uiState: StateFlow<TerminalReportUiState> = _uiState.asStateFlow()

    init {
        loadTerminalsAndCurrentTime()
        observeRecords()
    }

    private fun loadTerminalsAndCurrentTime() {
        val terminals = terminalRepository.getAllTerminals()
        val currentTime = simulationTimeRepository?.currentTime?.value
        val initialTerminal = terminals.firstOrNull()
        _uiState.update { current ->
            current.copy(
                availableTerminals = terminals,
                selectedTerminal = initialTerminal,
                selectedDay = currentTime?.day ?: current.selectedDay,
                selectedMonth = currentTime?.month ?: current.selectedMonth,
                selectedYear = currentTime?.year ?: current.selectedYear
            )
        }
        loadRecordsForSelection()
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

    fun previousDay() {
        val currentDay = _uiState.value.selectedDay
        if (currentDay > 1) {
            selectDate(currentDay - 1, _uiState.value.selectedMonth, _uiState.value.selectedYear)
        }
    }

    fun nextDay() {
        val currentDay = _uiState.value.selectedDay
        selectDate(currentDay + 1, _uiState.value.selectedMonth, _uiState.value.selectedYear)
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
        val terminal = _uiState.value.selectedTerminal ?: return
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
