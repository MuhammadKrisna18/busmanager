package com.example.busmanager.domain.route.usecase

import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePricing
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.repository.RoutePricingRepository
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.simulation.model.RouteDirection

data class LaunchCustomRouteParams(
    val route: BusRoute,
    val pricings: List<RoutePricing>,
    val bus: Bus,
    val driver: Driver,
    val departureHour: Int,
    val departureMinute: Int,
    val direction: RouteDirection,
    val headwayMinutes: Int,
    val transitDwellMinutes: Int,
    val isRoundTrip: Boolean,
    val returnHour: Int = 0,
    val returnMinute: Int = 0,
    val returnDirection: RouteDirection = RouteDirection.DESTINATION_TO_ORIGIN
)

class LaunchCustomRouteUseCase(
    private val routeRepository: RouteRepository,
    private val routePricingRepository: RoutePricingRepository,
    private val driverRepository: DriverRepository,
    private val fleetRepository: FleetRepository,
    private val routeScheduleRepository: RouteScheduleRepository
) {
    suspend operator fun invoke(params: LaunchCustomRouteParams) {
        routeRepository.saveRoute(params.route)
        routePricingRepository.savePricings(params.pricings)
        driverRepository.assignDriverToBus(params.driver.id, params.bus.id)
        fleetRepository.updateBus(
            params.bus.copy(
                routeId = params.route.id,
                driverId = params.driver.id,
                currentTerminalId = params.bus.currentTerminalId ?: params.route.originTerminal.id
            )
        )

        val baseTime = System.currentTimeMillis()
        val outboundScheduleId = "sched-${params.route.id}-outbound-$baseTime"
        val outboundSchedule = RouteSchedule(
            id = outboundScheduleId,
            routeId = params.route.id,
            departureHour = params.departureHour,
            departureMinute = params.departureMinute,
            direction = params.direction,
            assignedBusId = params.bus.id,
            headwayMinutes = params.headwayMinutes,
            transitDwellMinutes = params.transitDwellMinutes
        )
        routeScheduleRepository.addSchedule(outboundSchedule)
        routeScheduleRepository.assignBusToSchedule(outboundScheduleId, params.bus.id)

        if (params.isRoundTrip) {
            val returnScheduleId = "sched-${params.route.id}-return-${baseTime + 1}"
            val returnSchedule = RouteSchedule(
                id = returnScheduleId,
                routeId = params.route.id,
                departureHour = params.returnHour,
                departureMinute = params.returnMinute,
                direction = params.returnDirection,
                assignedBusId = params.bus.id,
                headwayMinutes = params.headwayMinutes,
                transitDwellMinutes = params.transitDwellMinutes
            )
            routeScheduleRepository.addSchedule(returnSchedule)
            routeScheduleRepository.assignBusToSchedule(returnScheduleId, params.bus.id)
        }
    }
}
