package com.example.busmanager.presentation.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.domain.company.usecase.GetCompanyUseCase
import com.example.busmanager.domain.fleet.usecase.GetOwnedFleetUseCase
import com.example.busmanager.domain.simulation.usecase.GetSimulationTimeUseCase
import com.example.busmanager.domain.simulation.usecase.ToggleSimulationPlayUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SimulationMapViewModel(
    private val getSimulationTimeUseCase: GetSimulationTimeUseCase,
    private val toggleSimulationPlayUseCase: ToggleSimulationPlayUseCase,
    private val getCompanyUseCase: GetCompanyUseCase,
    private val getOwnedFleetUseCase: GetOwnedFleetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SimulationMapUiState())
    val uiState: StateFlow<SimulationMapUiState> = _uiState.asStateFlow()

    init {
        observeSimulationEngine()
        observeCompanyAndFleet()
    }

    private fun observeSimulationEngine() {
        viewModelScope.launch {
            combine(
                getSimulationTimeUseCase(),
                toggleSimulationPlayUseCase.isPlaying()
            ) { time, playing ->
                _uiState.update { current ->
                    current.copy(
                        currentTime = time,
                        isPlaying = playing
                    )
                }
            }.collect {}
        }
    }

    private fun observeCompanyAndFleet() {
        viewModelScope.launch {
            combine(
                getCompanyUseCase(),
                getOwnedFleetUseCase()
            ) { company, fleet ->
                _uiState.update { current ->
                    current.copy(
                        companyName = company?.name ?: "PO TransNusantara",
                        balanceFormatted = company?.balance?.formatRupiah() ?: "Rp 0",
                        ownedBusCount = fleet.size
                    )
                }
            }.collect {}
        }
    }

    fun togglePlay() {
        toggleSimulationPlayUseCase()
    }
}
