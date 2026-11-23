package com.example.busmanager.domain.company.usecase

import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.repository.CompanyRepository
import kotlinx.coroutines.flow.Flow

class GetCompanyUseCase(
    private val companyRepository: CompanyRepository
) {
    operator fun invoke(): Flow<Company?> = companyRepository.getCompany()
}
