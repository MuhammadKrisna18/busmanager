package com.example.busmanager.domain.simulation.repository

import com.example.busmanager.domain.company.model.Money
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

    fun dispatchBus(
        bus: Bus,
        route: BusRoute,
        direction: RouteDirection,
        currentTime: GameCalendar,
        headwayMinutes: Int,
        transitDwellMinutes: Int,
        ticketPricePerPassenger: Money? = null
    ): ActiveBusTrip {
        return dispatchBus(bus, route, direction, currentTime)
    }

    fun advanceTripsTime(advanceMinutes: Int): List<ActiveBusTrip>

    fun cancelTrip(tripId: String)

    fun clearTrips()

    fun markCancelNextSchedule(tripId: String, cancel: Boolean)

    fun completeTrip(tripId: String)
}
