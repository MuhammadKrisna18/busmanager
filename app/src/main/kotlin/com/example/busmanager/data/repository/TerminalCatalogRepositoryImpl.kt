package com.example.busmanager.data.repository

import com.example.busmanager.data.local.datasource.TerminalDataSource
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.repository.TerminalRepository

class TerminalCatalogRepositoryImpl(
    private val dataSource: TerminalDataSource = TerminalDataSource()
) : TerminalRepository {

    override fun getAllTerminals(): List<Terminal> {
        return dataSource.getTerminals()
    }

    override fun getTerminalById(id: String): Terminal? {
        return dataSource.getTerminalById(id)
    }

    override fun getTerminalsByCity(city: String): List<Terminal> {
        return dataSource.getTerminals().filter { it.city.equals(city, ignoreCase = true) }
    }
}
