package com.example.busmanager.data.repository

import android.util.Log
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RouteCatalogRepositoryImpl(
    private val dataSource: RouteDataSource = RouteDataSource(),
    private val customRouteDao: CustomRouteDao? = null,
    private val terminalRepository: TerminalRepository? = null,
    private val routeScheduleDao: RouteScheduleDao? = null,
    private val routePricingDao: RoutePricingDao? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RouteRepository {

    private val scope = CoroutineScope(ioDispatcher)
    private val segmentMap = dataSource.getAllSegments().associateBy { it.id }
    private val customRoutes = mutableListOf<BusRoute>()

    init {
        loadCustomRoutesFromDatabase()
    }

    private fun loadCustomRoutesFromDatabase() {
        if (customRouteDao != null && terminalRepository != null) {
            scope.launch {
                try {
                    val entities = customRouteDao.getAllCustomRoutes()
                    val loadedRoutes = entities.mapNotNull { entity ->
                        entity.toDomain(terminalRepository) { segmentMap[it] }
                    }
                    synchronized(customRoutes) {
                        customRoutes.clear()
                        customRoutes.addAll(loadedRoutes)
                    }
                } catch (e: Exception) {
                    Log.e("RouteCatalogRepo", "Gagal memuat rute kustom dari database", e)
                }
            }
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

    override suspend fun saveRoute(route: BusRoute) {
        synchronized(customRoutes) {
            val existingIndex = customRoutes.indexOfFirst { it.id == route.id }
            if (existingIndex >= 0) {
                customRoutes[existingIndex] = route
            } else {
                customRoutes.add(route)
            }
        }
        if (customRouteDao != null) {
            withContext(ioDispatcher) {
                try {
                    customRouteDao.insertCustomRoute(route.toCustomRouteEntity())
                } catch (e: Exception) {
                    Log.e("RouteCatalogRepo", "Gagal menyimpan rute kustom", e)
                }
            }
        }
    }

    override suspend fun deleteRoute(routeId: String) {
        synchronized(customRoutes) {
            customRoutes.removeAll { it.id == routeId }
        }
        withContext(ioDispatcher) {
            try {
                customRouteDao?.deleteCustomRouteById(routeId)
                routeScheduleDao?.deleteSchedulesByRoute(routeId)
                routePricingDao?.deletePricingsByRoute(routeId)
            } catch (e: Exception) {
                Log.e("RouteCatalogRepo", "Gagal menghapus rute", e)
            }
        }
    }

    override suspend fun deleteAllCustomRoutes() {
        synchronized(customRoutes) {
            customRoutes.clear()
        }
        withContext(ioDispatcher) {
            try {
                customRouteDao?.deleteAllCustomRoutes()
            } catch (e: Exception) {
                Log.e("RouteCatalogRepo", "Gagal menghapus seluruh rute kustom", e)
            }
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
