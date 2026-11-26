package com.example.busmanager.presentation.simulation

import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.domain.simulation.usecase.DispatchBusUseCase
import com.example.busmanager.domain.terminal.model.TerminalPassengerRecord
import com.example.busmanager.domain.terminal.repository.TerminalPassengerRepository

class SimulationTripLifecycleCoordinator(
    private val fleetRepository: FleetRepository?,
    private val routeScheduleRepository: RouteScheduleRepository?,
    private val driverRepository: DriverRepository?,
    private val terminalPassengerRepository: TerminalPassengerRepository?,
    private val dispatchBusUseCase: DispatchBusUseCase
) {
    private val recordedTransitExchangeKeys = mutableSetOf<String>()

    fun calculateNextOperatingDeparture(baseEpochMinutes: Long): Long {
        val minuteOfDay = (baseEpochMinutes % 1440L).toInt()
        return when {
            minuteOfDay < 4 * 60 -> {
                val dayEpoch = (baseEpochMinutes / 1440L) * 1440L
                dayEpoch + 4 * 60L
            }
            minuteOfDay > 23 * 60 + 59 -> {
                val nextDayEpoch = (baseEpochMinutes / 1440L + 1L) * 1440L
                nextDayEpoch + 4 * 60L
            }
            else -> baseEpochMinutes
        }
    }

    suspend fun handleCompletedTrips(
        completedTrips: List<ActiveBusTrip>,
        currentTime: GameCalendar,
        getBus: suspend (String) -> Bus?,
        getSchedules: suspend (busId: String, routeId: String) -> List<RouteSchedule>,
        onBusUpdated: (Bus) -> Unit,
        onScheduleUpdated: (RouteSchedule) -> Unit
    ) {
        for (trip in completedTrips) {
            val destTerminal = if (trip.direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                trip.route.destinationTerminal
            } else {
                trip.route.originTerminal
            }
            val returnDir = if (trip.direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                RouteDirection.DESTINATION_TO_ORIGIN
            } else {
                RouteDirection.ORIGIN_TO_DESTINATION
            }

            val dateText = "${currentTime.dayOfWeekName()}, ${currentTime.formatDate()}"
            val timeText = currentTime.formatTime()

            terminalPassengerRepository?.recordPassengerExchange(
                TerminalPassengerRecord(
                    id = "rec-term-arr-${trip.busId}-${System.nanoTime()}",
                    terminalId = destTerminal.id,
                    terminalName = destTerminal.name,
                    busId = trip.busId,
                    busCode = trip.busCode,
                    routeId = trip.route.id,
                    routeName = trip.route.name,
                    day = currentTime.day,
                    month = currentTime.month,
                    year = currentTime.year,
                    dateFormatted = dateText,
                    timeFormatted = timeText,
                    boardedCount = 0,
                    alightedCount = trip.passengerCount
                )
            )

            val bus = getBus(trip.busId)
            if (bus != null) {
                if (trip.cancelNextSchedule) {
                    val updatedBus = bus.copy(
                        isOperating = false,
                        routeId = null,
                        currentTerminalId = destTerminal.id
                    )
                    onBusUpdated(updatedBus)
                    fleetRepository?.updateBus(updatedBus)
                    routeScheduleRepository?.getSchedulesListForRoute(trip.route.id)
                        ?.filter { it.assignedBusId == bus.id }
                        ?.forEach { routeScheduleRepository.deleteSchedule(it.id) }
                } else {
                    val updatedBus = bus.copy(
                        isOperating = false,
                        currentTerminalId = destTerminal.id
                    )
                    onBusUpdated(updatedBus)
                    fleetRepository?.updateBus(updatedBus)

                    val busSchedules = getSchedules(bus.id, trip.route.id)

                    val currentTotalMinutes = currentTime.day * 1440L + currentTime.hour * 60L + currentTime.minute
                    val effectiveHeadway = trip.headwayMinutes.coerceAtLeast(30)
                    val effectiveDwell = trip.transitDwellMinutes.coerceIn(0, 15)
                    val rawNextDeparture = currentTotalMinutes + effectiveHeadway
                    var resolvedMinutes = calculateNextOperatingDeparture(rawNextDeparture)
                    var resolvedHour = (((resolvedMinutes) % 1440L) / 60L).toInt()
                    var resolvedMin = ((resolvedMinutes) % 60L).toInt()

                    if (busSchedules.isNotEmpty()) {
                        val returnSchedule = busSchedules.find { it.direction == returnDir }
                        val targetScheduleId = returnSchedule?.id ?: busSchedules.first().id
                        if (routeScheduleRepository != null) {
                            while (routeScheduleRepository.checkConflict(trip.route.id, returnDir, resolvedHour, resolvedMin, targetScheduleId)) {
                                resolvedMinutes += 10
                                resolvedMinutes = calculateNextOperatingDeparture(resolvedMinutes)
                                resolvedHour = (((resolvedMinutes) % 1440L) / 60L).toInt()
                                resolvedMin = ((resolvedMinutes) % 60L).toInt()
                            }
                        }

                        if (returnSchedule != null) {
                            val updatedReturnSchedule = returnSchedule.copy(
                                departureHour = resolvedHour,
                                departureMinute = resolvedMin,
                                direction = returnDir,
                                headwayMinutes = effectiveHeadway,
                                transitDwellMinutes = effectiveDwell,
                                nextDepartureEpochMinutes = resolvedMinutes
                            )
                            routeScheduleRepository?.updateSchedule(updatedReturnSchedule)

                            val otherSchedules = busSchedules.filter { it.id != returnSchedule.id }
                            otherSchedules.forEach { other ->
                                val nextOutboundRaw = resolvedMinutes + trip.estimatedArrivalMinutes + effectiveHeadway
                                val nextOutboundResolved = calculateNextOperatingDeparture(nextOutboundRaw)
                                val updatedOther = other.copy(
                                    departureHour = (((nextOutboundResolved) % 1440L) / 60L).toInt(),
                                    departureMinute = ((nextOutboundResolved) % 60L).toInt(),
                                    nextDepartureEpochMinutes = nextOutboundResolved
                                )
                                routeScheduleRepository?.updateSchedule(updatedOther)
                                onScheduleUpdated(updatedOther)
                            }
                            onScheduleUpdated(updatedReturnSchedule)
                        } else {
                            val activeSchedule = busSchedules.first()
                            val updatedSchedule = activeSchedule.copy(
                                departureHour = resolvedHour,
                                departureMinute = resolvedMin,
                                direction = returnDir,
                                headwayMinutes = effectiveHeadway,
                                transitDwellMinutes = effectiveDwell,
                                nextDepartureEpochMinutes = resolvedMinutes
                            )
                            routeScheduleRepository?.updateSchedule(updatedSchedule)
                            onScheduleUpdated(updatedSchedule)
                        }
                    }
                }
            }

            dispatchBusUseCase.completeTrip(trip.id)
            bus?.driverId?.let { driverId ->
                val repo = driverRepository ?: return@let
                val driver = repo.getDriverById(driverId)
                if (driver?.isPendingRelease == true) {
                    repo.unassignDriver(driverId)
                }
            }
        }
    }

    suspend fun handleTransitPassengerExchanges(
        trips: List<ActiveBusTrip>,
        currentTime: GameCalendar,
        currentBuses: List<Bus>
    ) {
        val passengerRepo = terminalPassengerRepository ?: return
        val dateText = "${currentTime.dayOfWeekName()}, ${currentTime.formatDate()}"
        val timeText = currentTime.formatTime()

        for (trip in trips) {
            if (trip.isDwellingAtTransit) {
                val assignedBus = currentBuses.find { it.id == trip.busId }
                assignedBus?.driverId?.let { driverId ->
                    val repo = driverRepository ?: return@let
                    val driver = repo.getDriverById(driverId)
                    if (driver?.isPendingRelease == true) {
                        repo.unassignDriver(driverId)
                    }
                }
            }
            for (exchange in trip.transitExchanges) {
                val exchangeKey = "${trip.id}-${exchange.terminalId}-${exchange.stopIndex}"
                if (!recordedTransitExchangeKeys.contains(exchangeKey)) {
                    recordedTransitExchangeKeys.add(exchangeKey)
                    passengerRepo.recordPassengerExchange(
                        TerminalPassengerRecord(
                            id = "rec-term-trans-${trip.busId}-${exchange.terminalId}-${System.nanoTime()}",
                            terminalId = exchange.terminalId,
                            terminalName = exchange.terminalName,
                            busId = trip.busId,
                            busCode = trip.busCode,
                            routeId = trip.route.id,
                            routeName = trip.route.name,
                            day = currentTime.day,
                            month = currentTime.month,
                            year = currentTime.year,
                            dateFormatted = dateText,
                            timeFormatted = timeText,
                            boardedCount = exchange.boardedCount,
                            alightedCount = exchange.alightedCount
                        )
                    )
                }
            }
        }
    }
}
