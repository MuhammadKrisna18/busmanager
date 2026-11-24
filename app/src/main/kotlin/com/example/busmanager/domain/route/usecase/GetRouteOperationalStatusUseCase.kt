package com.example.busmanager.domain.route.usecase

import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.model.RouteOperationalInfo
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.simulation.repository.SimulationTripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

class GetRouteOperationalStatusUseCase(
    private val routeRepository: RouteRepository,
    private val routeScheduleRepository: RouteScheduleRepository,
    private val routePricingRepository: RoutePricingRepository,
    private val fleetRepository: FleetRepository,
    private val driverRepository: DriverRepository,
    private val simulationTripRepository: SimulationTripRepository? = null
) {

    operator fun invoke(): Flow<List<RouteOperationalInfo>> {
        val tripsFlow = simulationTripRepository?.activeTrips ?: flowOf(emptyList())

        return combine(
            fleetRepository.getOwnedBuses(),
            driverRepository.getAllDrivers(),
            routeScheduleRepository.getAllSchedules(),
            routePricingRepository.getAllPricingsFlow(),
            tripsFlow
        ) { buses, drivers, schedules, pricings, trips ->
            val allRoutes = routeRepository.getAllRoutes()
            val openedRouteIds = (
                pricings.map { it.routeId } +
                    schedules.map { it.routeId } +
                    buses.mapNotNull { it.routeId } +
                    trips.map { it.route.id }
                ).toSet()

            allRoutes.filter { openedRouteIds.contains(it.id) }.map { route ->
                val scheduledBusIds = schedules
                    .filter { it.routeId == route.id }
                    .mapNotNull { it.assignedBusId }
                    .toSet()

                val assignedBuses = buses.filter {
                    it.routeId == route.id || scheduledBusIds.contains(it.id)
                }

                val assignedDrivers = drivers.filter { driver ->
                    driver.isHired && assignedBuses.any { bus ->
                        bus.driverId == driver.id || driver.assignedBusId == bus.id
                    }
                }

                val routeSchedules = schedules.filter { it.routeId == route.id }
                val departureSchedules = routeSchedules.map {
                    val hour = String.format("%02d", it.departureHour)
                    val minute = String.format("%02d", it.departureMinute)
                    "$hour:$minute WIB (${it.direction.displayName})"
                }

                val routeTrips = trips.filter { it.route.id == route.id }
                val isActive = assignedBuses.isNotEmpty() &&
                    assignedDrivers.isNotEmpty() &&
                    (routeSchedules.isNotEmpty() || routeTrips.isNotEmpty())

                RouteOperationalInfo(
                    route = route,
                    isActive = isActive,
                    assignedBuses = assignedBuses,
                    assignedDrivers = assignedDrivers,
                    departureSchedules = departureSchedules,
                    activeTripsCount = routeTrips.size
                )
            }.sortedWith(
                compareByDescending<RouteOperationalInfo> { it.isActive }
                    .thenBy { it.route.name }
            )
        }
    }
}
