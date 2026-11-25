package com.example.busmanager.domain.simulation.usecase

import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.domain.simulation.repository.SimulationTripRepository

class DispatchBusUseCase(
    private val simulationTripRepository: SimulationTripRepository
) {
    val passengerQueueManager: com.example.busmanager.domain.simulation.model.PassengerQueueManager
        get() = simulationTripRepository.passengerQueueManager

    operator fun invoke(
        bus: Bus,
        route: BusRoute,
        direction: RouteDirection = RouteDirection.ORIGIN_TO_DESTINATION,
        currentTime: GameCalendar = GameCalendar.initial(),
        headwayMinutes: Int = 60,
        transitDwellMinutes: Int = 10,
        ticketPricePerPassenger: Money? = null
    ): ActiveBusTrip {
        return simulationTripRepository.dispatchBus(
            bus = bus,
            route = route,
            direction = direction,
            currentTime = currentTime,
            headwayMinutes = headwayMinutes,
            transitDwellMinutes = transitDwellMinutes,
            ticketPricePerPassenger = ticketPricePerPassenger
        )
    }

    fun markCancelNextSchedule(tripId: String, cancel: Boolean) {
        simulationTripRepository.markCancelNextSchedule(tripId, cancel)
    }

    fun completeTrip(tripId: String) {
        simulationTripRepository.completeTrip(tripId)
    }
}
