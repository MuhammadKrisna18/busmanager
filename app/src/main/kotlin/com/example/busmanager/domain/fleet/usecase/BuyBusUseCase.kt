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

    suspend fun buyBatch(templateBus: Bus, quantity: Int): BuyBusResult {
        if (quantity <= 0) return BuyBusResult.CompanyNotFound
        val company = companyRepository.getCompany().first() ?: return BuyBusResult.CompanyNotFound
        val totalCost = templateBus.totalPrice * quantity.toLong()
        if (!company.balance.canAfford(totalCost)) {
            return BuyBusResult.InsufficientFunds(required = totalCost, available = company.balance)
        }
        val deducted = companyRepository.deductBalance(totalCost)
        if (!deducted) {
            return BuyBusResult.InsufficientFunds(required = totalCost, available = company.balance)
        }

        val ownedBuses = fleetRepository.getOwnedBuses().first()
        val baseNumber = ownedBuses.size + 1
        val purchasedBuses = mutableListOf<Bus>()

        for (i in 0 until quantity) {
            val num = baseNumber + i
            val code = String.format("TN-%02d", num)
            val newBus = templateBus.copy(
                id = java.util.UUID.randomUUID().toString(),
                busCode = code
            )
            fleetRepository.addBus(newBus)
            purchasedBuses.add(newBus)
        }

        return BuyBusResult.BatchSuccess(purchasedBuses)
    }
}
