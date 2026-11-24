package com.example.busmanager.presentation.garage

import com.example.busmanager.domain.fleet.model.Bus

enum class GarageFilter(val displayName: String) {
    ALL("Semua"),
    OPERATING("Berdinas"),
    STANDBY("Di Garasi")
}

data class GarageBusItem(
    val bus: Bus,
    val driverName: String?,
    val routeName: String?,
    val hasPricings: Boolean,
    val isOperating: Boolean,
    val activeTripDirection: String? = null,
    val activeTripProgressPercent: Int? = null
) {
    val canOperate: Boolean
        get() = bus.routeId != null && bus.driverId != null && hasPricings

    val statusLabel: String
        get() = when {
            isOperating -> "● Berdinas"
            canOperate -> "Siap Beroperasi"
            else -> "Belum Siap Operasi"
        }
}

data class GarageUiState(
    val allBuses: List<GarageBusItem> = emptyList(),
    val filteredBuses: List<GarageBusItem> = emptyList(),
    val selectedFilter: GarageFilter = GarageFilter.ALL,
    val totalBusCount: Int = 0,
    val operatingBusCount: Int = 0,
    val standbyBusCount: Int = 0,
    val isLoading: Boolean = false,
    val companyName: String = "PO TransNusantara"
)
