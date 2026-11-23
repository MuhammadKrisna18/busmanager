package com.example.busmanager.domain.route.model

enum class RoadCharacteristic(
    val displayName: String,
    val baseWearRate: Float,
    val fuelMultiplier: Float
) {
    FLAT("Jalan Datar", 1.0f, 1.0f),
    COASTAL("Pesisir Pantai", 1.1f, 1.05f),
    MOUNTAIN_PASS("Pegunungan / Berkelok", 1.4f, 1.35f)
}
