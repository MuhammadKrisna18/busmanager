package com.example.busmanager.data

import com.example.busmanager.data.local.datasource.ChassisDataSource
import com.example.busmanager.data.local.datasource.KaroseriBodyDataSource
import com.example.busmanager.data.local.datasource.RouteDataSource
import com.example.busmanager.data.local.datasource.TerminalDataSource
import com.example.busmanager.data.repository.SimulationTripRepositoryImpl
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.RouteDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulationTripRepositoryTest {

    private val terminalDataSource = TerminalDataSource()
    private val routeDataSource = RouteDataSource(terminalDataSource)
    private val chassisDataSource = ChassisDataSource()
    private val bodyDataSource = KaroseriBodyDataSource()

    private val repository = SimulationTripRepositoryImpl()

    private fun createTestBus(): Bus {
        val chassis = chassisDataSource.getChassisList().first()
        val body = bodyDataSource.getKaroseriBodyList().first()
        return Bus(
            id = "bus-test-1",
            busCode = "TN-01",
            chassis = chassis,
            body = body
        )
    }

    @Test
    fun initialState_activeTripsEmpty() {
        assertTrue(repository.activeTrips.value.isEmpty())
    }

    @Test
    fun dispatchBus_createsActiveTripWithCalculatedEarnings() {
        val bus = createTestBus()
        val route = routeDataSource.getPredefinedRoutes().first()
        val calendar = GameCalendar.initial()

        val trip = repository.dispatchBus(
            bus = bus,
            route = route,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            currentTime = calendar
        )

        assertNotNull(trip)
        assertEquals(bus.id, trip.busId)
        assertEquals(bus.busCode, trip.busCode)
        assertEquals(route.id, trip.route.id)
        assertEquals(0.0f, trip.progressFraction, 0.001f)
        assertTrue(trip.passengerCount > 0)
        assertTrue(trip.ticketEarnings.amount > 0)
        assertEquals(1, repository.activeTrips.value.size)
    }

    @Test
    fun advanceTripsTime_advancesTripProgressAndFlipsOnCompletion() {
        val bus = createTestBus()
        val route = routeDataSource.getPredefinedRoutes().first()
        val calendar = GameCalendar.initial()

        repository.dispatchBus(
            bus = bus,
            route = route,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            currentTime = calendar
        )

        val duration = route.totalEstimatedDurationMinutes.coerceAtLeast(30)
        repository.advanceTripsTime(duration / 2)

        val halfTrip = repository.activeTrips.value.first()
        assertTrue(halfTrip.progressFraction in 0.4f..0.6f)

        repository.advanceTripsTime(duration)
        val returnTrip = repository.activeTrips.value.first()
        assertEquals(RouteDirection.DESTINATION_TO_ORIGIN, returnTrip.direction)
        assertEquals(0.0f, returnTrip.progressFraction, 0.001f)
    }

    @Test
    fun multiStopRoute_advancingPastTransit_exchangesPassengersAndIncreasesEarnings() {
        val bus = createTestBus()
        val multiStopRoute = routeDataSource.getPredefinedRoutes().first { it.segments.size > 1 }
        val calendar = GameCalendar.initial()

        val initialTrip = repository.dispatchBus(
            bus = bus,
            route = multiStopRoute,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            currentTime = calendar
        )

        assertTrue(initialTrip.passengerCount in 1..bus.body.passengerCapacity)
        assertEquals(0, initialTrip.visitedTransitStopIndices.size)

        val firstSegmentDuration = multiStopRoute.segments.first().baseDurationMinutes
        repository.advanceTripsTime(firstSegmentDuration + 1)

        val tripAtTransit = repository.activeTrips.value.first()
        assertTrue(tripAtTransit.visitedTransitStopIndices.contains(0))
        assertTrue(tripAtTransit.passengerCount in 0..bus.body.passengerCapacity)
        assertTrue(tripAtTransit.ticketEarnings.amount >= initialTrip.ticketEarnings.amount)
        assertTrue(tripAtTransit.totalBoardedPassengers >= initialTrip.passengerCount)
    }

    @Test
    fun cancelAndClearTrips_removesTrips() {
        val bus = createTestBus()
        val route = routeDataSource.getPredefinedRoutes().first()
        val trip = repository.dispatchBus(
            bus = bus,
            route = route,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            currentTime = GameCalendar.initial()
        )

        assertEquals(1, repository.activeTrips.value.size)
        repository.cancelTrip(trip.id)
        assertTrue(repository.activeTrips.value.isEmpty())

        repository.dispatchBus(
            bus = bus,
            route = route,
            direction = RouteDirection.ORIGIN_TO_DESTINATION,
            currentTime = GameCalendar.initial()
        )
        repository.clearTrips()
        assertTrue(repository.activeTrips.value.isEmpty())
    }
}
