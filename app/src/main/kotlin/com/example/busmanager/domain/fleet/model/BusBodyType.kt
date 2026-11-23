package com.example.busmanager.domain.fleet.model

enum class BusBodyType(val displayName: String, val heightMeters: Float) {
    HDD("High Deck Double Glass", 3.7f),
    SHD("Super High Deck", 3.85f),
    UHD("Ultra High Deck", 4.0f),
    SDD("Super Double Decker", 4.15f),
    SLEEPER_SUITES("Suites / Sleeper", 3.85f)
}
