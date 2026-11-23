package com.example.busmanager.domain.usecase

import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.company.repository.CompanyRepository
import com.example.busmanager.domain.fleet.model.AxleType
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.fleet.model.BusBodyType
import com.example.busmanager.domain.fleet.model.BusClass
import com.example.busmanager.domain.fleet.model.BuyBusResult
import com.example.busmanager.domain.fleet.model.Chassis
import com.example.busmanager.domain.fleet.model.ChassisBrand
import com.example.busmanager.domain.fleet.model.EngineLocation
import com.example.busmanager.domain.fleet.model.KaroseriBody
import com.example.busmanager.domain.fleet.model.KaroseriBrand
import com.example.busmanager.domain.fleet.model.SuspensionType
import com.example.busmanager.domain.fleet.model.TransmissionType
import com.example.busmanager.domain.fleet.repository.FleetRepository
import com.example.busmanager.domain.fleet.usecase.BuyBusUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BuyBusUseCaseTest {

    private lateinit var fakeCompanyRepo: FakeCompanyRepository
    private lateinit var fakeFleetRepo: FakeFleetRepository
    private lateinit var buyBusUseCase: BuyBusUseCase

    @Before
    fun setUp() {
        fakeCompanyRepo = FakeCompanyRepository()
        fakeFleetRepo = FakeFleetRepository()
        buyBusUseCase = BuyBusUseCase(fakeCompanyRepo, fakeFleetRepo)
    }

    private fun createSampleBus(totalPrice: Money): Bus {
        val chassis = Chassis(
            id = "c-1",
            modelName = "RK8",
            brand = ChassisBrand.HINO,
            engineLocation = EngineLocation.REAR,
            suspension = SuspensionType.LEAF_SPRING,
            axleType = AxleType.SINGLE_4X2,
            transmission = TransmissionType.MANUAL,
            horsepower = 260,
            torqueNm = 750,
            fuelCapacityLiters = 270,
            fuelConsumptionKmPerLiter = 3.2f,
            price = totalPrice
        )
        val body = KaroseriBody(
            id = "b-1",
            modelName = "HDD",
            brand = KaroseriBrand.ADIPUTRO,
            bodyType = BusBodyType.HDD,
            busClass = BusClass.BISNIS,
            passengerCapacity = 40,
            luggageCapacityKg = 1800,
            comfortRating = 80,
            price = Money.ZERO
        )
        return Bus(
            id = "bus-1",
            plateNumber = "B 1234 CD",
            nickname = "Bima",
            chassis = chassis,
            body = body
        )
    }

    @Test
    fun buyBusSucceedsWhenFundsAreSufficient() = runBlocking {
        fakeCompanyRepo.createCompany("PO Sinar Jaya", Money.rupiah(1_500_000_000))
        val bus = createSampleBus(Money.rupiah(1_200_000_000))

        val result = buyBusUseCase(bus)

        assertTrue(result is BuyBusResult.Success)
        val remaining = fakeCompanyRepo.getCompany().first()?.balance
        assertEquals(Money.rupiah(300_000_000), remaining)
        assertEquals(1, fakeFleetRepo.getOwnedBuses().first().size)
    }

    @Test
    fun buyBusFailsWhenFundsAreInsufficient() = runBlocking {
        fakeCompanyRepo.createCompany("PO Sinar Jaya", Money.rupiah(500_000_000))
        val bus = createSampleBus(Money.rupiah(1_200_000_000))

        val result = buyBusUseCase(bus)

        assertTrue(result is BuyBusResult.InsufficientFunds)
        val remaining = fakeCompanyRepo.getCompany().first()?.balance
        assertEquals(Money.rupiah(500_000_000), remaining)
        assertTrue(fakeFleetRepo.getOwnedBuses().first().isEmpty())
    }

    @Test
    fun buyBusReturnsCompanyNotFoundWhenNoCompanyExists() = runBlocking {
        val bus = createSampleBus(Money.rupiah(500_000_000))

        val result = buyBusUseCase(bus)

        assertTrue(result is BuyBusResult.CompanyNotFound)
    }

    private class FakeCompanyRepository : CompanyRepository {
        private val state = MutableStateFlow<Company?>(null)

        override fun getCompany(): Flow<Company?> = state

        override suspend fun createCompany(name: String, initialBalance: Money): Company {
            val created = Company(id = "comp-1", name = name, balance = initialBalance)
            state.value = created
            return created
        }

        override suspend fun updateBalance(newBalance: Money) {
            state.value = state.value?.copy(balance = newBalance)
        }

        override suspend fun deductBalance(amount: Money): Boolean {
            val current = state.value ?: return false
            if (!current.balance.canAfford(amount)) return false
            state.value = current.copy(balance = current.balance - amount)
            return true
        }

        override suspend fun addBalance(amount: Money) {
            val current = state.value ?: return
            state.value = current.copy(balance = current.balance + amount)
        }
    }

    private class FakeFleetRepository : FleetRepository {
        private val list = mutableListOf<Bus>()
        private val state = MutableStateFlow<List<Bus>>(emptyList())

        override fun getOwnedBuses(): Flow<List<Bus>> = state

        override suspend fun getBusById(id: String): Bus? = list.find { it.id == id }

        override suspend fun addBus(bus: Bus) {
            list.add(bus)
            state.value = list.toList()
        }

        override suspend fun updateBus(bus: Bus) {
            val index = list.indexOfFirst { it.id == bus.id }
            if (index != -1) {
                list[index] = bus
                state.value = list.toList()
            }
        }

        override suspend fun removeBus(busId: String) {
            list.removeAll { it.id == busId }
            state.value = list.toList()
        }
    }
}
