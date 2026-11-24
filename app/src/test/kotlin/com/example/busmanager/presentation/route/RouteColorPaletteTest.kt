package com.example.busmanager.presentation.route

import androidx.compose.ui.graphics.Color
import com.example.busmanager.presentation.route.components.RouteColorPalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RouteColorPaletteTest {

    @Test
    fun predefinedRoutes_haveExpectedUniqueColors() {
        val sbyProboColor = RouteColorPalette.getColorForRoute("route_sby_probo_toll")
        val sbyMlgColor = RouteColorPalette.getColorForRoute("route_sby_mlg_toll")
        val sbyBwiPanturaColor = RouteColorPalette.getColorForRoute("route_sby_bwi_pantura_toll")
        val sbyBwiSelatanColor = RouteColorPalette.getColorForRoute("route_sby_bwi_selatan_toll")

        assertEquals(Color(0xFFF97316), sbyProboColor)
        assertEquals(Color(0xFF8B5CF6), sbyMlgColor)
        assertEquals(Color(0xFF10B981), sbyBwiPanturaColor)
        assertEquals(Color(0xFFF43F5E), sbyBwiSelatanColor)

        assertNotEquals(sbyProboColor, sbyMlgColor)
        assertNotEquals(sbyProboColor, sbyBwiPanturaColor)
        assertNotEquals(sbyMlgColor, sbyBwiSelatanColor)
    }

    @Test
    fun unknownRouteId_resolvesToDeterministicDynamicColor() {
        val color1 = RouteColorPalette.getColorForRoute("route_custom_xyz")
        val color2 = RouteColorPalette.getColorForRoute("route_custom_xyz")

        assertNotNull(color1)
        assertEquals(color1, color2)
    }
}
