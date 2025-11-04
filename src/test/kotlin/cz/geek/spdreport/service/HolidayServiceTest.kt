package cz.geek.spdreport.service

import cz.geek.spdreport.model.Country
import cz.geek.spdreport.model.CountryHolidayConfig
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class HolidayServiceTest: FreeSpec() {
    init {

        val service = HolidayService(
            mapOf(Country.CZ to CountryHolidayConfig(javaClass.getResource("/holidays.ics")!!, "Státní svátek")),
            CalendarService()
        )

        "Should get holidays" {
            val holidays = service.getHolidays(Country.CZ, LocalDate.of(2023, 5, 1), LocalDate.of(2023, 5, 31))
            holidays.shouldBe(setOf(LocalDate.of(2023, 5, 1), LocalDate.of(2023, 5, 8)))
        }
    }
}