package com.example.busmanager.domain.company.usecase

import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.driver.repository.DriverRepository
import kotlinx.coroutines.flow.first

class StartNewGameUseCase(
    private val companyRepository: CompanyRepository,
    private val driverRepository: DriverRepository? = null
) {
    suspend operator fun invoke(companyName: String = DEFAULT_COMPANY_NAME): Company {
        driverRepository?.seedCandidateDriversIfEmpty()
        val existing = companyRepository.getCompany().first()
        if (existing != null) {
            if (existing.balance < DEFAULT_STARTING_BALANCE) {
                companyRepository.addBalance(DEFAULT_STARTING_BALANCE - existing.balance)
                return companyRepository.getCompany().first() ?: existing
            }
            return existing
        }
        return companyRepository.createCompany(
            name = companyName,
            initialBalance = DEFAULT_STARTING_BALANCE
        )
    }

    companion object {
        const val DEFAULT_COMPANY_NAME = "PO TransNusantara"
        val DEFAULT_STARTING_BALANCE = Money.rupiah(50_000_000_000L)
    }
}
