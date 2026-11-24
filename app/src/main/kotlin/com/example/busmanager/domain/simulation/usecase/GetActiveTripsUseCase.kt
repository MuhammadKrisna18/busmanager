package com.example.busmanager.domain.simulation.usecase

import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.repository.SimulationTripRepository
import kotlinx.coroutines.flow.StateFlow

class GetActiveTripsUseCase(
    private val simulationTripRepository: SimulationTripRepository
) {
    operator fun invoke(): StateFlow<List<ActiveBusTrip>> {
        return simulationTripRepository.activeTrips
    }
}
