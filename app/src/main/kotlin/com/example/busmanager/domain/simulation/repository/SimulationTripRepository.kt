package com.example.busmanager.domain.simulation.repository

import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.RouteDirection
import kotlinx.coroutines.flow.StateFlow

interface SimulationTripRepository {
    val activeTrips: StateFlow<List<ActiveBusTrip>>

    fun dispatchBus(
        bus: Bus,
        route: BusRoute,
        direction: RouteDirection,
        currentTime: GameCalendar
    ): ActiveBusTrip

    fun advanceTripsTime(advanceMinutes: Int): List<ActiveBusTrip>

    fun cancelTrip(tripId: String)

    fun clearTrips()
}
