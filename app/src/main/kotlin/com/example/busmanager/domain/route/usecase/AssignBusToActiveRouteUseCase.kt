package com.example.busmanager.domain.route.usecase

import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.model.DailySchedulePlan
import com.example.busmanager.domain.route.model.DepartureSlotCalculator
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
        transitDwellMinutes: Int = 10,
        dailySchedulePlan: DailySchedulePlan? = null
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
            departureMinute = departureMinute,
            minHeadwayBufferMinutes = DepartureSlotCalculator.SLOT_INTERVAL_MINUTES
        )

        if (conflictCheck is ScheduleConflictResult.Conflict) {
            return Result.failure(IllegalStateException(conflictCheck.errorMessage))
        }

        val departureTerminalId = if (direction == RouteDirection.ORIGIN_TO_DESTINATION) {
            route.originTerminal.id
        } else {
            route.destinationTerminal.id
        }

        routeScheduleRepository.deleteSchedulesForBus(busId)

        val baseTime = System.currentTimeMillis()
        if (dailySchedulePlan != null && dailySchedulePlan.legs.isNotEmpty()) {
            val insertedScheduleIds = mutableListOf<String>()
            for ((index, leg) in dailySchedulePlan.legs.withIndex()) {
                val scheduleId = "sched-${route.id}-$busId-$index-${baseTime + index}"
                val schedule = RouteSchedule(
                    id = scheduleId,
                    routeId = route.id,
                    departureHour = leg.departureHour,
                    departureMinute = leg.departureMinute,
                    direction = leg.direction,
                    assignedBusId = busId,
                    headwayMinutes = headwayMinutes,
                    transitDwellMinutes = transitDwellMinutes
                )
                val addResult = routeScheduleRepository.addSchedule(schedule)
                if (addResult.isFailure) {
                    insertedScheduleIds.forEach { routeScheduleRepository.deleteSchedule(it) }
                    val legCity = if (leg.direction == RouteDirection.ORIGIN_TO_DESTINATION) route.destinationTerminal.city else route.originTerminal.city
                    val legType = if (index % 2 == 0) "Pergi" else "Pulang"
                    val legNumber = (index / 2) + 1
                    val customMsg = if (index > 0) {
                        "Bentrok jadwal Rit $legNumber $legType arah $legCity pada jam ${leg.departureFormatted} WIB: slot sudah terisi armada lain."
                    } else {
                        addResult.exceptionOrNull()?.message ?: "Gagal menambahkan jadwal keberangkatan"
                    }
                    return Result.failure(IllegalStateException(customMsg))
                }
                insertedScheduleIds.add(scheduleId)
            }
        } else {
            val scheduleId = "sched-${route.id}-$busId-$baseTime"
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
            val addResult = routeScheduleRepository.addSchedule(schedule)
            if (addResult.isFailure) {
                return Result.failure(addResult.exceptionOrNull() ?: IllegalStateException("Gagal menambahkan jadwal keberangkatan"))
            }
        }

        driverRepository.assignDriverToBus(driverId, busId)

        val updatedBus = bus.copy(
            routeId = route.id,
            driverId = driverId,
            currentTerminalId = departureTerminalId
        )
        fleetRepository.updateBus(updatedBus)

        return Result.success(Unit)
    }
}
