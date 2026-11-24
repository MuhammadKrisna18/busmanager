package com.example.busmanager.data.local.datasource

import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.route.model.MapCoordinate
import com.example.busmanager.domain.route.model.Terminal
import com.example.busmanager.domain.route.model.TerminalType

class TerminalDataSource {

    private val purabaya = Terminal(
        id = "term_purabaya",
        name = "Terminal Purabaya",
        city = "Surabaya",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(
            x = 0.18f,
            y = 0.16f,
            latitude = -7.3523,
            longitude = 112.7262
        ),
        retributionFee = Money.rupiah(5000),
        passengerDemandMultiplier = 1.6f
    )

    private val arjosari = Terminal(
        id = "term_arjosari",
        name = "Terminal Arjosari",
        city = "Malang",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(
            x = 0.14f,
            y = 0.52f,
            latitude = -7.9315,
            longitude = 112.6582
        ),
        retributionFee = Money.rupiah(4000),
        passengerDemandMultiplier = 1.3f
    )

    private val bayuangga = Terminal(
        id = "term_bayuangga",
        name = "Terminal Bayuangga",
        city = "Probolinggo",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(
            x = 0.44f,
            y = 0.41f,
            latitude = -7.7655,
            longitude = 113.1952
        ),
        retributionFee = Money.rupiah(3000),
        passengerDemandMultiplier = 1.2f
    )

    private val tawangAlun = Terminal(
        id = "term_tawangalun",
        name = "Terminal Tawang Alun",
        city = "Jember",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(
            x = 0.68f,
            y = 0.63f,
            latitude = -8.1884,
            longitude = 113.6391
        ),
        retributionFee = Money.rupiah(3500),
        passengerDemandMultiplier = 1.25f
    )

    private val situbondo = Terminal(
        id = "term_situbondo",
        name = "Terminal Situbondo",
        city = "Situbondo",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(
            x = 0.77f,
            y = 0.38f,
            latitude = -7.7051,
            longitude = 114.0044
        ),
        retributionFee = Money.rupiah(3000),
        passengerDemandMultiplier = 1.1f
    )

    private val sritanjung = Terminal(
        id = "term_sritanjung",
        name = "Terminal Sritanjung",
        city = "Banyuwangi",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(
            x = 0.92f,
            y = 0.65f,
            latitude = -8.1342,
            longitude = 114.3912
        ),
        retributionFee = Money.rupiah(4000),
        passengerDemandMultiplier = 1.4f
    )

    private val kertajaya = Terminal(
        id = "term_kertajaya",
        name = "Terminal Kertajaya",
        city = "Mojokerto",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(
            x = 0.11f,
            y = 0.25f,
            latitude = -7.4725,
            longitude = 112.4411
        ),
        retributionFee = Money.rupiah(3500),
        passengerDemandMultiplier = 1.25f
    )

    private val tamanan = Terminal(
        id = "term_tamanan",
        name = "Terminal Tamanan",
        city = "Kediri",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(
            x = 0.05f,
            y = 0.44f,
            latitude = -7.8286,
            longitude = 111.9961
        ),
        retributionFee = Money.rupiah(3500),
        passengerDemandMultiplier = 1.3f
    )

    private val gayatri = Terminal(
        id = "term_gayatri",
        name = "Terminal Gayatri",
        city = "Tulungagung",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(
            x = 0.04f,
            y = 0.62f,
            latitude = -8.0645,
            longitude = 111.9056
        ),
        retributionFee = Money.rupiah(3000),
        passengerDemandMultiplier = 1.2f
    )

    private val patria = Terminal(
        id = "term_patria",
        name = "Terminal Patria",
        city = "Blitar",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(
            x = 0.09f,
            y = 0.64f,
            latitude = -8.0983,
            longitude = 112.1608
        ),
        retributionFee = Money.rupiah(3500),
        passengerDemandMultiplier = 1.25f
    )

    fun getTerminals(): List<Terminal> = listOf(
        purabaya,
        arjosari,
        bayuangga,
        tawangAlun,
        situbondo,
        sritanjung,
        kertajaya,
        tamanan,
        gayatri,
        patria
    )

    fun getTerminalById(id: String): Terminal? {
        return getTerminals().find { it.id == id }
    }
}
