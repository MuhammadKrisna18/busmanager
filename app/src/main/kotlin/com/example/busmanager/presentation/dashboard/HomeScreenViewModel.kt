package com.example.busmanager.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.domain.company.usecase.GetCompanyUseCase
import com.example.busmanager.domain.company.usecase.StartNewGameUseCase
import com.example.busmanager.domain.fleet.usecase.GetOwnedFleetUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val getCompanyUseCase: GetCompanyUseCase,
    private val getOwnedFleetUseCase: GetOwnedFleetUseCase,
    private val startNewGameUseCase: StartNewGameUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    init {
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            startNewGameUseCase()
            combine(
                getCompanyUseCase(),
                getOwnedFleetUseCase()
            ) { company, buses ->
                _uiState.update { current ->
                    current.copy(
                        company = company,
                        ownedBuses = buses,
                        isLoading = false
                    )
                }
            }.collect {}
        }
    }

    fun togglePlaySimulation() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }
}
