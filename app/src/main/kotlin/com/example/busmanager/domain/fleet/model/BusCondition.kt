package com.example.busmanager.domain.fleet.model

data class BusCondition(
    val engineHealthPercent: Int = 100,
    val bodyHealthPercent: Int = 100,
    val odometerKm: Long = 0L
) {
    init {
        require(engineHealthPercent in 0..100) { "Engine health must be between 0 and 100" }
        require(bodyHealthPercent in 0..100) { "Body health must be between 0 and 100" }
        require(odometerKm >= 0L) { "Odometer cannot be negative" }
    }

    val isRoadworthy: Boolean
        get() = engineHealthPercent >= 30 && bodyHealthPercent >= 25
}
