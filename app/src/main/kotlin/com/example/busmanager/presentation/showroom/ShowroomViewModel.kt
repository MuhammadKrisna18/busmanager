package com.example.busmanager.presentation.showroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.domain.company.usecase.GetCompanyUseCase
import com.example.busmanager.domain.finance.model.FinancialTransaction
import com.example.busmanager.domain.finance.model.TransactionCategory
import com.example.busmanager.domain.finance.model.TransactionType
import com.example.busmanager.domain.finance.repository.FinancialTransactionRepository
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.model.BusClass
import com.example.busmanager.domain.fleet.model.BuyBusResult
import com.example.busmanager.domain.fleet.model.Chassis
import com.example.busmanager.domain.fleet.model.ChassisBrand
import com.example.busmanager.domain.fleet.model.KaroseriBody
import com.example.busmanager.domain.fleet.model.KaroseriBrand
import com.example.busmanager.domain.fleet.repository.ChassisCatalogRepository
import com.example.busmanager.domain.fleet.repository.KaroseriCatalogRepository
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
    private val buyBusUseCase: BuyBusUseCase,
    private val getOwnedFleetUseCase: GetOwnedFleetUseCase,
    private val chassisCatalogRepository: ChassisCatalogRepository,
    private val karoseriCatalogRepository: KaroseriCatalogRepository,
    private val financialTransactionRepository: FinancialTransactionRepository? = null,
    private val getShowroomBusesUseCase: GetShowroomBusesUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShowroomUiState())
    val uiState: StateFlow<ShowroomUiState> = _uiState.asStateFlow()

    init {
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            combine(
                getCompanyUseCase(),
                getOwnedFleetUseCase(),
                chassisCatalogRepository.getAllChassis(),
                karoseriCatalogRepository.getAllBodies()
            ) { company, ownedBuses, chassisList, bodyList ->
                val nextNumber = ownedBuses.size + 1
                val defaultCode = String.format("TN-%02d", nextNumber)

                _uiState.update { current ->
                    current.copy(
                        company = company,
                        ownedBusCount = ownedBuses.size,
                        allChassis = chassisList,
                        allBodies = bodyList,
                        customBusCode = if (current.customBusCode.isBlank()) defaultCode else current.customBusCode,
                        isLoading = false
                    )
                }
            }.collect {}
        }

        if (getShowroomBusesUseCase != null) {
            viewModelScope.launch {
                getShowroomBusesUseCase.invoke().collect { readyStocks ->
                    _uiState.update { it.copy(readyStockBuses = readyStocks) }
                }
            }
        }
    }

    fun selectChassis(chassis: Chassis) {
        _uiState.update { current ->
            val isBodyStillCompatible = current.selectedBody?.let { body ->
                if (body.requiresTronton && chassis.axleType != com.example.busmanager.domain.fleet.model.AxleType.TRONTON_6X2) false
                else if (body.bodyType == com.example.busmanager.domain.fleet.model.BusBodyType.UHD && chassis.axleType != com.example.busmanager.domain.fleet.model.AxleType.TRONTON_6X2) false
                else if (chassis.engineLocation == com.example.busmanager.domain.fleet.model.EngineLocation.FRONT && body.bodyType != com.example.busmanager.domain.fleet.model.BusBodyType.HDD) false
                else true
            } ?: false

            current.copy(
                selectedChassis = chassis,
                selectedBody = if (isBodyStillCompatible) current.selectedBody else null
            )
        }
    }

    fun setChassisBrandFilter(brand: ChassisBrand?) {
        _uiState.update { it.copy(selectedChassisFilter = brand) }
    }

    fun selectBody(body: KaroseriBody) {
        _uiState.update { it.copy(selectedBody = body) }
    }

    fun setKaroseriBrandFilter(brand: KaroseriBrand?) {
        _uiState.update { it.copy(selectedKaroseriFilter = brand) }
    }

    fun selectBusClass(busClass: BusClass) {
        _uiState.update { it.copy(selectedBusClass = busClass) }
    }

    fun updateBusCode(code: String) {
        _uiState.update { it.copy(customBusCode = code.trim().uppercase()) }
    }

    fun goToStep(step: ShowroomStep) {
        when (step) {
            ShowroomStep.CHASSIS -> _uiState.update { it.copy(currentStep = step) }
            ShowroomStep.BODY -> if (_uiState.value.selectedChassis != null) {
                _uiState.update { it.copy(currentStep = step) }
            }
            ShowroomStep.CLASS -> if (_uiState.value.selectedChassis != null && _uiState.value.selectedBody != null) {
                _uiState.update { it.copy(currentStep = step) }
            }
            ShowroomStep.REVIEW -> if (_uiState.value.selectedChassis != null && _uiState.value.selectedBody != null) {
                _uiState.update { it.copy(currentStep = step) }
            }
        }
    }

    fun nextStep() {
        val current = _uiState.value
        when (current.currentStep) {
            ShowroomStep.CHASSIS -> if (current.selectedChassis != null) {
                _uiState.update { it.copy(currentStep = ShowroomStep.BODY) }
            }
            ShowroomStep.BODY -> if (current.selectedBody != null) {
                _uiState.update { it.copy(currentStep = ShowroomStep.CLASS) }
            }
            ShowroomStep.CLASS -> {
                _uiState.update { it.copy(currentStep = ShowroomStep.REVIEW) }
            }
            ShowroomStep.REVIEW -> {}
        }
    }

    fun previousStep() {
        val current = _uiState.value
        when (current.currentStep) {
            ShowroomStep.CHASSIS -> {}
            ShowroomStep.BODY -> _uiState.update { it.copy(currentStep = ShowroomStep.CHASSIS) }
            ShowroomStep.CLASS -> _uiState.update { it.copy(currentStep = ShowroomStep.BODY) }
            ShowroomStep.REVIEW -> _uiState.update { it.copy(currentStep = ShowroomStep.CLASS) }
        }
    }

    fun setMode(mode: ShowroomMode) {
        _uiState.update { it.copy(mode = mode, errorMessage = null) }
    }

    fun setBatchQuantity(quantity: Int) {
        _uiState.update { it.copy(batchQuantity = quantity.coerceIn(1, 20)) }
    }

    fun setInstantQuantity(busId: String, quantity: Int) {
        _uiState.update { current ->
            val updated = current.instantBatchQuantities.toMutableMap()
            updated[busId] = quantity.coerceIn(1, 20)
            current.copy(instantBatchQuantities = updated)
        }
    }

    fun confirmPurchase() {
        val state = _uiState.value
        val chassis = state.selectedChassis ?: return
        val body = state.tailoredBody ?: return
        val code = state.customBusCode.ifBlank {
            String.format("TN-%02d", state.ownedBusCount + 1)
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPurchasing = true, errorMessage = null) }

            val busToBuy = Bus(
                id = UUID.randomUUID().toString(),
                busCode = code,
                chassis = chassis,
                body = body
            )

            val quantity = state.batchQuantity.coerceAtLeast(1)
            val result = if (quantity > 1) {
                buyBusUseCase.buyBatch(busToBuy, quantity)
            } else {
                buyBusUseCase(busToBuy)
            }

            when (result) {
                is BuyBusResult.Success -> {
                    recordBusPurchaseTransaction(result.bus)
                    _uiState.update {
                        it.copy(
                            isPurchasing = false,
                            purchaseSuccessBus = result.bus,
                            purchaseSuccessBuses = listOf(result.bus),
                            errorMessage = null
                        )
                    }
                }
                is BuyBusResult.BatchSuccess -> {
                    result.buses.forEach { recordBusPurchaseTransaction(it) }
                    _uiState.update {
                        it.copy(
                            isPurchasing = false,
                            purchaseSuccessBus = result.buses.firstOrNull(),
                            purchaseSuccessBuses = result.buses,
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

    fun buyInstantBus(bus: Bus) {
        val state = _uiState.value
        val quantity = state.getInstantQuantity(bus.id)
        viewModelScope.launch {
            _uiState.update { it.copy(isPurchasing = true, errorMessage = null) }
            val result = if (quantity > 1) {
                buyBusUseCase.buyBatch(bus, quantity)
            } else {
                val singleBus = bus.copy(
                    id = UUID.randomUUID().toString(),
                    busCode = String.format("TN-%02d", state.ownedBusCount + 1)
                )
                buyBusUseCase(singleBus)
            }

            when (result) {
                is BuyBusResult.Success -> {
                    recordBusPurchaseTransaction(result.bus)
                    _uiState.update {
                        it.copy(
                            isPurchasing = false,
                            purchaseSuccessBus = result.bus,
                            purchaseSuccessBuses = listOf(result.bus),
                            errorMessage = null
                        )
                    }
                }
                is BuyBusResult.BatchSuccess -> {
                    result.buses.forEach { recordBusPurchaseTransaction(it) }
                    _uiState.update {
                        it.copy(
                            isPurchasing = false,
                            purchaseSuccessBus = result.buses.firstOrNull(),
                            purchaseSuccessBuses = result.buses,
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

    private suspend fun recordBusPurchaseTransaction(bus: Bus) {
        val transaction = FinancialTransaction(
            id = "tx-bus-${bus.id}-${System.nanoTime()}",
            dateText = "Hari 1",
            timeText = "08:00",
            day = 1,
            month = 1,
            year = 2026,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.BUS_PURCHASE,
            amount = bus.totalPrice,
            busId = bus.id,
            busCode = bus.busCode,
            description = "Pembelian armada baru: ${bus.busCode} (${bus.fullName})"
        )
        financialTransactionRepository?.recordTransaction(transaction)
    }

    fun resetBuilder() {
        val nextNumber = _uiState.value.ownedBusCount + 1
        val defaultCode = String.format("TN-%02d", nextNumber)
        _uiState.update {
            it.copy(
                currentStep = ShowroomStep.CHASSIS,
                selectedChassis = null,
                selectedBody = null,
                selectedBusClass = BusClass.EKSEKUTIF,
                customBusCode = defaultCode,
                purchaseSuccessBus = null,
                purchaseSuccessBuses = emptyList(),
                errorMessage = null
            )
        }
    }

    fun dismissSuccessNotice() {
        resetBuilder()
    }
}
