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
            x = 0.46f,
            y = 0.28f,
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
            x = 0.45f,
            y = 0.64f,
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
            x = 0.60f,
            y = 0.54f,
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
            x = 0.73f,
            y = 0.81f,
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
            x = 0.83f,
            y = 0.50f,
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
            x = 0.94f,
            y = 0.77f,
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
            x = 0.38f,
            y = 0.36f,
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
            x = 0.26f,
            y = 0.58f,
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
            x = 0.23f,
            y = 0.73f,
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
            x = 0.30f,
            y = 0.75f,
            latitude = -8.0983,
            longitude = 112.1608
        ),
        retributionFee = Money.rupiah(3500),
        passengerDemandMultiplier = 1.25f
    )

    private val surodakan = Terminal(
        id = "term_surodakan",
        name = "Terminal Surodakan",
        city = "Trenggalek",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(
            x = 0.18f,
            y = 0.72f,
            latitude = -8.0580,
            longitude = 111.7160
        ),
        retributionFee = Money.rupiah(3000),
        passengerDemandMultiplier = 1.15f
    )

    private val seloaji = Terminal(
        id = "term_seloaji",
        name = "Terminal Seloaji",
        city = "Ponorogo",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(
            x = 0.11f,
            y = 0.59f,
            latitude = -7.8420,
            longitude = 111.4780
        ),
        retributionFee = Money.rupiah(3500),
        passengerDemandMultiplier = 1.25f
    )

    private val purboyo = Terminal(
        id = "term_purboyo",
        name = "Terminal Purboyo",
        city = "Madiun",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(
            x = 0.12f,
            y = 0.44f,
            latitude = -7.6080,
            longitude = 111.5280
        ),
        retributionFee = Money.rupiah(4000),
        passengerDemandMultiplier = 1.35f
    )

    private val kertonegoro = Terminal(
        id = "term_kertonegoro",
        name = "Terminal Kertonegoro",
        city = "Ngawi",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(
            x = 0.10f,
            y = 0.32f,
            latitude = -7.4110,
            longitude = 111.4350
        ),
        retributionFee = Money.rupiah(3500),
        passengerDemandMultiplier = 1.3f
    )

    private val anjukLadang = Terminal(
        id = "term_anjukladang",
        name = "Terminal Anjuk Ladang",
        city = "Nganjuk",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(
            x = 0.23f,
            y = 0.44f,
            latitude = -7.6020,
            longitude = 111.9120
        ),
        retributionFee = Money.rupiah(3500),
        passengerDemandMultiplier = 1.25f
    )

    private val rajekwesi = Terminal(
        id = "term_rajekwesi",
        name = "Terminal Rajekwesi",
        city = "Bojonegoro",
        type = TerminalType.TIPE_A,
        coordinate = MapCoordinate(
            x = 0.23f,
            y = 0.16f,
            latitude = -7.1620,
            longitude = 111.8900
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
        patria,
        surodakan,
        seloaji,
        purboyo,
        kertonegoro,
        anjukLadang,
        rajekwesi
    )

    fun getTerminalById(id: String): Terminal? {
        return getTerminals().find { it.id == id }
    }
}
