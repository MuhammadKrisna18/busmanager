package com.example.busmanager.domain.finance.model

enum class TransactionCategory(val displayName: String) {
    TICKET_SALE("Tiket Penumpang"),
    DRIVER_WAGE("Gaji Kru & Sopir"),
    BUS_PURCHASE("Pembelian Bus"),
    BUS_SALE("Penjualan Bus"),
    OTHER("Lain-lain")
}
