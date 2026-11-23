package com.example.busmanager.presentation.showroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.domain.company.usecase.GetCompanyUseCase
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.model.BuyBusResult
import com.example.busmanager.domain.fleet.model.ChassisBrand
import com.example.busmanager.domain.fleet.model.KaroseriBrand
import com.example.busmanager.domain.fleet.usecase.BuyBusUseCase
import com.example.busmanager.domain.fleet.usecase.GetOwnedFleetUseCase
import com.example.busmanager.domain.fleet.usecase.GetShowroomBusesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ShowroomViewModel(
    private val getCompanyUseCase: GetCompanyUseCase,
    private val getShowroomBusesUseCase: GetShowroomBusesUseCase,
    private val buyBusUseCase: BuyBusUseCase,
    private val getOwnedFleetUseCase: GetOwnedFleetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShowroomUiState())
    val uiState: StateFlow<ShowroomUiState> = _uiState.asStateFlow()

    private var ownedBusCount = 0

    init {
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            combine(
                getCompanyUseCase(),
                getShowroomBusesUseCase(),
                getOwnedFleetUseCase()
            ) { company, readyStocks, ownedBuses ->
                ownedBusCount = ownedBuses.size
                _uiState.update { current ->
                    current.copy(
                        company = company,
                        readyStockBuses = readyStocks,
                        filteredBuses = applyFilters(
                            readyStocks,
                            current.selectedKaroseriFilter,
                            current.selectedChassisFilter
                        ),
                        isLoading = false
                    )
                }
            }.collect {}
        }
    }

    fun setKaroseriFilter(brand: KaroseriBrand?) {
        _uiState.update { current ->
            current.copy(
                selectedKaroseriFilter = brand,
                filteredBuses = applyFilters(
                    current.readyStockBuses,
                    brand,
                    current.selectedChassisFilter
                )
            )
        }
    }

    fun setChassisFilter(brand: ChassisBrand?) {
        _uiState.update { current ->
            current.copy(
                selectedChassisFilter = brand,
                filteredBuses = applyFilters(
                    current.readyStockBuses,
                    current.selectedKaroseriFilter,
                    brand
                )
            )
        }
    }

    fun openPurchaseDialog(bus: Bus) {
        val nextNumber = ownedBusCount + 1
        val defaultCode = String.format("TN-%02d", nextNumber)
        _uiState.update {
            it.copy(
                selectedBusForPurchase = bus,
                customBusCode = defaultCode,
                errorMessage = null
            )
        }
    }

    fun updateBusCode(code: String) {
        _uiState.update { it.copy(customBusCode = code.trim().uppercase()) }
    }

    fun dismissPurchaseDialog() {
        _uiState.update {
            it.copy(
                selectedBusForPurchase = null,
                customBusCode = "",
                errorMessage = null
            )
        }
    }

    fun dismissSuccessNotice() {
        _uiState.update { it.copy(purchaseSuccessBus = null) }
    }

    fun confirmPurchase() {
        val selected = _uiState.value.selectedBusForPurchase ?: return
        val code = _uiState.value.customBusCode.ifBlank {
            String.format("TN-%02d", ownedBusCount + 1)
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPurchasing = true, errorMessage = null) }

            val busToBuy = selected.copy(
                id = UUID.randomUUID().toString(),
                busCode = code
            )

            when (val result = buyBusUseCase(busToBuy)) {
                is BuyBusResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isPurchasing = false,
                            selectedBusForPurchase = null,
                            purchaseSuccessBus = result.bus,
                            errorMessage = null
                        )
                    }
                }
                is BuyBusResult.InsufficientFunds -> {
                    _uiState.update {
                        it.copy(
                            isPurchasing = false,
                            errorMessage = "Saldo tidak mencukupi. Diperlukan ${result.required.formatRupiah()}"
                        )
                    }
                }
                is BuyBusResult.CompanyNotFound -> {
                    _uiState.update {
                        it.copy(
                            isPurchasing = false,
                            errorMessage = "Data perusahaan tidak ditemukan"
                        )
                    }
                }
            }
        }
    }

    private fun applyFilters(
        buses: List<Bus>,
        karoseriBrand: KaroseriBrand?,
        chassisBrand: ChassisBrand?
    ): List<Bus> {
        return buses.filter { bus ->
            val matchKaroseri = karoseriBrand == null || bus.body.brand == karoseriBrand
            val matchChassis = chassisBrand == null || bus.chassis.brand == chassisBrand
            matchKaroseri && matchChassis
        }
    }
}
