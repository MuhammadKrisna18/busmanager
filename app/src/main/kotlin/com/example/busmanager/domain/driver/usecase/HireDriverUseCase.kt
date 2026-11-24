package com.example.busmanager.domain.driver.usecase

import com.example.busmanager.domain.driver.repository.DriverRepository

class HireDriverUseCase(
    private val driverRepository: DriverRepository
) {
    suspend operator fun invoke(driverId: String): Boolean {
        return driverRepository.hireDriver(driverId)
    }
}
