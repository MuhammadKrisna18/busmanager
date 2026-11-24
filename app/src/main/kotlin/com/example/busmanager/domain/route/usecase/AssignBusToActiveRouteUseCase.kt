package com.example.busmanager.domain.route.usecase

import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.simulation.model.RouteDirection

class AssignBusToActiveRouteUseCase(
    private val fleetRepository: FleetRepository,
    private val routeRepository: RouteRepository,
    private val driverRepository: DriverRepository,
    private val routeScheduleRepository: RouteScheduleRepository,
    private val validateScheduleConflictUseCase: ValidateScheduleConflictUseCase
) {
    suspend operator fun invoke(
        busId: String,
        routeId: String,
        driverId: String,
        departureHour: Int,
        departureMinute: Int,
        direction: RouteDirection,
        headwayMinutes: Int = 60,
        transitDwellMinutes: Int = 10
    ): Result<Unit> {
        val bus = fleetRepository.getBusById(busId)
            ?: return Result.failure(IllegalArgumentException("Armada bus tidak ditemukan"))

        val route = routeRepository.getRouteById(routeId)
            ?: return Result.failure(IllegalArgumentException("Trayek tidak ditemukan"))

        val driver = driverRepository.getDriverById(driverId)
            ?: return Result.failure(IllegalArgumentException("Sopir tidak ditemukan"))

        if (driver.assignedBusId != null && driver.assignedBusId != busId) {
            return Result.failure(IllegalStateException("Sopir ${driver.name} sudah bertugas pada armada lain"))
        }

        val conflictCheck = validateScheduleConflictUseCase(
            route = route,
            direction = direction,
            departureHour = departureHour,
            departureMinute = departureMinute
        )

        if (conflictCheck is ScheduleConflictResult.Conflict) {
            return Result.failure(IllegalStateException(conflictCheck.errorMessage))
        }

        val terminalLocation = bus.currentTerminalId ?: if (direction == RouteDirection.ORIGIN_TO_DESTINATION) {
            route.originTerminal.id
        } else {
            route.destinationTerminal.id
        }

        driverRepository.assignDriverToBus(driverId, busId)

        val updatedBus = bus.copy(
            routeId = route.id,
            driverId = driverId,
            currentTerminalId = terminalLocation
        )
        fleetRepository.updateBus(updatedBus)

        val scheduleId = "sched-${route.id}-$busId-${System.currentTimeMillis()}"
        val schedule = RouteSchedule(
            id = scheduleId,
            routeId = route.id,
            departureHour = departureHour,
            departureMinute = departureMinute,
            direction = direction,
            assignedBusId = busId,
            headwayMinutes = headwayMinutes,
            transitDwellMinutes = transitDwellMinutes
        )

        return routeScheduleRepository.addSchedule(schedule)
    }
}
