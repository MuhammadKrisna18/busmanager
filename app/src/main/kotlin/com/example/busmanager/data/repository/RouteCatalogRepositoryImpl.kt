package com.example.busmanager.data.repository

import com.example.busmanager.data.local.datasource.RouteDataSource
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RouteSegment
import com.example.busmanager.domain.route.repository.RouteRepository

class RouteCatalogRepositoryImpl(
    private val dataSource: RouteDataSource = RouteDataSource()
) : RouteRepository {

    private val customRoutes = mutableListOf<BusRoute>()

    override fun getAllSegments(): List<RouteSegment> {
        return dataSource.getAllSegments()
    }

    override fun getSegmentById(id: String): RouteSegment? {
        return dataSource.getAllSegments().find { it.id == id }
    }

    override fun getSegmentsBetween(fromTerminalId: String, toTerminalId: String): List<RouteSegment> {
        return dataSource.getAllSegments().filter {
            it.fromTerminalId == fromTerminalId && it.toTerminalId == toTerminalId
        }
    }

    override fun getAllPredefinedRoutes(): List<BusRoute> {
        return dataSource.getPredefinedRoutes()
    }

    override fun getAllRoutes(): List<BusRoute> {
        return dataSource.getPredefinedRoutes() + customRoutes
    }

    override fun getRouteById(id: String): BusRoute? {
        return customRoutes.find { it.id == id } ?: dataSource.getPredefinedRoutes().find { it.id == id }
    }

    override fun saveRoute(route: BusRoute) {
        val existingIndex = customRoutes.indexOfFirst { it.id == route.id }
        if (existingIndex >= 0) {
            customRoutes[existingIndex] = route
        } else {
            customRoutes.add(route)
        }
    }

    override fun findRoutes(
        originTerminalId: String,
        destinationTerminalId: String,
        preferredPathType: RoutePathType?
    ): List<BusRoute> {
        return getAllRoutes().filter { route ->
            val matchOrigin = route.originTerminal.id == originTerminalId
            val matchDestination = route.destinationTerminal.id == destinationTerminalId
            val matchPathType = preferredPathType == null || route.pathType == preferredPathType
            matchOrigin && matchDestination && matchPathType
        }
    }

    override fun getConnectedTerminalIds(terminalId: String): List<String> {
        return dataSource.getAllSegments()
            .filter { it.fromTerminalId == terminalId }
            .map { it.toTerminalId }
            .distinct()
    }
}
