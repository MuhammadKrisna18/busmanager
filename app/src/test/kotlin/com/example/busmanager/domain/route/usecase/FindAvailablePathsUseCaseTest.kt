package com.example.busmanager.domain.route.usecase

import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import com.example.busmanager.data.repository.TerminalCatalogRepositoryImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FindAvailablePathsUseCaseTest {

    private lateinit var terminalRepository: TerminalCatalogRepositoryImpl
    private lateinit var routeRepository: RouteCatalogRepositoryImpl
    private lateinit var useCase: FindAvailablePathsUseCase

    @Before
    fun setUp() {
        terminalRepository = TerminalCatalogRepositoryImpl()
        routeRepository = RouteCatalogRepositoryImpl()
        useCase = FindAvailablePathsUseCase(terminalRepository, routeRepository)
    }

    @Test
    fun invoke_sameOriginAndDestination_returnsEmpty() {
        val paths = useCase("term_purabaya", "term_purabaya")
        assertTrue(paths.isEmpty())
    }

    @Test
    fun invoke_surabayaToMalang_findsDirectPath() {
        val paths = useCase("term_purabaya", "term_arjosari")
        assertTrue(paths.isNotEmpty())
        val directPath = paths.first()
        assertEquals(2, directPath.size)
        assertEquals("term_purabaya", directPath.first().id)
        assertEquals("term_arjosari", directPath.last().id)
    }

    @Test
    fun invoke_surabayaToBanyuwangi_findsPanturaAndSelatanCorridors() {
        val paths = useCase("term_purabaya", "term_sritanjung")
        assertTrue(paths.size >= 2)

        val hasPantura = paths.any { path -> path.any { it.id == "term_situbondo" } }
        val hasSelatan = paths.any { path -> path.any { it.id == "term_tawangalun" } }

        assertTrue(hasPantura)
        assertTrue(hasSelatan)

        assertTrue(paths.all { it.first().id == "term_purabaya" && it.last().id == "term_sritanjung" })
    }
}
