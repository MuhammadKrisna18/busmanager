package com.example.busmanager.presentation.route.components

import androidx.compose.ui.graphics.Color

object RouteColorPalette {
    private val predefinedRouteColors = mapOf(
        "route_sby_mlg_toll" to Color(0xFF8B5CF6),
        "route_sby_mlg_nontoll" to Color(0xFF7C3AED),
        "route_mlg_sby_toll" to Color(0xFF6366F1),
        "route_mlg_sby_nontoll" to Color(0xFF4F46E5),
        "route_sby_probo_toll" to Color(0xFFF97316),
        "route_sby_probo_nontoll" to Color(0xFFEA580C),
        "route_probo_sby_toll" to Color(0xFFF59E0B),
        "route_probo_sby_nontoll" to Color(0xFFD97706),
        "route_sby_bwi_pantura_toll" to Color(0xFF10B981),
        "route_sby_bwi_pantura_nontoll" to Color(0xFF059669),
        "route_bwi_sby_pantura_toll" to Color(0xFF06B6D4),
        "route_bwi_sby_pantura_nontoll" to Color(0xFF0891B2),
        "route_sby_bwi_selatan_toll" to Color(0xFFF43F5E),
        "route_sby_bwi_selatan_nontoll" to Color(0xFFE11D48),
        "route_bwi_sby_selatan_toll" to Color(0xFFBE123C),
        "route_bwi_sby_selatan_nontoll" to Color(0xFF9F1239),
        "route_mlg_bwi_selatan" to Color(0xFFEC4899),
        "route_bwi_mlg_selatan" to Color(0xFFDB2777)
    )

    private val dynamicPalette = listOf(
        Color(0xFF8B5CF6),
        Color(0xFFF97316),
        Color(0xFF10B981),
        Color(0xFFF43F5E),
        Color(0xFF06B6D4),
        Color(0xFFF59E0B),
        Color(0xFF6366F1),
        Color(0xFFEC4899),
        Color(0xFF14B8A6),
        Color(0xFF3B82F6)
    )

    fun getColorForRoute(routeId: String): Color {
        return predefinedRouteColors[routeId] ?: run {
            val hash = kotlin.math.abs(routeId.hashCode())
            dynamicPalette[hash % dynamicPalette.size]
        }
    }
}
