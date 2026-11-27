package com.example.busmanager.presentation.garage

import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.DailySchedulePlan
import com.example.busmanager.domain.route.model.RouteCategory
import com.example.busmanager.domain.route.model.TerminalDepartureSlot
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.presentation.common.FeedbackToastData
import com.example.busmanager.presentation.route.wizard.SlotTimeFilter

enum class GarageFilter(val displayName: String) {
    ALL("Semua"),
    OPERATING("Berdinas"),
    STANDBY("Di Garasi")
}

data class GarageScheduleTarget(
    val bus: Bus,
    val routeName: String,
    val currentHeadwayMinutes: Int = 60
)

data class QuickAssignState(
    val bus: Bus,
    val availableRoutes: List<BusRoute> = emptyList(),
    val selectedRoute: BusRoute? = null,
    val availableDrivers: List<Driver> = emptyList(),
    val selectedDriver: Driver? = null,
    val routeCategory: RouteCategory = RouteCategory.AKDP,
    val availableSlots: List<TerminalDepartureSlot> = emptyList(),
    val selectedSlot: TerminalDepartureSlot? = null,
    val selectedTimeFilter: SlotTimeFilter = SlotTimeFilter.MORNING,
    val targetRitCount: Int = 2,
    val dailySchedulePlan: DailySchedulePlan? = null,
    val departureHour: Int = 7,
    val departureMinute: Int = 0,
    val direction: RouteDirection = RouteDirection.ORIGIN_TO_DESTINATION,
    val headwayMinutes: Int = 60,
    val transitDwellMinutes: Int = 10,
    val conflictError: String? = null,
    val isSubmitting: Boolean = false
) {
    val canSubmit: Boolean
        get() = selectedRoute != null &&
                selectedDriver != null &&
                selectedSlot != null &&
                selectedSlot.isAvailable &&
                conflictError == null &&
                !isSubmitting
}

data class GarageBusItem(
    val bus: Bus,
    val driverName: String?,
    val routeName: String?,
    val hasPricings: Boolean,
    val isOperating: Boolean,
    val activeTripDirection: String? = null,
    val activeTripProgressPercent: Int? = null,
    val headwayMinutes: Int = 60,
    val transitDwellMinutes: Int = 10,
    val hasSchedules: Boolean = false
) {
    val canOperate: Boolean
        get() = bus.routeId != null && bus.driverId != null && hasPricings

    val isOnDuty: Boolean
        get() = isOperating || canOperate || bus.hasRoute

    val statusLabel: String
        get() = when {
            isOperating -> "● Sedang Berdinas"
            canOperate -> "Menunggu Jadwal Keberangkatan"
            bus.hasRoute -> "Menunggu Sopir"
            else -> "Tersedia di Garasi"
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
    val companyName: String = "PO TransNusantara",
    val editingScheduleTarget: GarageScheduleTarget? = null,
    val quickAssignState: QuickAssignState? = null,
    val userMessage: String? = null,
    val feedbackToast: FeedbackToastData? = null
)
