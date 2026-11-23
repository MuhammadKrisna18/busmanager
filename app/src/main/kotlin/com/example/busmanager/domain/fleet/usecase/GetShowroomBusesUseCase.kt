package com.example.busmanager.domain.fleet.usecase

import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.repository.ShowroomCatalogRepository
import kotlinx.coroutines.flow.Flow

class GetShowroomBusesUseCase(
    private val showroomRepository: ShowroomCatalogRepository
) {
    operator fun invoke(): Flow<List<Bus>> = showroomRepository.getReadyStockBuses()
}
