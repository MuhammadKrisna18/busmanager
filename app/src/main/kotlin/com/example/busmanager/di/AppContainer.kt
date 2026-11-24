package com.example.busmanager.di

import android.content.Context
import com.example.busmanager.core.update.AppUpdateManager
import com.example.busmanager.data.local.database.BusManagerDatabase
import com.example.busmanager.data.repository.ChassisCatalogRepositoryImpl
import com.example.busmanager.data.repository.CompanyRepositoryImpl
import com.example.busmanager.data.repository.FleetRepositoryImpl
import com.example.busmanager.data.repository.KaroseriCatalogRepositoryImpl
import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import com.example.busmanager.data.repository.ShowroomCatalogRepositoryImpl
import com.example.busmanager.data.repository.SimulationTimeRepositoryImpl
import com.example.busmanager.data.repository.TerminalCatalogRepositoryImpl
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.company.usecase.GetCompanyUseCase
import com.example.busmanager.domain.company.usecase.StartNewGameUseCase
import com.example.busmanager.domain.fleet.repository.ChassisCatalogRepository
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.fleet.repository.KaroseriCatalogRepository
import com.example.busmanager.domain.fleet.repository.ShowroomCatalogRepository
import com.example.busmanager.domain.fleet.usecase.BuyBusUseCase
import com.example.busmanager.domain.fleet.usecase.GetOwnedFleetUseCase
import com.example.busmanager.domain.fleet.usecase.GetShowroomBusesUseCase
import com.example.busmanager.domain.route.repository.RouteRepository
import com.example.busmanager.domain.route.repository.TerminalRepository
import com.example.busmanager.domain.route.usecase.CalculateRouteCostUseCase
import com.example.busmanager.domain.route.usecase.GetAvailableRoutesUseCase
import com.example.busmanager.domain.route.usecase.GetAvailableTerminalsUseCase
import com.example.busmanager.domain.route.usecase.GetRouteTransitStopsUseCase
import com.example.busmanager.domain.simulation.repository.SimulationTimeRepository
import com.example.busmanager.domain.simulation.usecase.GetSimulationTimeUseCase
import com.example.busmanager.domain.simulation.usecase.ToggleSimulationPlayUseCase

class AppContainer(context: Context) {

    private val database: BusManagerDatabase = BusManagerDatabase.getInstance(context)

    val chassisCatalogRepository: ChassisCatalogRepository = ChassisCatalogRepositoryImpl()
    val karoseriCatalogRepository: KaroseriCatalogRepository = KaroseriCatalogRepositoryImpl()
    val showroomCatalogRepository: ShowroomCatalogRepository = ShowroomCatalogRepositoryImpl()
    val terminalRepository: TerminalRepository = TerminalCatalogRepositoryImpl()
    val routeRepository: RouteRepository = RouteCatalogRepositoryImpl()
    val simulationTimeRepository: SimulationTimeRepository = SimulationTimeRepositoryImpl()

    val companyRepository: CompanyRepository = CompanyRepositoryImpl(database.companyDao())
    val fleetRepository: FleetRepository = FleetRepositoryImpl(
        database.ownedBusDao(),
        chassisCatalogRepository,
        karoseriCatalogRepository
    )

    val startNewGameUseCase = StartNewGameUseCase(companyRepository)
    val getCompanyUseCase = GetCompanyUseCase(companyRepository)
    val getOwnedFleetUseCase = GetOwnedFleetUseCase(fleetRepository)
    val getShowroomBusesUseCase = GetShowroomBusesUseCase(showroomCatalogRepository)
    val buyBusUseCase = BuyBusUseCase(companyRepository, fleetRepository)

    val getAvailableTerminalsUseCase = GetAvailableTerminalsUseCase(terminalRepository)
    val getAvailableRoutesUseCase = GetAvailableRoutesUseCase(routeRepository)
    val getRouteTransitStopsUseCase = GetRouteTransitStopsUseCase(routeRepository)
    val calculateRouteCostUseCase = CalculateRouteCostUseCase()

    val getSimulationTimeUseCase = GetSimulationTimeUseCase(simulationTimeRepository)
    val toggleSimulationPlayUseCase = ToggleSimulationPlayUseCase(simulationTimeRepository)

    val appUpdateManager = AppUpdateManager()
}
