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
    val departureHour: Int = 7,
    val departureMinute: Int = 0,
    val direction: RouteDirection = RouteDirection.ORIGIN_TO_DESTINATION,
    val hasConflict: Boolean = false,

    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val launchSuccess: Boolean = false,
    val launchedRouteId: String? = null
) {
    val canProceedFromStep1: Boolean
        get() = selectedOrigin != null && selectedDestination != null && selectedOrigin.id != selectedDestination.id

    val canProceedFromStep2: Boolean
        get() = builtRoute != null

    val canProceedFromStep3: Boolean
        get() = pricings.isNotEmpty()

    val canLaunchFromStep4: Boolean
        get() = selectedBus != null && selectedDriver != null && !hasConflict && !isSaving
}
