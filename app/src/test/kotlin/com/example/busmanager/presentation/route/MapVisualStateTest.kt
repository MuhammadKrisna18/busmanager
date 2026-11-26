package com.example.busmanager.presentation.route

import com.example.busmanager.data.local.datasource.RouteDataSource
import com.example.busmanager.data.local.datasource.TerminalDataSource
import com.example.busmanager.presentation.route.components.MapVisualState
import com.example.busmanager.presentation.route.components.TerminalNodeRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MapVisualStateTest {

    private val terminalDataSource = TerminalDataSource()
    private val routeDataSource = RouteDataSource(terminalDataSource)

    @Test
    fun defaultMapVisualState_shouldInitializeCorrectly() {
        val state = MapVisualState()
        assertEquals(0, state.terminals.size)
        assertEquals(0, state.allSegments.size)
        assertNull(state.selectedRoute)
        assertNull(state.selectedOrigin)
        assertNull(state.selectedDestination)
        assertEquals(0, state.transitTerminals.size)
    }

    @Test
    fun mapVisualState_withLoadedData_shouldPreserveHierarchy() {
        val terminals = terminalDataSource.getTerminals()
        val segments = routeDataSource.getAllSegments()
        val route = routeDataSource.getPredefinedRoutes().first()

        val state = MapVisualState(
            terminals = terminals,
            allSegments = segments,
            selectedRoute = route,
            selectedOrigin = route.originTerminal,
            selectedDestination = route.destinationTerminal,
            transitTerminals = route.transitTerminals
        )

        assertEquals(21, state.terminals.size)
        assertEquals(82, state.allSegments.size)
        assertNotNull(state.selectedRoute)
        assertEquals(route.originTerminal, state.selectedOrigin)
        assertEquals(route.destinationTerminal, state.selectedDestination)
    }

    @Test
    fun terminalNodeRole_determination_shouldMatchSelectionState() {
        val terminals = terminalDataSource.getTerminals()
        val origin = terminals.first { it.id == "term_purabaya" }
        val transit = terminals.first { it.id == "term_bayuangga" }
        val destination = terminals.first { it.id == "term_sritanjung" }
        val normal = terminals.first { it.id == "term_arjosari" }

        val transitList = listOf(transit)

        fun determineRole(terminalId: String): TerminalNodeRole {
            return when {
                origin.id == terminalId -> TerminalNodeRole.ORIGIN
                destination.id == terminalId -> TerminalNodeRole.DESTINATION
                transitList.any { it.id == terminalId } -> TerminalNodeRole.TRANSIT
                else -> TerminalNodeRole.DEFAULT
            }
        }

        assertEquals(TerminalNodeRole.ORIGIN, determineRole(origin.id))
        assertEquals(TerminalNodeRole.DESTINATION, determineRole(destination.id))
        assertEquals(TerminalNodeRole.TRANSIT, determineRole(transit.id))
        assertEquals(TerminalNodeRole.DEFAULT, determineRole(normal.id))
    }
}
