package com.example.busmanager.presentation.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.finance.usecase.GetFinancialTransactionsUseCase
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.repository.RouteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FinanceViewModel(
    private val companyRepository: CompanyRepository,
    private val getFinancialTransactionsUseCase: GetFinancialTransactionsUseCase,
    private val fleetRepository: FleetRepository,
    private val routeRepository: RouteRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            companyRepository.getCompany().collect { comp ->
                _uiState.update { it.copy(company = comp) }
            }
        }

        viewModelScope.launch {
            getFinancialTransactionsUseCase().collect { txList ->
                _uiState.update { it.copy(transactions = txList) }
            }
        }

        viewModelScope.launch {
            fleetRepository.getOwnedBuses().collect { buses ->
                _uiState.update { it.copy(availableBuses = buses) }
            }
        }
    }

    fun setTypeFilter(filter: TransactionTypeFilter) {
        _uiState.update { it.copy(selectedTypeFilter = filter) }
    }

    fun setBusFilter(busId: String?) {
        _uiState.update { it.copy(selectedBusId = busId) }
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(
                selectedTypeFilter = TransactionTypeFilter.ALL,
                selectedBusId = null
            )
        }
    }
}
