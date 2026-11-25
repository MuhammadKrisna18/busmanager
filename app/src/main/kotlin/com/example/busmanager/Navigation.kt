package com.example.busmanager

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.busmanager.di.AppContainer
import com.example.busmanager.presentation.common.AppTransitions
import com.example.busmanager.presentation.dashboard.HomeScreen
import com.example.busmanager.presentation.dashboard.HomeScreenViewModel
import com.example.busmanager.presentation.driver.DriverManagementScreen
import com.example.busmanager.presentation.driver.DriverManagementViewModel
import com.example.busmanager.presentation.finance.FinanceScreen
import com.example.busmanager.presentation.finance.FinanceViewModel
import com.example.busmanager.presentation.garage.GarageScreen
import com.example.busmanager.presentation.garage.GarageViewModel
import com.example.busmanager.presentation.route.info.RouteInfoScreen
import com.example.busmanager.presentation.route.info.RouteInfoViewModel
import com.example.busmanager.presentation.route.wizard.RouteWizardScreen
import com.example.busmanager.presentation.route.wizard.RouteWizardViewModel
import com.example.busmanager.presentation.showroom.ShowroomScreen
import com.example.busmanager.presentation.showroom.ShowroomViewModel
import com.example.busmanager.presentation.simulation.SimulationMapScreen
import com.example.busmanager.presentation.simulation.SimulationMapViewModel
import com.example.busmanager.presentation.splash.SplashScreen
import com.example.busmanager.presentation.terminal.TerminalReportScreen
import com.example.busmanager.presentation.terminal.TerminalReportViewModel

@Composable
fun MainNavigation(appContainer: AppContainer) {
    val backStack = rememberNavBackStack(SplashRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = { AppTransitions.contentTransform() },
        popTransitionSpec = { AppTransitions.contentTransform() },
        predictivePopTransitionSpec = { AppTransitions.contentTransform() },
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
                        appUpdateManager = appContainer.appUpdateManager,
                        resetGameUseCase = appContainer.resetGameUseCase,
                        getRouteOperationalStatusUseCase = appContainer.getRouteOperationalStatusUseCase,
                        processOfflineProgressionUseCase = appContainer.processOfflineProgressionUseCase,
                        reconcileBusOperatingStateUseCase = appContainer.reconcileBusOperatingStateUseCase,
                        getSimulationTimeUseCase = appContainer.getSimulationTimeUseCase
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
                    },
                    onNavigateToFinance = {
                        backStack.add(FinanceRoute)
                    },
                    onNavigateToTerminalReport = {
                        backStack.add(TerminalReportRoute)
                    },
                    onNavigateToRouteInfo = {
                        backStack.add(RouteInfoRoute)
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
                        driverRepository = appContainer.driverRepository,
                        setSimulationSpeedUseCase = appContainer.setSimulationSpeedUseCase,
                        recordFinancialTransactionUseCase = appContainer.recordFinancialTransactionUseCase,
                        terminalPassengerRepository = appContainer.terminalPassengerRepository,
                        fleetRepository = appContainer.fleetRepository
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
                    },
                    onNavigateToRouteInfo = {
                        backStack.add(RouteInfoRoute)
                    }
                )
            }
            entry<ShowroomRoute> {
                val viewModel: ShowroomViewModel = viewModel {
                    ShowroomViewModel(
                        getCompanyUseCase = appContainer.getCompanyUseCase,
                        buyBusUseCase = appContainer.buyBusUseCase,
                        getOwnedFleetUseCase = appContainer.getOwnedFleetUseCase,
                        chassisCatalogRepository = appContainer.chassisCatalogRepository,
                        karoseriCatalogRepository = appContainer.karoseriCatalogRepository,
                        financialTransactionRepository = appContainer.financialTransactionRepository,
                        getShowroomBusesUseCase = appContainer.getShowroomBusesUseCase
                    )
                }
                ShowroomScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    },
                    onNavigateToGarage = {
                        backStack.removeLastOrNull()
                        backStack.add(GarageRoute)
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
                        companyRepository = appContainer.companyRepository,
                        getActiveTripsUseCase = appContainer.getActiveTripsUseCase,
                        sellBusUseCase = appContainer.sellBusUseCase,
                        reconcileBusOperatingStateUseCase = appContainer.reconcileBusOperatingStateUseCase,
                        routeScheduleRepository = appContainer.routeScheduleRepository,
                        assignBusToActiveRouteUseCase = appContainer.assignBusToActiveRouteUseCase,
                        validateScheduleConflictUseCase = appContainer.validateScheduleConflictUseCase,
                        getRouteOperationalStatusUseCase = appContainer.getRouteOperationalStatusUseCase
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
            entry<FinanceRoute> {
                val viewModel: FinanceViewModel = viewModel {
                    FinanceViewModel(
                        companyRepository = appContainer.companyRepository,
                        getFinancialTransactionsUseCase = appContainer.getFinancialTransactionsUseCase,
                        fleetRepository = appContainer.fleetRepository,
                        routeRepository = appContainer.routeRepository
                    )
                }
                FinanceScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<TerminalReportRoute> {
                val viewModel: TerminalReportViewModel = viewModel {
                    TerminalReportViewModel(
                        terminalRepository = appContainer.terminalRepository,
                        terminalPassengerRepository = appContainer.terminalPassengerRepository,
                        simulationTimeRepository = appContainer.simulationTimeRepository,
                        getRouteOperationalStatusUseCase = appContainer.getRouteOperationalStatusUseCase
                    )
                }
                TerminalReportScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<RouteInfoRoute> {
                val viewModel: RouteInfoViewModel = viewModel {
                    RouteInfoViewModel(
                        getRouteOperationalStatusUseCase = appContainer.getRouteOperationalStatusUseCase,
                        routePricingRepository = appContainer.routePricingRepository,
                        fleetRepository = appContainer.fleetRepository,
                        driverRepository = appContainer.driverRepository,
                        routeScheduleRepository = appContainer.routeScheduleRepository
                    )
                }
                RouteInfoScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    },
                    onNavigateToNewRoute = {
                        backStack.add(RoutePlannerRoute)
                    }
                )
            }
        }
    )
}
