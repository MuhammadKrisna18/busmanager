package com.example.busmanager.domain.driver.usecase

import com.example.busmanager.domain.driver.repository.DriverRepository

class AssignDriverToBusUseCase(
    private val driverRepository: DriverRepository
) {
    suspend operator fun invoke(driverId: String, busId: String): Boolean {
        return driverRepository.assignDriverToBus(driverId, busId)
    }
}
