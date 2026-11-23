package com.example.busmanager.data.mapper

import com.example.busmanager.data.local.entity.CompanyEntity
import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money

fun CompanyEntity.toDomain(): Company = Company(
    id = id,
    name = name,
    balance = Money.rupiah(balance),
    reputation = reputation
)

fun Company.toEntity(): CompanyEntity = CompanyEntity(
    id = id,
    name = name,
    balance = balance.amount,
    reputation = reputation
)
