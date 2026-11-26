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
        assertEquals(21, terminals.size)
        assertTrue(terminals.any { it.city == "Surabaya" })
        assertTrue(terminals.any { it.city == "Malang" })
        assertTrue(terminals.any { it.city == "Probolinggo" })
        assertTrue(terminals.any { it.city == "Jember" })
        assertTrue(terminals.any { it.city == "Situbondo" })
        assertTrue(terminals.any { it.city == "Banyuwangi" })
        assertTrue(terminals.any { it.city == "Mojokerto" })
        assertTrue(terminals.any { it.city == "Kediri" })
        assertTrue(terminals.any { it.city == "Tulungagung" })
        assertTrue(terminals.any { it.city == "Blitar" })
        assertTrue(terminals.any { it.city == "Trenggalek" })
        assertTrue(terminals.any { it.city == "Ponorogo" })
        assertTrue(terminals.any { it.city == "Madiun" })
        assertTrue(terminals.any { it.city == "Ngawi" })
        assertTrue(terminals.any { it.city == "Nganjuk" })
        assertTrue(terminals.any { it.city == "Bojonegoro" })
        assertTrue(terminals.any { it.city == "Lumajang" })
        assertTrue(terminals.any { it.city == "Bondowoso" })
        assertTrue(terminals.any { it.city == "Pacitan" })
        assertTrue(terminals.any { it.city == "Sragen" })
        assertTrue(terminals.any { it.city == "Pati" })
    }
}
