package com.example.busmanager.domain.simulation.model

import kotlin.random.Random

class PassengerDemandGenerator(
    private val random: Random = Random.Default
) {
    fun generateInitialLoad(capacity: Int): Int {
        val fraction = random.nextDouble(0.40, 0.95).toFloat()
        return (capacity * fraction).toInt().coerceIn(1, capacity)
    }

    fun calculateTransitAlighting(currentPassengers: Int): Int {
        if (currentPassengers <= 0) return 0
        val rate = random.nextDouble(0.15, 0.40).toFloat()
        return (currentPassengers * rate).toInt().coerceIn(0, currentPassengers)
    }

    fun calculateTransitBoarding(emptySeats: Int): Int {
        if (emptySeats <= 0) return 0
        val rate = random.nextDouble(0.20, 0.85).toFloat()
        return (emptySeats * rate).toInt().coerceIn(0, emptySeats)
    }
}
