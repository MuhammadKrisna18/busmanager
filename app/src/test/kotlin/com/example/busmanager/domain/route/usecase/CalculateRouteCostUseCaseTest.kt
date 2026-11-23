package com.example.busmanager.domain.route.usecase

import com.example.busmanager.data.local.datasource.ChassisDataSource
import com.example.busmanager.data.local.datasource.KaroseriBodyDataSource
import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import com.example.busmanager.domain.fleet.model.Bus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculateRouteCostUseCaseTest {

    private lateinit var useCase: CalculateRouteCostUseCase
    private lateinit var testBus: Bus
    private val routeRepository = RouteCatalogRepositoryImpl()

    @Before
    fun setUp() {
        useCase = CalculateRouteCostUseCase()
        val chassis = ChassisDataSource().getChassisList().first { it.id == "chassis-hino-rn285" }
        val body = KaroseriBodyDataSource().getKaroseriBodyList().first { it.id == "body-ap-jb3-shd" }
        testBus = Bus(
            id = "bus-test-1",
            plateNumber = "N 1234 TN",
            nickname = "Arjuna",
            chassis = chassis,
            body = body
        )
    }

    @Test
    fun invoke_surabayaMalangToll_shouldCalculateAccurately() {
        val route = routeRepository.getRouteById("route_sby_mlg_toll")
        assertNotNull(route)

        val estimate = useCase(route!!, testBus)
        assertEquals("route_sby_mlg_toll", estimate.routeId)
        assertEquals(90.0, estimate.totalDistanceKm, 0.001)
        assertEquals(75, estimate.estimatedDurationMinutes)
        assertEquals(60000L, estimate.tollFee.amount)
        assertEquals(9000L, estimate.terminalRetributionFee.amount)
        assertTrue(estimate.estimatedFuelLiters > 0.0)
        assertTrue(estimate.estimatedFuelCost.amount > 0L)
        assertTrue(estimate.totalOperationalCost.amount > estimate.tollFee.amount)
    }

    @Test
    fun invoke_tollVsNonTollComparison_nonTollShouldHaveZeroTollFee() {
        val tollRoute = routeRepository.getRouteById("route_sby_mlg_toll")!!
        val nonTollRoute = routeRepository.getRouteById("route_sby_mlg_nontoll")!!

        val tollEstimate = useCase(tollRoute, testBus)
        val nonTollEstimate = useCase(nonTollRoute, testBus)

        assertEquals(0L, nonTollEstimate.tollFee.amount)
        assertTrue(tollEstimate.tollFee.amount > 0L)
        assertTrue(tollEstimate.estimatedDurationMinutes < nonTollEstimate.estimatedDurationMinutes)
    }
}
