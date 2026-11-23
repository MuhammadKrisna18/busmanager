package com.example.busmanager.presentation.dashboard

import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.fleet.model.Bus

data class HomeScreenUiState(
    val company: Company? = null,
    val ownedBuses: List<Bus> = emptyList(),
    val isPlaying: Boolean = false,
    val gameDay: Int = 1,
    val gameHour: Int = 8,
    val isLoading: Boolean = true
)
