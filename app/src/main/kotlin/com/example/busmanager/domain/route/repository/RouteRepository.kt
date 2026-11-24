package com.example.busmanager.domain.route.repository

import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RouteSegment

interface RouteRepository {
    fun getAllSegments(): List<RouteSegment>
    fun getSegmentById(id: String): RouteSegment?
    fun getSegmentsBetween(fromTerminalId: String, toTerminalId: String): List<RouteSegment>
    fun getAllPredefinedRoutes(): List<BusRoute>
    fun getAllRoutes(): List<BusRoute>
    fun getRouteById(id: String): BusRoute?
    fun saveRoute(route: BusRoute)
    fun findRoutes(
        originTerminalId: String,
        destinationTerminalId: String,
        preferredPathType: RoutePathType? = null
    ): List<BusRoute>
    fun getConnectedTerminalIds(terminalId: String): List<String>
}
