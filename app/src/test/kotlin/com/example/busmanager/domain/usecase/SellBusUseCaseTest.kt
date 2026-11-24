package com.example.busmanager.domain.usecase

import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.driver.model.Driver
import com.example.busmanager.domain.driver.repository.DriverRepository
import com.example.busmanager.domain.fleet.model.AxleType
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.model.BusBodyType
import com.example.busmanager.domain.fleet.model.BusClass
import com.example.busmanager.domain.fleet.model.Chassis
import com.example.busmanager.domain.fleet.model.ChassisBrand
import com.example.busmanager.domain.fleet.model.EngineLocation
import com.example.busmanager.domain.fleet.model.KaroseriBody
import com.example.busmanager.domain.fleet.model.KaroseriBrand
import com.example.busmanager.domain.fleet.model.SuspensionType
import com.example.busmanager.domain.fleet.model.TransmissionType
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.fleet.usecase.SellBusResult
import com.example.busmanager.domain.fleet.usecase.SellBusUseCase
import com.example.busmanager.domain.route.model.RouteSchedule
import com.example.busmanager.domain.route.repository.RouteScheduleRepository
import com.example.busmanager.domain.simulation.model.ActiveBusTrip
import com.example.busmanager.domain.simulation.model.GameCalendar
import com.example.busmanager.domain.simulation.model.RouteDirection
import com.example.busmanager.domain.simulation.repository.SimulationTripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SellBusUseCaseTest {

    private lateinit var fakeFleetRepo: FakeFleetRepository
    private lateinit var fakeCompanyRepo: FakeCompanyRepository
    private lateinit var fakeDriverRepo: FakeDriverRepository
    private lateinit var fakeScheduleRepo: FakeRouteScheduleRepository
    private lateinit var fakeTripRepo: FakeSimulationTripRepository
    private lateinit var sellBusUseCase: SellBusUseCase

    private fun createDummyBus(
        id: String = "bus-1",
        busCode: String = "MK01",
        driverId: String? = null,
        routeId: String? = null,
        isOperating: Boolean = false
    ): Bus {
        val chassis = Chassis(
            id = "chassis-1",
            modelName = "Hino RK8",
            brand = ChassisBrand.HINO,
            engineLocation = EngineLocation.REAR,
            suspension = SuspensionType.LEAF_SPRING,
            axleType = AxleType.SINGLE_4X2,
            transmission = TransmissionType.MANUAL,
            horsepower = 260,
            torqueNm = 750,
            fuelCapacityLiters = 270,
            fuelConsumptionKmPerLiter = 3.5f,
            price = Money.rupiah(1_000_000_000L)
        )
        val body = KaroseriBody(
            id = "body-1",
            modelName = "Jetbus 3+ SHD",
            brand = KaroseriBrand.ADIPUTRO,
            bodyType = BusBodyType.SHD,
            busClass = BusClass.EKSEKUTIF,
            passengerCapacity = 32,
            luggageCapacityKg = 1500,
            comfortRating = 8,
            price = Money.rupiah(500_000_000L)
        )
        return Bus(
            id = id,
            busCode = busCode,
            chassis = chassis,
            body = body,
            driverId = driverId,
            routeId = routeId,
            isOperating = isOperating
        )
    }

    @Before
    fun setUp() {
        fakeFleetRepo = FakeFleetRepository()
        fakeCompanyRepo = FakeCompanyRepository()
        fakeDriverRepo = FakeDriverRepository()
        fakeScheduleRepo = FakeRouteScheduleRepository()
        fakeTripRepo = FakeSimulationTripRepository()

        sellBusUseCase = SellBusUseCase(
            fleetRepository = fakeFleetRepo,
            companyRepository = fakeCompanyRepo,
            driverRepository = fakeDriverRepo,
            routeScheduleRepository = fakeScheduleRepo,
            simulationTripRepository = fakeTripRepo
        )
    }

    @Test
    fun sellBus_failsWhenBusIsOperating() = runBlocking {
        val bus = createDummyBus(isOperating = true)
        fakeFleetRepo.addBus(bus)

        val result = sellBusUseCase(bus.id)
        assertTrue(result is SellBusResult.BusOperatingError)
        assertEquals(1, fakeFleetRepo.buses.size)
    }

    @Test
    fun sellBus_failsWhenBusNotFound() = runBlocking {
        val result = sellBusUseCase("non-existent-id")
        assertTrue(result is SellBusResult.BusNotFound)
    }

    @Test
    fun sellBus_succeedsWhenStandbyAndReleasesDriverAndSchedule() = runBlocking {
        val bus = createDummyBus(driverId = "driver-1", routeId = "route-1")
        fakeFleetRepo.addBus(bus)

        val result = sellBusUseCase(bus.id)
        assertTrue(result is SellBusResult.Success)

        val success = result as SellBusResult.Success
        val expectedRefund = Money.rupiah((bus.totalPrice.amount * 0.70).toLong())
        assertEquals(expectedRefund, success.refundAmount)
        assertEquals(0, fakeFleetRepo.buses.size)
        assertTrue(fakeDriverRepo.unassignedDriverIds.contains("driver-1"))
        assertEquals(expectedRefund, fakeCompanyRepo.currentBalance)
    }

    private class FakeFleetRepository : FleetRepository {
        val buses = mutableListOf<Bus>()

        override fun getOwnedBuses(): Flow<List<Bus>> = MutableStateFlow(buses.toList())
        override suspend fun getBusById(id: String): Bus? = buses.find { it.id == id }
        override suspend fun addBus(bus: Bus) {
            buses.add(bus)
        }
        override suspend fun updateBus(bus: Bus) {}
        override suspend fun removeBus(busId: String) {
            buses.removeAll { it.id == busId }
        }
    }

    private class FakeCompanyRepository : CompanyRepository {
        var currentBalance = Money.ZERO

        override fun getCompany(): Flow<Company?> = MutableStateFlow(null)
        override suspend fun createCompany(name: String, initialBalance: Money): Company {
            currentBalance = initialBalance
            return Company("comp-1", name, initialBalance, 100)
        }
        override suspend fun updateBalance(newBalance: Money) {
            currentBalance = newBalance
        }
        override suspend fun deductBalance(amount: Money): Boolean = true
        override suspend fun addBalance(amount: Money) {
            currentBalance = currentBalance + amount
        }
    }

    private class FakeDriverRepository : DriverRepository {
        val unassignedDriverIds = mutableListOf<String>()

        override fun getAllDrivers(): Flow<List<Driver>> = MutableStateFlow(emptyList())
        override fun getHiredDrivers(): Flow<List<Driver>> = MutableStateFlow(emptyList())
        override suspend fun getDriverById(id: String): Driver? = null
        override suspend fun hireDriver(driverId: String): Boolean = true
        override suspend fun assignDriverToBus(driverId: String, busId: String): Boolean = true
        override suspend fun unassignDriver(driverId: String) {
            unassignedDriverIds.add(driverId)
        }
        override suspend fun fireDriver(driverId: String) {}
        override suspend fun seedCandidateDriversIfEmpty() {}
    }

    private class FakeRouteScheduleRepository : RouteScheduleRepository {
        val deletedScheduleIds = mutableListOf<String>()

        override fun getSchedulesForRoute(routeId: String) = MutableStateFlow(emptyList<RouteSchedule>())
        override fun getAllSchedules() = MutableStateFlow(emptyList<RouteSchedule>())
        override suspend fun getSchedulesListForRoute(routeId: String) = emptyList<RouteSchedule>()
        override suspend fun getScheduleById(id: String) = null
        override suspend fun addSchedule(schedule: RouteSchedule) = Result.success(Unit)
        override suspend fun updateSchedule(schedule: RouteSchedule) = Result.success(Unit)
        override suspend fun assignBusToSchedule(scheduleId: String, busId: String) = true
        override suspend fun unassignBusFromSchedule(scheduleId: String) {}
        override suspend fun deleteSchedule(scheduleId: String) {
            deletedScheduleIds.add(scheduleId)
        }
        override suspend fun checkConflict(routeId: String, direction: RouteDirection, hour: Int, minute: Int, excludeScheduleId: String?) = false
    }

    private class FakeSimulationTripRepository : SimulationTripRepository {
        override val activeTrips: StateFlow<List<ActiveBusTrip>> = MutableStateFlow(emptyList())

        override fun dispatchBus(bus: Bus, route: com.example.busmanager.domain.route.model.BusRoute, direction: RouteDirection, currentTime: GameCalendar): ActiveBusTrip {
            error("Not implemented")
        }
        override fun advanceTripsTime(advanceMinutes: Int): List<ActiveBusTrip> = emptyList()
        override fun cancelTrip(tripId: String) {}
        override fun markCancelNextSchedule(tripId: String, cancel: Boolean) {}
        override fun completeTrip(tripId: String) {}
        override fun clearTrips() {}
    }
}
