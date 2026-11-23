package com.example.busmanager.domain.company.model

data class Company(
    val id: String,
    val name: String,
    val balance: Money,
    val reputation: Int = 100
)
