package com.example.busmanager

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.busmanager.di.AppContainer
import com.example.busmanager.presentation.dashboard.HomeScreen
import com.example.busmanager.presentation.dashboard.HomeScreenViewModel
import com.example.busmanager.presentation.driver.DriverManagementScreen
import com.example.busmanager.presentation.driver.DriverManagementViewModel
import com.example.busmanager.presentation.garage.GarageScreen
import com.example.busmanager.presentation.garage.GarageViewModel
import com.example.busmanager.presentation.route.wizard.RouteWizardScreen
import com.example.busmanager.presentation.route.wizard.RouteWizardViewModel
import com.example.busmanager.presentation.showroom.ShowroomScreen
import com.example.busmanager.presentation.showroom.ShowroomViewModel
import com.example.busmanager.presentation.simulation.SimulationMapScreen
import com.example.busmanager.presentation.simulation.SimulationMapViewModel
import com.example.busmanager.presentation.splash.SplashScreen

@Composable
fun MainNavigation(appContainer: AppContainer) {
    val backStack = rememberNavBackStack(SplashRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<SplashRoute> {
                SplashScreen(
                    startNewGameUseCase = appContainer.startNewGameUseCase,
                    onNavigateToHome = {
                        backStack.clear()
                        backStack.add(HomeRoute)
                    }
                )
            }
            entry<HomeRoute> {
                val viewModel: HomeScreenViewModel = viewModel {
                    HomeScreenViewModel(
                        getCompanyUseCase = appContainer.getCompanyUseCase,
                        getOwnedFleetUseCase = appContainer.getOwnedFleetUseCase,
                        startNewGameUseCase = appContainer.startNewGameUseCase,
                        getHiredDriversUseCase = appContainer.getHiredDriversUseCase,
                        appUpdateManager = appContainer.appUpdateManager
                    )
                }
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToSimulationMap = {
                        backStack.add(SimulationMapRoute)
                    },
                    onNavigateToShowroom = {
                        backStack.add(ShowroomRoute)
                    },
                    onNavigateToGarage = {
                        backStack.add(GarageRoute)
                    },
                    onNavigateToRoutes = {
                        backStack.add(RoutePlannerRoute)
                    },
                    onNavigateToDrivers = {
                        backStack.add(DriverManagementRoute)
                    }
                )
            }
            entry<SimulationMapRoute> {
                val viewModel: SimulationMapViewModel = viewModel {
                    SimulationMapViewModel(
                        getSimulationTimeUseCase = appContainer.getSimulationTimeUseCase,
                        toggleSimulationPlayUseCase = appContainer.toggleSimulationPlayUseCase,
                        getCompanyUseCase = appContainer.getCompanyUseCase,
                        getOwnedFleetUseCase = appContainer.getOwnedFleetUseCase,
                        getAvailableTerminalsUseCase = appContainer.getAvailableTerminalsUseCase,
                        routeRepository = appContainer.routeRepository,
                        getActiveTripsUseCase = appContainer.getActiveTripsUseCase,
                        dispatchBusUseCase = appContainer.dispatchBusUseCase,
                        validateBusOperationUseCase = appContainer.validateBusOperationUseCase,
                        routeScheduleRepository = appContainer.routeScheduleRepository,
                        routePricingRepository = appContainer.routePricingRepository,
                        companyRepository = appContainer.companyRepository,
                        driverRepository = appContainer.driverRepository
                    )
                }
                SimulationMapScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    },
                    onNavigateToBuses = {
                        backStack.add(ShowroomRoute)
                    },
                    onNavigateToRoutes = {
                        backStack.add(RoutePlannerRoute)
                    }
                )
            }
            entry<ShowroomRoute> {
                val viewModel: ShowroomViewModel = viewModel {
                    ShowroomViewModel(
                        getCompanyUseCase = appContainer.getCompanyUseCase,
                        getShowroomBusesUseCase = appContainer.getShowroomBusesUseCase,
                        buyBusUseCase = appContainer.buyBusUseCase,
                        getOwnedFleetUseCase = appContainer.getOwnedFleetUseCase
                    )
                }
                ShowroomScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<RoutePlannerRoute> {
                val viewModel: RouteWizardViewModel = viewModel {
                    RouteWizardViewModel(
                        terminalRepository = appContainer.terminalRepository,
                        routeRepository = appContainer.routeRepository,
                        findAvailablePathsUseCase = appContainer.findAvailablePathsUseCase,
                        buildCustomRouteUseCase = appContainer.buildCustomRouteUseCase,
                        saveCustomRouteUseCase = appContainer.saveCustomRouteUseCase,
                        routePricingRepository = appContainer.routePricingRepository,
                        routeScheduleRepository = appContainer.routeScheduleRepository,
                        driverRepository = appContainer.driverRepository,
                        fleetRepository = appContainer.fleetRepository
                    )
                }
                RouteWizardScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<DriverManagementRoute> {
                val viewModel: DriverManagementViewModel = viewModel {
                    DriverManagementViewModel(
                        getAvailableDriversUseCase = appContainer.getAvailableDriversUseCase,
                        getHiredDriversUseCase = appContainer.getHiredDriversUseCase,
                        hireDriverUseCase = appContainer.hireDriverUseCase,
                        assignDriverToBusUseCase = appContainer.assignDriverToBusUseCase,
                        getOwnedFleetUseCase = appContainer.getOwnedFleetUseCase,
                        getCompanyUseCase = appContainer.getCompanyUseCase,
                        driverRepository = appContainer.driverRepository
                    )
                }
                DriverManagementScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<GarageRoute> {
                val viewModel: GarageViewModel = viewModel {
                    GarageViewModel(
                        fleetRepository = appContainer.fleetRepository,
                        driverRepository = appContainer.driverRepository,
                        routeRepository = appContainer.routeRepository,
                        routePricingRepository = appContainer.routePricingRepository,
                        companyRepository = appContainer.companyRepository
                    )
                }
                GarageScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    },
                    onNavigateToShowroom = {
                        backStack.add(ShowroomRoute)
                    },
                    onNavigateToRoutes = {
                        backStack.add(RoutePlannerRoute)
                    }
                )
            }
        }
    )
}
