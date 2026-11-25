package com.example.busmanager.domain.company.usecase

import com.example.busmanager.domain.company.model.CompanyTier
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculateCompanyTierUseCaseTest {

    private lateinit var useCase: CalculateCompanyTierUseCase

    @Before
    fun setUp() {
        useCase = CalculateCompanyTierUseCase()
    }

    @Test
    fun newCompany_startsAtPerintisTier() {
        val tier = useCase(busCount = 1, routeCount = 0, boardedCount = 0)
        assertEquals(CompanyTier.PERINTIS, tier)
        assertEquals(1, tier.level)
        assertEquals("PO Perintis", tier.title)
    }

    @Test
    fun reachingThresholds_promotesToRegional() {
        val tier = useCase(busCount = 2, routeCount = 1, boardedCount = 50)
        assertEquals(CompanyTier.REGIONAL, tier)
        assertEquals(2, tier.level)
        assertEquals("Operator Regional", tier.title)
    }

    @Test
    fun reachingThresholds_promotesToKelasUtama() {
        val tier = useCase(busCount = 5, routeCount = 2, boardedCount = 200)
        assertEquals(CompanyTier.BERKEMBANG, tier)
        assertEquals(3, tier.level)
        assertEquals("PO Kelas Utama", tier.title)
    }

    @Test
    fun reachingThresholds_promotesToElitJatim() {
        val tier = useCase(busCount = 8, routeCount = 3, boardedCount = 500)
        assertEquals(CompanyTier.EKSEKUTIF, tier)
        assertEquals(4, tier.level)
        assertEquals("Operator Elit Jatim", tier.title)
    }

    @Test
    fun topTier_reachesRajaJalanan() {
        val tier = useCase(busCount = 15, routeCount = 6, boardedCount = 1200)
        assertEquals(CompanyTier.PENGUASA, tier)
        assertEquals(5, tier.level)
        assertEquals("Raja Jalanan Nusantara", tier.title)
    }
}
