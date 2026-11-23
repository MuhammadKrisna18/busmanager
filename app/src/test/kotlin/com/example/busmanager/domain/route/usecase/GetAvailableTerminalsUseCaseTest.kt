package com.example.busmanager.domain.route.usecase

import com.example.busmanager.data.repository.TerminalCatalogRepositoryImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetAvailableTerminalsUseCaseTest {

    private lateinit var useCase: GetAvailableTerminalsUseCase

    @Before
    fun setUp() {
        val repository = TerminalCatalogRepositoryImpl()
        useCase = GetAvailableTerminalsUseCase(repository)
    }

    @Test
    fun invoke_shouldReturnAllAvailableTerminals() {
        val terminals = useCase()
        assertEquals(6, terminals.size)
        assertTrue(terminals.any { it.city == "Surabaya" })
        assertTrue(terminals.any { it.city == "Malang" })
        assertTrue(terminals.any { it.city == "Probolinggo" })
        assertTrue(terminals.any { it.city == "Jember" })
        assertTrue(terminals.any { it.city == "Situbondo" })
        assertTrue(terminals.any { it.city == "Banyuwangi" })
    }
}
