package com.example.busmanager.data

import com.example.busmanager.data.repository.TerminalCatalogRepositoryImpl
import com.example.busmanager.domain.route.model.TerminalType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TerminalRepositoryTest {

    private lateinit var repository: TerminalCatalogRepositoryImpl

    @Before
    fun setUp() {
        repository = TerminalCatalogRepositoryImpl()
    }

    @Test
    fun getAllTerminals_shouldReturnAllTerminals() {
        val terminals = repository.getAllTerminals()
        assertEquals(21, terminals.size)

        val cities = terminals.map { it.city }.toSet()
        assertTrue(cities.contains("Surabaya"))
        assertTrue(cities.contains("Malang"))
        assertTrue(cities.contains("Probolinggo"))
        assertTrue(cities.contains("Jember"))
        assertTrue(cities.contains("Situbondo"))
        assertTrue(cities.contains("Banyuwangi"))
        assertTrue(cities.contains("Mojokerto"))
        assertTrue(cities.contains("Kediri"))
        assertTrue(cities.contains("Tulungagung"))
        assertTrue(cities.contains("Blitar"))
        assertTrue(cities.contains("Trenggalek"))
        assertTrue(cities.contains("Ponorogo"))
        assertTrue(cities.contains("Madiun"))
        assertTrue(cities.contains("Ngawi"))
        assertTrue(cities.contains("Nganjuk"))
        assertTrue(cities.contains("Bojonegoro"))
        assertTrue(cities.contains("Lumajang"))
        assertTrue(cities.contains("Bondowoso"))
        assertTrue(cities.contains("Pacitan"))
        assertTrue(cities.contains("Sragen"))
        assertTrue(cities.contains("Pati"))
    }

    @Test
    fun getTerminalById_existingTerminal_shouldReturnTerminal() {
        val terminal = repository.getTerminalById("term_purabaya")
        assertNotNull(terminal)
        assertEquals("Terminal Purabaya", terminal?.name)
        assertEquals("Surabaya", terminal?.city)
        assertEquals(TerminalType.TIPE_A, terminal?.type)
        assertTrue(terminal?.retributionFee?.amount ?: 0L > 0L)
    }

    @Test
    fun getTerminalById_nonExistingTerminal_shouldReturnNull() {
        val terminal = repository.getTerminalById("term_invalid")
        assertNull(terminal)
    }

    @Test
    fun getTerminalsByCity_caseInsensitive_shouldReturnCorrectTerminals() {
        val surabaya = repository.getTerminalsByCity("surabaya")
        assertEquals(1, surabaya.size)
        assertEquals("term_purabaya", surabaya.first().id)

        val banyuwangi = repository.getTerminalsByCity("Banyuwangi")
        assertEquals(1, banyuwangi.size)
        assertEquals("term_sritanjung", banyuwangi.first().id)
    }
}
