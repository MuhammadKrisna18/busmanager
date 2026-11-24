package com.example.busmanager.presentation.route.wizard

import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.simulation.model.RouteDirection

enum class RouteWizardStep(val stepNumber: Int, val title: String) {
    ORIGIN_DESTINATION(1, "Asal & Tujuan"),
    PATH_AND_STOPS(2, "Jalur & Halte"),
    PRICING_MATRIX(3, "Tarif Tiket"),
    BUS_DRIVER_SCHEDULE(4, "Bus & Kru")
}

data class RouteWizardUiState(
    val currentStep: RouteWizardStep = RouteWizardStep.ORIGIN_DESTINATION,
    val allTerminals: List<Terminal> = emptyList(),
    val selectedOrigin: Terminal? = null,
    val selectedDestination: Terminal? = null,

    val availablePaths: List<List<Terminal>> = emptyList(),
    val selectedPathIndex: Int = 0,
    val activeTransitIds: Set<String> = emptySet(),
    val segmentPathTypes: Map<Pair<String, String>, RoutePathType> = emptyMap(),
    val builtRoute: BusRoute? = null,

    val pricings: List<RoutePricing> = emptyList(),
    val editingPricing: RoutePricing? = null,

    val availableBuses: List<Bus> = emptyList(),
    val selectedBus: Bus? = null,
    val availableDrivers: List<Driver> = emptyList(),
    val selectedDriver: Driver? = null,
    val driverAssignedBusCodes: Map<String, String> = emptyMap(),
    val departureHour: Int = 7,
    val departureMinute: Int = 0,
    val isRoundTrip: Boolean = true,
    val returnHour: Int = 10,
    val returnMinute: Int = 0,
    val direction: RouteDirection = RouteDirection.ORIGIN_TO_DESTINATION,
    val hasOutboundConflict: Boolean = false,
    val hasReturnConflict: Boolean = false,

    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val launchSuccess: Boolean = false,
    val launchedRouteId: String? = null
) {
    val returnDirection: RouteDirection
        get() = if (direction == RouteDirection.ORIGIN_TO_DESTINATION) {
            RouteDirection.DESTINATION_TO_ORIGIN
        } else {
            RouteDirection.ORIGIN_TO_DESTINATION
        }

    val estimatedDurationMinutes: Int
        get() = builtRoute?.totalEstimatedDurationMinutes ?: 0

    val outboundDepartureTotalMinutes: Int
        get() = departureHour * 60 + departureMinute

    val arrivalTotalMinutes: Int
        get() = outboundDepartureTotalMinutes + estimatedDurationMinutes

    val arrivalHour: Int
        get() = (arrivalTotalMinutes / 60) % 24

    val arrivalMinute: Int
        get() = arrivalTotalMinutes % 60

    val minReturnTotalMinutes: Int
        get() = arrivalTotalMinutes + 60

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
        get() = isRoundTrip && (layoverMinutes < 60)

    val hasConflict: Boolean
        get() = hasOutboundConflict || (isRoundTrip && (hasReturnConflict || hasLayoverConflict))

    val canProceedFromStep1: Boolean
        get() = selectedOrigin != null && selectedDestination != null && selectedOrigin.id != selectedDestination.id

    val canProceedFromStep2: Boolean
        get() = builtRoute != null

    val canProceedFromStep3: Boolean
        get() = pricings.isNotEmpty()

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
}
