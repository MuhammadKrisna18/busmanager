package com.example.busmanager.domain.company.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculateCompanyReputationUseCaseTest {

    private lateinit var useCase: CalculateCompanyReputationUseCase

    @Before
    fun setUp() {
        useCase = CalculateCompanyReputationUseCase()
    }

    @Test
    fun initialCompany_hasMaxReputation() {
        val result = useCase(totalBoardedPassengers = 0, totalLostPassengers = 0)
        assertEquals(5.0f, result.rating, 0.01f)
        assertEquals("Sangat Dipercaya Penumpang", result.statusTitle)
    }

    @Test
    fun flawlessService_maintainsMaxReputation() {
        val result = useCase(totalBoardedPassengers = 250, totalLostPassengers = 0)
        assertEquals(5.0f, result.rating, 0.01f)
        assertEquals("Sangat Dipercaya Penumpang", result.statusTitle)
    }

    @Test
    fun moderateLoss_reducesReputationProportionally() {
        val result = useCase(totalBoardedPassengers = 90, totalLostPassengers = 10)
        assertEquals(4.6f, result.rating, 0.01f)
        assertEquals("Pelayanan Sangat Baik", result.statusTitle)
    }

    @Test
    fun severeLoss_dropsReputationSignificantly() {
        val result = useCase(totalBoardedPassengers = 50, totalLostPassengers = 50)
        assertEquals(3.0f, result.rating, 0.01f)
        assertEquals("Sering Mengalami Keterlambatan", result.statusTitle)
    }

    @Test
    fun extremeLoss_neverDropsBelowFloor() {
        val result = useCase(totalBoardedPassengers = 5, totalLostPassengers = 200)
        assertTrue(result.rating >= 1.0f)
        assertEquals("Reputasi Buruk / Armada Minim", result.statusTitle)
    }
}
