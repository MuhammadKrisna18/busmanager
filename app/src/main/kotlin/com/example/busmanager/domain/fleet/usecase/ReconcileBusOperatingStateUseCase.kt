package com.example.busmanager.domain.fleet.usecase

import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.simulation.repository.SimulationTripRepository
import kotlinx.coroutines.flow.first

class ReconcileBusOperatingStateUseCase(
    private val fleetRepository: FleetRepository,
    private val simulationTripRepository: SimulationTripRepository,
    private val routeRepository: RouteRepository? = null
) {
    suspend operator fun invoke(): List<Bus> {
        val buses = fleetRepository.getOwnedBuses().first()
        val activeTrips = simulationTripRepository.activeTrips.value
        val activeBusIds = activeTrips.map { it.busId }.toSet()

        val reconciledBuses = mutableListOf<Bus>()

        for (bus in buses) {
            val isCurrentlyOnRoad = activeBusIds.contains(bus.id)
            if (bus.isOperating && !isCurrentlyOnRoad) {
                val fallbackTerminalId = bus.currentTerminalId
                    ?: bus.routeId?.let { rId -> routeRepository?.getRouteById(rId)?.originTerminal?.id }
                    ?: "term_purabaya"

                val reconciled = bus.copy(
                    isOperating = false,
                    currentTerminalId = fallbackTerminalId
                )
                fleetRepository.updateBus(reconciled)
                reconciledBuses.add(reconciled)
            } else if (!bus.isOperating && isCurrentlyOnRoad) {
                val reconciled = bus.copy(isOperating = true)
                fleetRepository.updateBus(reconciled)
                reconciledBuses.add(reconciled)
            }
        }

        return reconciledBuses
    }
}
