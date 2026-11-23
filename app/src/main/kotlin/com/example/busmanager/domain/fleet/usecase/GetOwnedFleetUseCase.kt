package com.example.busmanager.domain.fleet.usecase

import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.FleetRepository
import kotlinx.coroutines.flow.Flow

class GetOwnedFleetUseCase(
    private val fleetRepository: FleetRepository
) {
    operator fun invoke(): Flow<List<Bus>> = fleetRepository.getOwnedBuses()
}
