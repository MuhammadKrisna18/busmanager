package com.example.busmanager.presentation.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmanager.domain.company.usecase.GetCompanyUseCase
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.driver.usecase.AssignDriverToBusUseCase
import com.example.busmanager.domain.driver.usecase.GetAvailableDriversUseCase
import com.example.busmanager.domain.driver.usecase.GetHiredDriversUseCase
import com.example.busmanager.domain.driver.usecase.HireDriverUseCase
import com.example.busmanager.domain.fleet.usecase.GetOwnedFleetUseCase
import com.example.busmanager.presentation.common.FeedbackToastData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DriverManagementViewModel(
    private val getAvailableDriversUseCase: GetAvailableDriversUseCase,
    private val getHiredDriversUseCase: GetHiredDriversUseCase,
    private val hireDriverUseCase: HireDriverUseCase,
    private val assignDriverToBusUseCase: AssignDriverToBusUseCase,
    private val getOwnedFleetUseCase: GetOwnedFleetUseCase,
    private val getCompanyUseCase: GetCompanyUseCase,
    private val driverRepository: DriverRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverManagementUiState())
    val uiState: StateFlow<DriverManagementUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            driverRepository.seedCandidateDriversIfEmpty()
            combine(
                getAvailableDriversUseCase(),
                getHiredDriversUseCase(),
                getOwnedFleetUseCase(),
                getCompanyUseCase()
            ) { allDrivers, hiredDrivers, buses, company ->
                val candidates = allDrivers.filter { !it.isHired }
                _uiState.update { current ->
                    current.copy(
                        hiredDrivers = hiredDrivers,
                        candidateDrivers = candidates,
                        ownedBuses = buses,
                        companyBalance = company?.balance ?: current.companyBalance,
                        isLoading = false
                    )
                }
            }.collect {}
        }
    }

    fun selectTab(tab: DriverTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun hireDriver(driverId: String) {
        viewModelScope.launch {
            val candidate = _uiState.value.candidateDrivers.find { it.id == driverId }
            val driverName = candidate?.name ?: "Sopir"
            val success = hireDriverUseCase(driverId)
            if (success) {
                _uiState.update {
                    it.copy(
                        message = "Sopir berhasil direkrut ke armada!",
                        feedbackToast = FeedbackToastData(
                            iconEmoji = "👨‍✈️",
                            title = "$driverName Berhasil Direkrut!",
                            subtitle = "Siap ditugaskan berdinas pada armada bus",
                            durationMillis = 1000L
                        )
                    )
                }
            }
        }
    }

    fun openAssignDialog(driver: Driver) {
        _uiState.update { it.copy(selectedDriverForAssignment = driver) }
    }

    fun closeAssignDialog() {
        _uiState.update { it.copy(selectedDriverForAssignment = null) }
    }

    fun assignDriverToBus(driverId: String, busId: String) {
        viewModelScope.launch {
            val driver = _uiState.value.hiredDrivers.find { it.id == driverId }
            val bus = _uiState.value.ownedBuses.find { it.id == busId }
            val success = assignDriverToBusUseCase(driverId, busId)
            if (success) {
                _uiState.update {
                    it.copy(
                        selectedDriverForAssignment = null,
                        message = "Sopir berhasil ditugaskan ke armada!",
                        feedbackToast = FeedbackToastData(
                            iconEmoji = "📋",
                            title = "Penugasan Berhasil!",
                            subtitle = "${driver?.name ?: "Sopir"} ditugaskan ke ${bus?.busCode ?: "armada"}",
                            durationMillis = 1000L
                        )
                    )
                }
            }
        }
    }

    fun openUnassignConfirmation(driver: Driver) {
        _uiState.update { it.copy(driverToUnassign = driver) }
    }

    fun closeUnassignConfirmation() {
        _uiState.update { it.copy(driverToUnassign = null) }
    }

    fun confirmUnassignDriver() {
        val driver = _uiState.value.driverToUnassign ?: return
        executeUnassign(driver)
        _uiState.update { it.copy(driverToUnassign = null) }
    }

    fun unassignDriver(driverId: String) {
        val driver = _uiState.value.hiredDrivers.find { it.id == driverId }
        if (driver != null) {
            executeUnassign(driver)
        } else {
            viewModelScope.launch {
                driverRepository.unassignDriver(driverId)
                _uiState.update { it.copy(message = "Tugas sopir berhasil dilepas") }
            }
        }
    }

    private fun executeUnassign(driver: Driver) {
        viewModelScope.launch {
            val assignedBus = _uiState.value.ownedBuses.find { it.id == driver.assignedBusId }
            if (assignedBus != null && assignedBus.isOperating) {
                driverRepository.setPendingRelease(driver.id, true)
                _uiState.update {
                    it.copy(
                        message = "Sopir ${driver.name} sedang dinas. Tugas akan dilepas otomatis saat armada tiba di terminal."
                    )
                }
            } else {
                driverRepository.unassignDriver(driver.id)
                _uiState.update {
                    it.copy(
                        message = "Tugas sopir berhasil dilepas"
                    )
                }
            }
        }
    }

    fun openFireConfirmation(driver: Driver) {
        _uiState.update { it.copy(driverToFire = driver) }
    }

    fun closeFireConfirmation() {
        _uiState.update { it.copy(driverToFire = null) }
    }

    fun confirmFireDriver() {
        val driver = _uiState.value.driverToFire ?: return
        viewModelScope.launch {
            val assignedBus = _uiState.value.ownedBuses.find { it.id == driver.assignedBusId }
            if (assignedBus != null && assignedBus.isOperating) {
                driverRepository.setPendingRelease(driver.id, true)
                _uiState.update {
                    it.copy(
                        driverToFire = null,
                        message = "Sopir ${driver.name} sedang dinas. PHK akan diproses saat armada tiba di terminal."
                    )
                }
            } else {
                driverRepository.fireDriver(driver.id)
                _uiState.update {
                    it.copy(
                        driverToFire = null,
                        message = "${driver.name} telah diberhentikan (PHK)"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun dismissFeedbackToast() {
        _uiState.update { it.copy(feedbackToast = null) }
    }
}
