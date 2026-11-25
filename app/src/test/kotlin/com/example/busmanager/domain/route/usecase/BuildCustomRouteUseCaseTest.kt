package com.example.busmanager.domain.route.usecase

import com.example.busmanager.data.repository.RouteCatalogRepositoryImpl
import com.example.busmanager.data.repository.TerminalCatalogRepositoryImpl
import com.example.busmanager.domain.route.model.RoutePathType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BuildCustomRouteUseCaseTest {

    private lateinit var terminalRepository: TerminalCatalogRepositoryImpl
    private lateinit var routeRepository: RouteCatalogRepositoryImpl
    private lateinit var useCase: BuildCustomRouteUseCase

    @Before
    fun setUp() {
        terminalRepository = TerminalCatalogRepositoryImpl()
        routeRepository = RouteCatalogRepositoryImpl()
        useCase = BuildCustomRouteUseCase(routeRepository)
    }

    @Test
    fun invoke_singleTerminal_returnsFailure() {
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val result = useCase(listOf(purabaya))
        assertTrue(result.isFailure)
    }

    @Test
    fun invoke_unconnectedTerminals_returnsFailure() {
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val sritanjung = terminalRepository.getTerminalById("term_sritanjung")!!
        val result = useCase(listOf(purabaya, sritanjung))
        assertTrue(result.isFailure)
    }

    @Test
    fun invoke_validThreeTerminalsPath_buildsRouteSuccessfully() {
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val bayuangga = terminalRepository.getTerminalById("term_bayuangga")!!
        val tawangalun = terminalRepository.getTerminalById("term_tawangalun")!!

        val result = useCase(
            terminals = listOf(purabaya, bayuangga, tawangalun),
            preferredSegmentPathTypes = mapOf(
                Pair("term_purabaya", "term_bayuangga") to RoutePathType.TOLL
            ),
            customName = "Surabaya - Jember Executive"
        )

        assertTrue(result.isSuccess)
        val route = result.getOrNull()
        assertNotNull(route)
        assertEquals("Surabaya - Jember Executive", route?.name)
        assertEquals(purabaya.id, route?.originTerminal?.id)
        assertEquals(tawangalun.id, route?.destinationTerminal?.id)
        assertEquals(2, route?.segments?.size)
        assertEquals(RoutePathType.TOLL, route?.pathType)
        assertEquals(1, route?.transitTerminals?.size)
        assertEquals(bayuangga.id, route?.transitTerminals?.first()?.id)
    }

    @Test
    fun invoke_nonTollPreference_setsPathTypeNonToll() {
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val arjosari = terminalRepository.getTerminalById("term_arjosari")!!

        val result = useCase(
            terminals = listOf(purabaya, arjosari),
            preferredSegmentPathTypes = mapOf(
                Pair("term_purabaya", "term_arjosari") to RoutePathType.NON_TOLL
            )
        )

        assertTrue(result.isSuccess)
        val route = result.getOrNull()
        assertEquals(RoutePathType.NON_TOLL, route?.pathType)
    }

    @Test
    fun invoke_southernCorridor_buildsContinuousMultiStopRoute() {
        val tamanan = terminalRepository.getTerminalById("term_tamanan")!!
        val gayatri = terminalRepository.getTerminalById("term_gayatri")!!
        val patria = terminalRepository.getTerminalById("term_patria")!!
        val arjosari = terminalRepository.getTerminalById("term_arjosari")!!

        val result = useCase(
            terminals = listOf(tamanan, gayatri, patria, arjosari),
            customName = "Kediri - Malang Jalur Selatan"
        )

        assertTrue(result.isSuccess)
        val route = result.getOrNull()
        assertNotNull(route)
        assertEquals(tamanan.id, route?.originTerminal?.id)
        assertEquals(arjosari.id, route?.destinationTerminal?.id)
        assertEquals(2, route?.transitTerminals?.size)
        assertEquals(3, route?.segments?.size)
    }

    @Test
    fun invoke_expressMode_clearsTransitTerminalsEvenIfIntermediateExist() {
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val bayuangga = terminalRepository.getTerminalById("term_bayuangga")!!
        val tawangalun = terminalRepository.getTerminalById("term_tawangalun")!!

        val result = useCase(
            terminals = listOf(purabaya, bayuangga, tawangalun),
            isExpress = true
        )

        assertTrue(result.isSuccess)
        val route = result.getOrNull()
        assertNotNull(route)
        assertTrue(route!!.transitTerminals.isEmpty())
    }

    @Test
    fun invoke_selectiveTransitTerminals_includesOnlyActiveTransitTerminals() {
        val tamanan = terminalRepository.getTerminalById("term_tamanan")!!
        val gayatri = terminalRepository.getTerminalById("term_gayatri")!!
        val patria = terminalRepository.getTerminalById("term_patria")!!
        val arjosari = terminalRepository.getTerminalById("term_arjosari")!!

        val result = useCase(
            terminals = listOf(tamanan, gayatri, patria, arjosari),
            activeTransitTerminalIds = setOf(patria.id),
            isExpress = false
        )

        assertTrue(result.isSuccess)
        val route = result.getOrNull()
        assertNotNull(route)
        assertEquals(1, route!!.transitTerminals.size)
        assertEquals(patria.id, route.transitTerminals.first().id)
    }

    @Test
    fun invoke_tollVsNonTollToggles_adjustsTollFeeCorrectly() {
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val arjosari = terminalRepository.getTerminalById("term_arjosari")!!

        val tollResult = useCase(
            terminals = listOf(purabaya, arjosari),
            preferredSegmentPathTypes = mapOf(
                Pair(purabaya.id, arjosari.id) to RoutePathType.TOLL
            )
        )
        val nonTollResult = useCase(
            terminals = listOf(purabaya, arjosari),
            preferredSegmentPathTypes = mapOf(
                Pair(purabaya.id, arjosari.id) to RoutePathType.NON_TOLL
            )
        )

        assertTrue(tollResult.isSuccess)
        assertTrue(nonTollResult.isSuccess)

        val tollRoute = tollResult.getOrNull()!!
        val nonTollRoute = nonTollResult.getOrNull()!!

        assertTrue(tollRoute.totalTollFee.amount > 0L)
        assertEquals(0L, nonTollRoute.totalTollFee.amount)
    }

    @Test
    fun invoke_emptyTransitTerminalSet_clearsTransitTerminals() {
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val bayuangga = terminalRepository.getTerminalById("term_bayuangga")!!
        val tawangalun = terminalRepository.getTerminalById("term_tawangalun")!!

        val result = useCase(
            terminals = listOf(purabaya, bayuangga, tawangalun),
            activeTransitTerminalIds = emptySet()
        )

        assertTrue(result.isSuccess)
        val route = result.getOrNull()
        assertNotNull(route)
        assertTrue(route!!.transitTerminals.isEmpty())
        assertEquals("Surabaya - Jember", route.name)
    }

    @Test
    fun invoke_defaultNullTransitTerminalSet_includesAllIntermediateTerminals() {
        val purabaya = terminalRepository.getTerminalById("term_purabaya")!!
        val bayuangga = terminalRepository.getTerminalById("term_bayuangga")!!
        val tawangalun = terminalRepository.getTerminalById("term_tawangalun")!!

        val result = useCase(
            terminals = listOf(purabaya, bayuangga, tawangalun)
        )

        assertTrue(result.isSuccess)
        val route = result.getOrNull()
        assertNotNull(route)
        assertEquals(1, route!!.transitTerminals.size)
        assertEquals(bayuangga.id, route.transitTerminals.first().id)
        assertEquals("Surabaya - Jember (Via Probolinggo)", route.name)
    }
}
