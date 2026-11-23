package com.example.busmanager.domain.fleet.model

enum class BusClass(val displayName: String, val ticketMultiplier: Float) {
    EKONOMI("Ekonomi", 1.0f),
    BISNIS("Bisnis AC", 1.25f),
    VIP("VIP", 1.5f),
    EKSEKUTIF("Eksekutif", 1.8f),
    SUPER_EKSEKUTIF("Super Eksekutif", 2.2f),
    SUITES_CLASS("Suites / Sleeper Class", 2.8f)
}
