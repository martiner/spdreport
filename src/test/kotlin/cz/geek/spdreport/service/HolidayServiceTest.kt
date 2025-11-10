package cz.geek.spdreport.service

import cz.geek.spdreport.model.Country
import cz.geek.spdreport.model.CountryHolidayConfig
import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import java.time.LocalDate

data class HolidayTestConfig(
    val country: Country,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val expectedHolidays: Set<LocalDate>
)

class HolidayServiceTest: FreeSpec() {
    init {

        val service = HolidayService(
            mapOf(
                Country.CZ to CountryHolidayConfig(javaClass.getResource("/holidays_cz.ics")!!, "Státní svátek"),
                Country.SK to CountryHolidayConfig(javaClass.getResource("/holidays_sk.ics")!!, "Public holiday")
            ),
            CalendarService()
        )

        withData(
            nameFn = { "Load holidays for country ${it.country.value}" },
            HolidayTestConfig(
                Country.CZ,
                LocalDate.of(2023, 5, 1),
                LocalDate.of(2023, 5, 31),
                setOf(
                    LocalDate.of(2023, 5, 1),
                    LocalDate.of(2023, 5, 8)
                )
            ),
            HolidayTestConfig(
                Country.SK,
                LocalDate.of(2023, 5, 1),
                LocalDate.of(2023, 5, 31),
                setOf(
                    LocalDate.of(2023, 5, 1),
                    LocalDate.of(2023, 5, 8)
                )
            )
        ) { (country, startDate, endDate, expectedHolidays) ->
            val holidays = service.getHolidays(country, startDate, endDate)
            holidays.shouldBe(expectedHolidays)
        }

    }
}