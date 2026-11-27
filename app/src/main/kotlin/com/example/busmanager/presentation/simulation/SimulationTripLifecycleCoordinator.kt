package com.example.busmanager.presentation.simulation

import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.model.DepartureSlotCalculator
import com.example.busmanager.domain.route.model.RouteCategory
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

                    val isAkap = RouteCategory.fromRoute(trip.route) == RouteCategory.AKAP
                    val minLayover = if (returnDir == RouteDirection.DESTINATION_TO_ORIGIN) {
                        if (isAkap) {
                            DepartureSlotCalculator.AKAP_DESTINATION_LAYOVER_MINUTES
                        } else {
                            DepartureSlotCalculator.AKDP_DESTINATION_LAYOVER_MINUTES
                        }
                    } else {
                        DepartureSlotCalculator.AKDP_ORIGIN_INTER_PP_LAYOVER_MINUTES
                    }

                    val currentTotalMinutes = currentTime.day * 1440L + currentTime.hour * 60L + currentTime.minute
                    val effectiveHeadway = trip.headwayMinutes.coerceAtLeast(minLayover)
                    val rawNextDeparture = currentTotalMinutes + effectiveHeadway
                    var resolvedMinutes = calculateNextOperatingDeparture(rawNextDeparture)
                    val (alignedH, alignedM) = DepartureSlotCalculator.alignToNext15MinuteSlot((resolvedMinutes % 1440L).toInt())
                    var resolvedHour = alignedH
                    var resolvedMin = alignedM
                    resolvedMinutes = (resolvedMinutes / 1440L) * 1440L + resolvedHour * 60L + resolvedMin

                    val effectiveDwell = trip.transitDwellMinutes.coerceIn(0, 15)

                    if (busSchedules.isNotEmpty()) {
                        val upcomingReturnSchedules = busSchedules
                            .filter { it.direction == returnDir }
                            .sortedBy { it.departureHour * 60 + it.departureMinute }

                        val returnSchedule = upcomingReturnSchedules.firstOrNull { sched ->
                            val schedEpochMinutes = currentTime.day * 1440L + sched.departureHour * 60L + sched.departureMinute
                            schedEpochMinutes >= rawNextDeparture
                        } ?: upcomingReturnSchedules.firstOrNull()

                        val targetScheduleId = returnSchedule?.id ?: busSchedules.first().id
                        if (returnSchedule != null) {
                            val scheduledEpochMinutes = currentTime.day * 1440L + returnSchedule.departureHour * 60L + returnSchedule.departureMinute
                            if (scheduledEpochMinutes >= rawNextDeparture) {
                                resolvedMinutes = scheduledEpochMinutes
                                resolvedHour = returnSchedule.departureHour
                                resolvedMin = returnSchedule.departureMinute
                            } else {
                                val nextDayEpoch = (currentTime.day + 1L) * 1440L + returnSchedule.departureHour * 60L + returnSchedule.departureMinute
                                resolvedMinutes = nextDayEpoch
                                resolvedHour = returnSchedule.departureHour
                                resolvedMin = returnSchedule.departureMinute
                            }
                        }

                        if (routeScheduleRepository != null) {
                            while (routeScheduleRepository.checkConflict(trip.route.id, returnDir, resolvedHour, resolvedMin, targetScheduleId)) {
                                resolvedMinutes += DepartureSlotCalculator.SLOT_INTERVAL_MINUTES
                                resolvedMinutes = calculateNextOperatingDeparture(resolvedMinutes)
                                val (adjH, adjM) = DepartureSlotCalculator.alignToNext15MinuteSlot((resolvedMinutes % 1440L).toInt())
                                resolvedHour = adjH
                                resolvedMin = adjM
                                resolvedMinutes = (resolvedMinutes / 1440L) * 1440L + resolvedHour * 60L + resolvedMin
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
