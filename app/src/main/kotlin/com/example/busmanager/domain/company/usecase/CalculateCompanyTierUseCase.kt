package com.example.busmanager.domain.company.usecase

import com.example.busmanager.domain.company.model.CompanyTier

class CalculateCompanyTierUseCase {
    operator fun invoke(busCount: Int, routeCount: Int, boardedCount: Int): CompanyTier {
        return CompanyTier.calculateTier(busCount, routeCount, boardedCount)
    }
}
