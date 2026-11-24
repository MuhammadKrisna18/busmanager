package com.example.busmanager.domain.simulation.usecase

import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.domain.simulation.repository.SimulationTripRepository

class DispatchBusUseCase(
    private val simulationTripRepository: SimulationTripRepository
) {
    operator fun invoke(
        bus: Bus,
        route: BusRoute,
        direction: RouteDirection = RouteDirection.ORIGIN_TO_DESTINATION,
        currentTime: GameCalendar = GameCalendar.initial()
    ): ActiveBusTrip {
        return simulationTripRepository.dispatchBus(
            bus = bus,
            route = route,
            direction = direction,
            currentTime = currentTime
        )
    }
}
