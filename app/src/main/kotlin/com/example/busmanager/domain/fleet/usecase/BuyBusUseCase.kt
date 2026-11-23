package com.example.busmanager.domain.fleet.usecase

import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.model.BuyBusResult
import com.example.busmanager.domain.fleet.repository.FleetRepository
import kotlinx.coroutines.flow.first

class BuyBusUseCase(
    private val companyRepository: CompanyRepository,
    private val fleetRepository: FleetRepository
) {
    suspend operator fun invoke(bus: Bus): BuyBusResult {
        val company = companyRepository.getCompany().first() ?: return BuyBusResult.CompanyNotFound
        if (!company.balance.canAfford(bus.totalPrice)) {
            return BuyBusResult.InsufficientFunds(required = bus.totalPrice, available = company.balance)
        }
        val deducted = companyRepository.deductBalance(bus.totalPrice)
        if (!deducted) {
            return BuyBusResult.InsufficientFunds(required = bus.totalPrice, available = company.balance)
        }
        fleetRepository.addBus(bus)
        return BuyBusResult.Success(bus)
    }
}
