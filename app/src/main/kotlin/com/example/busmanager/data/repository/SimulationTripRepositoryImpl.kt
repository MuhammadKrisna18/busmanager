package com.example.busmanager.data.repository

import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.model.BusMovementInterpolator
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.PassengerDemandGenerator
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.domain.simulation.repository.SimulationTripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class SimulationTripRepositoryImpl(
    private val passengerDemandGenerator: PassengerDemandGenerator = PassengerDemandGenerator()
) : SimulationTripRepository {

    private val _activeTrips = MutableStateFlow<List<ActiveBusTrip>>(emptyList())
    override val activeTrips: StateFlow<List<ActiveBusTrip>> = _activeTrips.asStateFlow()

    override fun dispatchBus(
        bus: Bus,
        route: BusRoute,
        direction: RouteDirection,
        currentTime: GameCalendar
    ): ActiveBusTrip {
        val capacity = bus.body.passengerCapacity
        val passengerCount = passengerDemandGenerator.generateInitialLoad(capacity)

        val farePerKm = 650L
        val earningsAmount = (farePerKm * route.totalDistanceKm.toLong() * passengerCount)
        val ticketEarnings = Money.rupiah(earningsAmount)

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
            estimatedArrivalMinutes = route.totalEstimatedDurationMinutes.coerceAtLeast(30),
            elapsedMinutes = 0,
            progressFraction = 0.0f,
            currentCoordinate = initialCoord,
            passengerCount = passengerCount,
            ticketEarnings = ticketEarnings,
            isCompleted = false,
            maxCapacity = capacity,
            visitedTransitStopIndices = emptySet(),
            totalBoardedPassengers = passengerCount,
            totalAlightedPassengers = 0
        )

        _activeTrips.update { current ->
            current.filterNot { it.busId == bus.id } + newTrip
        }

        return newTrip
    }

    override fun advanceTripsTime(advanceMinutes: Int): List<ActiveBusTrip> {
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
                        totalAlightedPassengers = trip.passengerCount
                    )
                } else {
                    val newCoord = BusMovementInterpolator.interpolatePosition(
                        waypoints = trip.route.allWaypoints,
                        fraction = newProgress,
                        direction = trip.direction
                    )

                    val segmentsForDirection = if (trip.direction == RouteDirection.ORIGIN_TO_DESTINATION) {
                        trip.route.segments
                    } else {
                        trip.route.segments.reversed()
                    }

                    var currentPassengers = trip.passengerCount
                    var currentEarnings = trip.ticketEarnings
                    var totalBoarded = trip.totalBoardedPassengers
                    var totalAlighted = trip.totalAlightedPassengers
                    val newlyVisitedStops = trip.visitedTransitStopIndices.toMutableSet()

                    if (segmentsForDirection.size > 1) {
                        var cumulativeMinutes = 0
                        for (index in 0 until segmentsForDirection.size - 1) {
                            cumulativeMinutes += segmentsForDirection[index].baseDurationMinutes
                            if (newElapsed >= cumulativeMinutes && !newlyVisitedStops.contains(index)) {
                                newlyVisitedStops.add(index)
                                val alighted = passengerDemandGenerator.calculateTransitAlighting(currentPassengers)
                                val remaining = (currentPassengers - alighted).coerceAtLeast(0)
                                val emptySeats = (trip.maxCapacity - remaining).coerceAtLeast(0)
                                val boarded = passengerDemandGenerator.calculateTransitBoarding(emptySeats)
                                currentPassengers = (remaining + boarded).coerceIn(0, trip.maxCapacity)
                                totalBoarded += boarded
                                totalAlighted += alighted

                                val remainingDistance = segmentsForDirection.drop(index + 1).sumOf { it.distanceKm }
                                val transitTicketAmount = (650L * remainingDistance.toLong() * boarded).coerceAtLeast(0L)
                                currentEarnings += Money.rupiah(transitTicketAmount)
                            }
                        }
                    }

                    trip.copy(
                        elapsedMinutes = newElapsed,
                        progressFraction = newProgress,
                        currentCoordinate = newCoord,
                        passengerCount = currentPassengers,
                        ticketEarnings = currentEarnings,
                        visitedTransitStopIndices = newlyVisitedStops,
                        totalBoardedPassengers = totalBoarded,
                        totalAlightedPassengers = totalAlighted
                    )
                }
            }
        }
        return _activeTrips.value
    }

    override fun cancelTrip(tripId: String) {
        _activeTrips.update { it.filterNot { trip -> trip.id == tripId } }
    }

    override fun clearTrips() {
        _activeTrips.value = emptyList()
    }

    override fun markCancelNextSchedule(tripId: String, cancel: Boolean) {
        _activeTrips.update { list ->
            list.map { if (it.id == tripId) it.copy(cancelNextSchedule = cancel) else it }
        }
    }

    override fun completeTrip(tripId: String) {
        _activeTrips.update { list ->
            list.filterNot { it.id == tripId }
        }
    }
}
