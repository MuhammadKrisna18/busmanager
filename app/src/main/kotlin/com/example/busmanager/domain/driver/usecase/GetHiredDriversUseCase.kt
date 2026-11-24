package com.example.busmanager.domain.driver.usecase

import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.driver.repository.DriverRepository
import kotlinx.coroutines.flow.Flow

class GetHiredDriversUseCase(
    private val driverRepository: DriverRepository
) {
    operator fun invoke(): Flow<List<Driver>> {
        return driverRepository.getHiredDrivers()
    }
}
