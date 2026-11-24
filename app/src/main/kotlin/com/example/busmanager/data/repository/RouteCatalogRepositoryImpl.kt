package com.example.busmanager.data.repository

import com.example.busmanager.data.local.dao.CustomRouteDao
import com.example.busmanager.data.local.dao.RoutePricingDao
import com.example.busmanager.data.local.dao.RouteScheduleDao
import com.example.busmanager.data.local.datasource.RouteDataSource
import com.example.busmanager.data.mapper.toCustomRouteEntity
import com.example.busmanager.data.mapper.toDomain
import com.example.busmanager.domain.route.model.BusRoute
import com.example.busmanager.domain.route.model.RoutePathType
import com.example.busmanager.domain.route.model.RouteSegment
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.repository.TerminalRepository

class RouteCatalogRepositoryImpl(
    private val dataSource: RouteDataSource = RouteDataSource(),
    private val customRouteDao: CustomRouteDao? = null,
    private val terminalRepository: TerminalRepository? = null,
    private val routeScheduleDao: RouteScheduleDao? = null,
    private val routePricingDao: RoutePricingDao? = null
) : RouteRepository {

    private val segmentMap = dataSource.getAllSegments().associateBy { it.id }
    private val customRoutes = mutableListOf<BusRoute>()

    init {
        loadCustomRoutesFromDatabase()
    }

    private fun loadCustomRoutesFromDatabase() {
        if (customRouteDao != null && terminalRepository != null) {
            try {
                val entities = customRouteDao.getAllCustomRoutesSync()
                val loadedRoutes = entities.mapNotNull { entity ->
                    entity.toDomain(terminalRepository) { segmentMap[it] }
                }
                synchronized(customRoutes) {
                    customRoutes.clear()
                    customRoutes.addAll(loadedRoutes)
                }
            } catch (_: Exception) {}
        }
    }

    override fun getAllSegments(): List<RouteSegment> {
        return dataSource.getAllSegments()
    }

    override fun getSegmentById(id: String): RouteSegment? {
        return segmentMap[id]
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
        val customs = synchronized(customRoutes) { customRoutes.toList() }
        return dataSource.getPredefinedRoutes() + customs
    }

    override fun getRouteById(id: String): BusRoute? {
        val custom = synchronized(customRoutes) { customRoutes.find { it.id == id } }
        return custom ?: dataSource.getPredefinedRoutes().find { it.id == id }
    }

    override fun saveRoute(route: BusRoute) {
        synchronized(customRoutes) {
            val existingIndex = customRoutes.indexOfFirst { it.id == route.id }
            if (existingIndex >= 0) {
                customRoutes[existingIndex] = route
            } else {
                customRoutes.add(route)
            }
        }
        customRouteDao?.insertCustomRouteSync(route.toCustomRouteEntity())
    }

    override fun deleteRoute(routeId: String) {
        synchronized(customRoutes) {
            customRoutes.removeAll { it.id == routeId }
        }
        customRouteDao?.deleteCustomRouteById(routeId)
        routeScheduleDao?.deleteSchedulesByRouteSync(routeId)
        routePricingDao?.deletePricingsByRouteSync(routeId)
    }

    override fun deleteAllCustomRoutes() {
        synchronized(customRoutes) {
            customRoutes.clear()
        }
        customRouteDao?.deleteAllCustomRoutes()
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
