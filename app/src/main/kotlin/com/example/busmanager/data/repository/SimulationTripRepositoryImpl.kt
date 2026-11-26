package com.example.busmanager.data.repository

import com.example.busmanager.data.local.dao.ActiveTripDao
import com.example.busmanager.data.mapper.toDomain
import com.example.busmanager.data.mapper.toEntity
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.model.BusMovementInterpolator
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.PassengerDemandGenerator
import com.example.busmanager.domain.simulation.model.PassengerQueueManager
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.domain.simulation.model.TransitExchangeInfo
import com.example.busmanager.domain.simulation.repository.SimulationTripRepository
import com.example.busmanager.domain.simulation.usecase.RecordSimulationLogUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class SimulationTripRepositoryImpl(
    override val passengerQueueManager: PassengerQueueManager = PassengerQueueManager(),
    private val passengerDemandGenerator: PassengerDemandGenerator = PassengerDemandGenerator(),
    private val activeTripDao: ActiveTripDao? = null,
    private val routeRepository: RouteRepository? = null,
    private val recordSimulationLogUseCase: RecordSimulationLogUseCase? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : SimulationTripRepository {

    private val _activeTrips = MutableStateFlow<List<ActiveBusTrip>>(emptyList())
    override val activeTrips: StateFlow<List<ActiveBusTrip>> = _activeTrips.asStateFlow()

    init {
        if (activeTripDao != null && routeRepository != null) {
            try {
                val entities = activeTripDao.getAllTripsSync()
                val loadedTrips = entities.mapNotNull { it.toDomain(routeRepository) }
                if (loadedTrips.isNotEmpty()) {
                    _activeTrips.value = loadedTrips
                }
            } catch (_: Exception) {}
        }
    }

    override fun dispatchBus(
        bus: Bus,
        route: BusRoute,
        direction: RouteDirection,
        currentTime: GameCalendar
    ): ActiveBusTrip = dispatchBus(bus, route, direction, currentTime, 60, 10)

    override fun dispatchBus(
        bus: Bus,
        route: BusRoute,
        direction: RouteDirection,
        currentTime: GameCalendar,
        headwayMinutes: Int,
        transitDwellMinutes: Int,
        ticketPricePerPassenger: Money?
    ): ActiveBusTrip {
        val capacity = bus.body.passengerCapacity
        val originTerm = if (direction == RouteDirection.ORIGIN_TO_DESTINATION) {
            route.originTerminal
        } else {
            route.destinationTerminal
        }
        val passengerCount = passengerQueueManager.boardPassengers(
            terminalId = originTerm.id,
            routeId = route.id,
            availableSeats = capacity
        )

        val ticketEarnings = if (ticketPricePerPassenger != null && ticketPricePerPassenger.amount > 0L) {
            Money.rupiah(ticketPricePerPassenger.amount * passengerCount)
        } else {
            val farePerKm = 650L
            val earningsAmount = (farePerKm * route.totalDistanceKm.toLong() * passengerCount)
            Money.rupiah(earningsAmount)
        }

        val dwellTotal = route.transitTerminals.size * transitDwellMinutes
        val totalEstimatedArrivalMinutes = (route.totalEstimatedDurationMinutes + dwellTotal).coerceAtLeast(30)

        val initialCoord = BusMovementInterpolator.interpolatePosition(
            waypoints = route.allWaypoints,
            fraction = 0.0f,
            direction = direction
        )

        val newTrip = ActiveBusTrip(
            id = "trip-${UUID.randomUUID().toString().take(8)}",
            busId = bus.id,
            busCode = bus.busCode,
            route = route,
            direction = direction,
            departureTime = currentTime,
            estimatedArrivalMinutes = totalEstimatedArrivalMinutes,
            elapsedMinutes = 0,
            progressFraction = 0.0f,
            currentCoordinate = initialCoord,
            passengerCount = passengerCount,
            ticketEarnings = ticketEarnings,
            isCompleted = false,
            maxCapacity = capacity,
            visitedTransitStopIndices = emptySet(),
            totalBoardedPassengers = passengerCount,
            totalAlightedPassengers = 0,
            isDwellingAtTransit = false,
            dwellingTerminalName = null,
            headwayMinutes = headwayMinutes,
            transitDwellMinutes = transitDwellMinutes,
            ticketPricePerPassenger = ticketPricePerPassenger
        )

        _activeTrips.update { current ->
            current.filterNot { it.busId == bus.id } + newTrip
        }

        if (activeTripDao != null) {
            scope.launch {
                activeTripDao.insertTrip(newTrip.toEntity())
            }
        }

        if (recordSimulationLogUseCase != null) {
            val destTerm = if (direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                route.destinationTerminal
            } else {
                route.originTerminal
            }
            val dateText = "${currentTime.dayOfWeekName()}, ${currentTime.formatDate()}"
            val timeText = currentTime.formatTime()
            scope.launch {
                recordSimulationLogUseCase.recordDispatch(
                    busCode = bus.busCode,
                    routeName = route.name,
                    destTerminalName = destTerm.name,
                    passengerCount = passengerCount,
                    timeText = timeText,
                    dateText = dateText,
                    busId = bus.id,
                    routeId = route.id
                )
            }
        }

        return newTrip
    }

    override fun advanceTripsTime(advanceMinutes: Int): List<ActiveBusTrip> {
        return advanceTripsTime(advanceMinutes, null)
    }

    override fun advanceTripsTime(advanceMinutes: Int, currentTime: GameCalendar?): List<ActiveBusTrip> {
        val currentTrips = _activeTrips.value
        val activeRoutes = currentTrips.map { it.route }.distinctBy { it.id }
        if (currentTime != null && activeRoutes.isNotEmpty()) {
            passengerQueueManager.advanceTime(advanceMinutes, currentTime, activeRoutes)
        }

        _activeTrips.update { currentList ->
            currentList.map { trip ->
                val newElapsed = trip.elapsedMinutes + advanceMinutes
                val newProgress = (newElapsed.toFloat() / trip.estimatedArrivalMinutes).coerceIn(0.0f, 1.0f)

                if (newProgress >= 1.0f) {
                    val finalCoord = BusMovementInterpolator.interpolatePosition(
                        waypoints = trip.route.allWaypoints,
                        fraction = 1.0f,
                        direction = trip.direction
                    )
                    trip.copy(
                        elapsedMinutes = trip.estimatedArrivalMinutes,
                        progressFraction = 1.0f,
                        currentCoordinate = finalCoord,
                        isCompleted = true,
                        totalAlightedPassengers = trip.passengerCount,
                        isDwellingAtTransit = false,
                        dwellingTerminalName = null
                    )
                } else {
                    val segmentsForDirection = if (trip.direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                        trip.route.segments
                    } else {
                        trip.route.segments.reversed().map { seg ->
                            seg.copy(waypoints = seg.waypoints.reversed())
                        }
                    }
                    val transitTerminalsForDirection = if (trip.direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                        trip.route.transitTerminals
                    } else {
                        trip.route.transitTerminals.reversed()
                    }

                    var cumulativeMinutes = 0
                    var computedCoord = trip.currentCoordinate
                    var isDwelling = false
                    var dwellingName: String? = null

                    var currentPassengers = trip.passengerCount
                    var currentEarnings = trip.ticketEarnings
                    var totalBoarded = trip.totalBoardedPassengers
                    var totalAlighted = trip.totalAlightedPassengers
                    val newlyVisitedStops = trip.visitedTransitStopIndices.toMutableSet()
                    val newTransitExchanges = trip.transitExchanges.toMutableList()

                    for (i in segmentsForDirection.indices) {
                        val seg = segmentsForDirection[i]
                        val travelDuration = seg.baseDurationMinutes
                        val travelEnd = cumulativeMinutes + travelDuration

                        if (newElapsed < travelEnd) {
                            val fracInSeg = ((newElapsed - cumulativeMinutes).toFloat() / travelDuration.coerceAtLeast(1)).coerceIn(0.0f, 1.0f)
                            computedCoord = BusMovementInterpolator.interpolatePosition(
                                waypoints = seg.waypoints,
                                fraction = fracInSeg,
                                direction = RouteDirection.ORIGIN_TO_DESTINATION
                            )
                            isDwelling = false
                            dwellingName = null
                            break
                        }

                        cumulativeMinutes = travelEnd

                        if (i < segmentsForDirection.size - 1) {
                            val dwellDuration = trip.transitDwellMinutes
                            val dwellEnd = cumulativeMinutes + dwellDuration
                            val transitTerm = transitTerminalsForDirection.getOrNull(i)

                            if (!newlyVisitedStops.contains(i)) {
                                newlyVisitedStops.add(i)
                                val alighted = passengerQueueManager.alightPassengers(currentPassengers, isTerminalStop = false)
                                val remaining = (currentPassengers - alighted).coerceAtLeast(0)
                                val emptySeats = (trip.maxCapacity - remaining).coerceAtLeast(0)
                                val boarded = if (transitTerm != null) {
                                    passengerQueueManager.boardPassengers(
                                        terminalId = transitTerm.id,
                                        routeId = trip.route.id,
                                        availableSeats = emptySeats
                                    )
                                } else {
                                    0
                                }
                                currentPassengers = (remaining + boarded).coerceIn(0, trip.maxCapacity)
                                totalBoarded += boarded
                                totalAlighted += alighted

                                val remainingDistance = segmentsForDirection.drop(i + 1).sumOf { it.distanceKm }
                                val transitTicketAmount = if (trip.ticketPricePerPassenger != null && trip.route.totalDistanceKm > 0) {
                                    val frac = (remainingDistance / trip.route.totalDistanceKm).coerceIn(0.1, 1.0)
                                    (trip.ticketPricePerPassenger.amount * frac * boarded).toLong().coerceAtLeast(0L)
                                } else {
                                    (650L * remainingDistance.toLong() * boarded).coerceAtLeast(0L)
                                }
                                currentEarnings += Money.rupiah(transitTicketAmount)

                                if (transitTerm != null) {
                                    newTransitExchanges.add(
                                        TransitExchangeInfo(
                                            stopIndex = i,
                                            terminalId = transitTerm.id,
                                            terminalName = transitTerm.name,
                                            boardedCount = boarded,
                                            alightedCount = alighted
                                        )
                                    )
                                    if (recordSimulationLogUseCase != null && currentTime != null) {
                                        val dateText = "${currentTime.dayOfWeekName()}, ${currentTime.formatDate()}"
                                        val timeText = currentTime.formatTime()
                                        scope.launch {
                                            recordSimulationLogUseCase.recordTransit(
                                                busCode = trip.busCode,
                                                routeName = trip.route.name,
                                                transitTerminalName = transitTerm.name,
                                                boardedCount = boarded,
                                                alightedCount = alighted,
                                                timeText = timeText,
                                                dateText = dateText,
                                                busId = trip.busId,
                                                routeId = trip.route.id
                                            )
                                        }
                                    }
                                }
                            }

                            if (newElapsed < dwellEnd) {
                                computedCoord = seg.waypoints.lastOrNull() ?: trip.currentCoordinate
                                isDwelling = true
                                dwellingName = transitTerm?.name ?: "Terminal Transit"
                                break
                            }

                            cumulativeMinutes = dwellEnd
                        }
                    }

                    trip.copy(
                        elapsedMinutes = newElapsed,
                        progressFraction = newProgress,
                        currentCoordinate = computedCoord,
                        passengerCount = currentPassengers,
                        ticketEarnings = currentEarnings,
                        visitedTransitStopIndices = newlyVisitedStops,
                        totalBoardedPassengers = totalBoarded,
                        totalAlightedPassengers = totalAlighted,
                        isDwellingAtTransit = isDwelling,
                        dwellingTerminalName = dwellingName,
                        transitExchanges = newTransitExchanges
                    )
                }
            }
        }

        if (activeTripDao != null && _activeTrips.value.isNotEmpty()) {
            val entities = _activeTrips.value.map { it.toEntity() }
            scope.launch {
                activeTripDao.insertTrips(entities)
            }
        }

        return _activeTrips.value
    }

    override fun cancelTrip(tripId: String) {
        _activeTrips.update { it.filterNot { trip -> trip.id == tripId } }
        if (activeTripDao != null) {
            scope.launch {
                activeTripDao.deleteTrip(tripId)
            }
        }
    }

    override fun clearTrips() {
        _activeTrips.value = emptyList()
        if (activeTripDao != null) {
            scope.launch {
                activeTripDao.deleteAllTrips()
            }
        }
    }

    override fun markCancelNextSchedule(tripId: String, cancel: Boolean) {
        _activeTrips.update { list ->
            list.map { if (it.id == tripId) it.copy(cancelNextSchedule = cancel) else it }
        }
        if (activeTripDao != null) {
            val target = _activeTrips.value.find { it.id == tripId }
            if (target != null) {
                scope.launch {
                    activeTripDao.updateTrip(target.toEntity())
                }
            }
        }
    }

    override fun completeTrip(tripId: String) {
        _activeTrips.update { list ->
            list.filterNot { it.id == tripId }
        }
        if (activeTripDao != null) {
            scope.launch {
                activeTripDao.deleteTrip(tripId)
            }
        }
    }
}
