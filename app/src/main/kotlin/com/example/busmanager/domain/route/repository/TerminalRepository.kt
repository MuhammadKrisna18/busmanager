package com.example.busmanager.domain.route.repository

import com.example.busmanager.domain.route.model.Terminal

interface TerminalRepository {
    fun getAllTerminals(): List<Terminal>
    fun getTerminalById(id: String): Terminal?
    fun getTerminalsByCity(city: String): List<Terminal>
}
