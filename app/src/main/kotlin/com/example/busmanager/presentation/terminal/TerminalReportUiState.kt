package com.example.busmanager.presentation.terminal

import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.terminal.model.TerminalBusBreakdown
import com.example.busmanager.domain.terminal.model.TerminalPassengerRecord

data class TerminalReportUiState(
    val availableTerminals: List<Terminal> = emptyList(),
    val selectedTerminal: Terminal? = null,
    val selectedDay: Int = 24,
    val selectedMonth: Int = 11,
    val selectedYear: Int = 2026,
    val totalBoarded: Int = 0,
    val totalAlighted: Int = 0,
    val records: List<TerminalPassengerRecord> = emptyList(),
    val busBreakdowns: List<TerminalBusBreakdown> = emptyList(),
    val showBreakdownModal: Boolean = false,
    val showHistoryModal: Boolean = false,
    val isHistoricalView: Boolean = false,
    val availableHistoryDates: List<Triple<Int, Int, Int>> = emptyList(),
    val hasActiveTerminals: Boolean = true,
    val isLoading: Boolean = false
) {
    val selectedDateFormatted: String
        get() {
            val calendar = GameCalendar(year = selectedYear, month = selectedMonth, day = selectedDay, hour = 0, minute = 0)
            return "${calendar.dayOfWeekName()}, ${calendar.formatDate()}"
        }

    val hasData: Boolean
        get() = records.isNotEmpty()
}
