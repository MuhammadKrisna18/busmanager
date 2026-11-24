package com.example.busmanager.domain.route.usecase

import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import com.example.busmanager.data.repository.TerminalCatalogRepositoryImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetConnectedTerminalsUseCaseTest {

    private lateinit var terminalRepository: TerminalCatalogRepositoryImpl
    private lateinit var routeRepository: RouteCatalogRepositoryImpl
    private lateinit var useCase: GetConnectedTerminalsUseCase

    @Before
    fun setUp() {
        terminalRepository = TerminalCatalogRepositoryImpl()
        routeRepository = RouteCatalogRepositoryImpl()
        useCase = GetConnectedTerminalsUseCase(terminalRepository, routeRepository)
    }

    @Test
    fun invoke_fromPurabaya_returnsBayuanggaAndArjosari() {
        val connected = useCase("term_purabaya")
        assertEquals(2, connected.size)
        val cities = connected.map { it.city }
        assertTrue(cities.contains("Malang"))
        assertTrue(cities.contains("Probolinggo"))
    }

    @Test
    fun invoke_fromSitubondo_returnsBayuanggaAndSritanjung() {
        val connected = useCase("term_situbondo")
        assertEquals(2, connected.size)
        val cities = connected.map { it.city }
        assertTrue(cities.contains("Probolinggo"))
        assertTrue(cities.contains("Banyuwangi"))
    }

    @Test
    fun invoke_invalidTerminal_returnsEmptyList() {
        val connected = useCase("invalid_term")
        assertTrue(connected.isEmpty())
    }
}
