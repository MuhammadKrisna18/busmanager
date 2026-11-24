package com.example.busmanager.domain.finance.model

enum class TransactionCategory(val displayName: String) {
    TICKET_SALE("Tiket Penumpang"),
    DRIVER_WAGE("Gaji Kru & Sopir"),
    BUS_PURCHASE("Pembelian Bus"),
    OTHER("Lain-lain")
}
