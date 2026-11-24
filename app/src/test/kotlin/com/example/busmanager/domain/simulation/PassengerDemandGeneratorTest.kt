package com.example.busmanager.domain.simulation

import com.example.busmanager.domain.simulation.model.PassengerDemandGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PassengerDemandGeneratorTest {

    private val generator = PassengerDemandGenerator(Random(42))

    @Test
    fun generateInitialLoad_returnsWithinExpectedBounds() {
        val capacity = 40
        repeat(50) {
            val count = generator.generateInitialLoad(capacity)
            assertTrue(count in 16..38)
            assertTrue(count in 1..capacity)
        }
    }

    @Test
    fun calculateTransitAlighting_returnsWithinExpectedBounds() {
        val currentPassengers = 30
        repeat(50) {
            val alighted = generator.calculateTransitAlighting(currentPassengers)
            assertTrue(alighted in 4..12)
            assertTrue(alighted in 0..currentPassengers)
        }
    }

    @Test
    fun calculateTransitBoarding_returnsWithinExpectedBounds() {
        val emptySeats = 20
        repeat(50) {
            val boarded = generator.calculateTransitBoarding(emptySeats)
            assertTrue(boarded in 4..17)
            assertTrue(boarded in 0..emptySeats)
        }
    }

    @Test
    fun edgeCases_zeroCapacityOrEmptyBus_handledSafely() {
        assertEquals(0, generator.calculateTransitAlighting(0))
        assertEquals(0, generator.calculateTransitBoarding(0))
        assertEquals(1, generator.generateInitialLoad(1))
    }
}
