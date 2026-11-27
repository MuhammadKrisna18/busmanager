package com.example.busmanager.presentation.route.wizard

import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.DailySchedulePlan
import com.example.busmanager.domain.route.model.RouteCategory
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.model.RouteSegment
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.model.TerminalDepartureSlot
import com.example.busmanager.domain.simulation.model.RouteDirection

enum class RouteWizardStep(val stepNumber: Int, val title: String) {
    ORIGIN_DESTINATION(1, "Asal & Tujuan"),
    PATH_AND_STOPS(2, "Jalur & Halte"),
    PRICING_MATRIX(3, "Tarif Tiket"),
    DEPARTURE_SCHEDULE(4, "Jadwal Slot"),
    BUS_DRIVER_ASSIGNMENT(5, "Bus & Kru")
}

data class RouteWizardUiState(
    val currentStep: RouteWizardStep = RouteWizardStep.ORIGIN_DESTINATION,
    val allTerminals: List<Terminal> = emptyList(),
    val allSegments: List<RouteSegment> = emptyList(),
    val selectedPathNodes: List<Terminal> = emptyList(),
    val selectedOrigin: Terminal? = null,
    val selectedDestination: Terminal? = null,

    val availablePaths: List<List<Terminal>> = emptyList(),
    val selectedPathIndex: Int = 0,
    val isExpress: Boolean = false,
    val activeTransitIds: Set<String> = emptySet(),
    val segmentPathTypes: Map<Pair<String, String>, RoutePathType> = emptyMap(),
    val builtRoute: BusRoute? = null,

    val pricings: List<RoutePricing> = emptyList(),
    val editingPricing: RoutePricing? = null,

    val routeCategory: RouteCategory = RouteCategory.AKDP,
    val availableDepartureSlots: List<TerminalDepartureSlot> = emptyList(),
    val selectedDepartureSlot: TerminalDepartureSlot? = null,
    val selectedTimeFilter: SlotTimeFilter = SlotTimeFilter.MORNING,
    val targetRitCount: Int = 2,
    val dailySchedulePlan: DailySchedulePlan? = null,

    val availableBuses: List<Bus> = emptyList(),
    val selectedBus: Bus? = null,
    val availableDrivers: List<Driver> = emptyList(),
    val selectedDriver: Driver? = null,
    val driverAssignedBusCodes: Map<String, String> = emptyMap(),
    val departureHour: Int = 7,
    val departureMinute: Int = 0,
    val departureTimeInputText: String = "07:00",
    val departureTimeDescription: String = "07:00 WIB (07.00 Pagi)",
    val isRoundTrip: Boolean = true,
    val returnHour: Int = 10,
    val returnMinute: Int = 0,
    val direction: RouteDirection = RouteDirection.ORIGIN_TO_DESTINATION,
    val headwayMinutes: Int = 60,
    val transitDwellMinutes: Int = 10,
    val hasOutboundConflict: Boolean = false,
    val hasReturnConflict: Boolean = false,

    val isSaving: Boolean = false,
    val showConfirmationDialog: Boolean = false,
    val errorMessage: String? = null,
    val launchSuccess: Boolean = false,
    val isStandbySaved: Boolean = false,
    val launchedRouteId: String? = null
) {
    val returnDirection: RouteDirection
        get() = if (direction == RouteDirection.ORIGIN_TO_DESTINATION) {
            RouteDirection.DESTINATION_TO_ORIGIN
        } else {
            RouteDirection.ORIGIN_TO_DESTINATION
        }

    val estimatedDurationMinutes: Int
        get() = builtRoute?.totalDurationWithDwellMinutes ?: 0

    val outboundDepartureTotalMinutes: Int
        get() = departureHour * 60 + departureMinute

    val arrivalTotalMinutes: Int
        get() = outboundDepartureTotalMinutes + estimatedDurationMinutes

    val arrivalHour: Int
        get() = (arrivalTotalMinutes / 60) % 24

    val arrivalMinute: Int
        get() = arrivalTotalMinutes % 60

    val minReturnTotalMinutes: Int
        get() {
            val rawMin = arrivalTotalMinutes + 30
            val minOfDay = rawMin % 1440
            return when {
                minOfDay < 4 * 60 -> (rawMin / 1440) * 1440 + 4 * 60
                else -> rawMin
            }
        }

    val minReturnHour: Int
        get() = (minReturnTotalMinutes / 60) % 24

    val minReturnMinute: Int
        get() = minReturnTotalMinutes % 60

    val rawReturnMinutes: Int
        get() = returnHour * 60 + returnMinute

    val effectiveReturnMinutes: Int
        get() = if (rawReturnMinutes >= outboundDepartureTotalMinutes) {
            rawReturnMinutes
        } else {
            rawReturnMinutes + 24 * 60
        }

    val layoverMinutes: Int
        get() = effectiveReturnMinutes - arrivalTotalMinutes

    val hasLayoverConflict: Boolean
        get() = isRoundTrip && (layoverMinutes < 30)

    val hasConflict: Boolean
        get() = hasOutboundConflict || (isRoundTrip && (hasReturnConflict || hasLayoverConflict))

    val candidateNeighborTerminalIds: Set<String>
        get() {
            val head = selectedPathNodes.lastOrNull() ?: return emptySet()
            val selectedIds = selectedPathNodes.map { it.id }.toSet()
            return allSegments
                .filter { it.fromTerminalId == head.id }
                .map { it.toTerminalId }
                .filter { it !in selectedIds }
                .toSet()
        }

    val canProceedFromStep1: Boolean
        get() = routeCategory == RouteCategory.AKDP &&
                (((selectedOrigin != null && selectedDestination != null && selectedOrigin.id != selectedDestination.id && builtRoute != null) || (selectedPathNodes.size >= 2 && builtRoute != null)))

    val canProceedFromStep2: Boolean
        get() = builtRoute != null

    val canProceedFromStep3: Boolean
        get() = pricings.isNotEmpty()

    val canProceedFromStep4: Boolean
        get() = selectedDepartureSlot != null && selectedDepartureSlot.isAvailable && dailySchedulePlan != null

    val isSelectedBusAvailable: Boolean
        get() {
            val bus = selectedBus ?: return false
            return !bus.isOperating && !bus.hasRoute && !bus.hasDriver
        }

    val isSelectedDriverAvailable: Boolean
        get() {
            val driver = selectedDriver ?: return false
            val assignedCode = driverAssignedBusCodes[driver.id]
            return assignedCode == null && !driver.isAssigned
        }

    val canLaunchFromStep4: Boolean
        get() = selectedBus != null && isSelectedBusAvailable && selectedDriver != null && isSelectedDriverAvailable && !hasConflict && !isSaving

    val canLaunchFromStep5: Boolean
        get() = canLaunchFromStep4 && canProceedFromStep4

    val canSaveStandby: Boolean
        get() = canProceedFromStep1 && canProceedFromStep2 && canProceedFromStep3 && !isSaving
}
