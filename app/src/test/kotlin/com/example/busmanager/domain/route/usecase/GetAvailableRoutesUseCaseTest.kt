package com.example.busmanager.domain.route.usecase

import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import com.example.busmanager.domain.route.model.RoutePathType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetAvailableRoutesUseCaseTest {

    private lateinit var useCase: GetAvailableRoutesUseCase

    @Before
    fun setUp() {
        val repository = RouteCatalogRepositoryImpl()
        useCase = GetAvailableRoutesUseCase(repository)
    }

    @Test
    fun invoke_sameOriginAndDestination_shouldReturnEmpty() {
        val routes = useCase("term_purabaya", "term_purabaya")
        assertTrue(routes.isEmpty())
    }

    @Test
    fun invoke_blankTerminal_shouldReturnEmpty() {
        val routes = useCase("", "term_purabaya")
        assertTrue(routes.isEmpty())
    }

    @Test
    fun invoke_validOriginAndDestination_shouldReturnCorridors() {
        val routes = useCase("term_purabaya", "term_sritanjung")
        assertEquals(4, routes.size)
    }

    @Test
    fun invoke_withPreferredPathType_shouldFilterCorrectly() {
        val tollRoutes = useCase(
            originTerminalId = "term_purabaya",
            destinationTerminalId = "term_arjosari",
            preferredPathType = RoutePathType.TOLL
        )
        assertEquals(1, tollRoutes.size)
        assertEquals(RoutePathType.TOLL, tollRoutes.first().pathType)
    }
}
