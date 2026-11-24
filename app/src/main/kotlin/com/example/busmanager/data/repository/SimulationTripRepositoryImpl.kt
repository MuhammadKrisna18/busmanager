package com.example.busmanager.data.repository

import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.model.BusMovementInterpolator
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.domain.simulation.repository.SimulationTripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class SimulationTripRepositoryImpl : SimulationTripRepository {

    private val _activeTrips = MutableStateFlow<List<ActiveBusTrip>>(emptyList())
    override val activeTrips: StateFlow<List<ActiveBusTrip>> = _activeTrips.asStateFlow()

    override fun dispatchBus(
        bus: Bus,
        route: BusRoute,
        direction: RouteDirection,
        currentTime: GameCalendar
    ): ActiveBusTrip {
        val passengerLoadFraction = 0.85f
        val passengerCount = (bus.body.passengerCapacity * passengerLoadFraction).toInt().coerceAtLeast(1)

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
            isCompleted = false
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
                    val flippedDirection = when (trip.direction) {
                        RouteDirection.ORIGIN_TO_DESTINATION -> RouteDirection.DESTINATION_TO_ORIGIN
                        RouteDirection.DESTINATION_TO_ORIGIN -> RouteDirection.ORIGIN_TO_DESTINATION
                    }
                    val resetCoord = BusMovementInterpolator.interpolatePosition(
                        waypoints = trip.route.allWaypoints,
                        fraction = 0.0f,
                        direction = flippedDirection
                    )
                    trip.copy(
                        direction = flippedDirection,
                        elapsedMinutes = 0,
                        progressFraction = 0.0f,
                        currentCoordinate = resetCoord
                    )
                } else {
                    val newCoord = BusMovementInterpolator.interpolatePosition(
                        waypoints = trip.route.allWaypoints,
                        fraction = newProgress,
                        direction = trip.direction
                    )
                    trip.copy(
                        elapsedMinutes = newElapsed,
                        progressFraction = newProgress,
                        currentCoordinate = newCoord
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
}
