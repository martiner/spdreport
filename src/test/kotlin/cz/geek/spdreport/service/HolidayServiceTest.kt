package cz.geek.spdreport.service

import cz.geek.spdreport.model.Country
import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import java.net.URL
import java.time.LocalDate

data class HolidayTestConfig(
    val country: Country,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val expectedHolidays: Set<LocalDate>
)

class HolidayServiceTest: FreeSpec() {
    init {

        val calendarService = mockk<CalendarService>()
        val realCalendarService = CalendarService()
        
        // Mock the load method to return events from local test resources
        every { calendarService.load(any<Resource>(), any<LocalDate>(), any<LocalDate>()) } answers {
            val url = firstArg<Resource>()
            val start = secondArg<LocalDate>()
            val end = thirdArg<LocalDate>()
            
            // Determine which test resource file to use based on URL
            val resourcePath = when {
                url.toString().contains("en.czech") -> "/holidays_cz.ics"
                url.toString().contains("en.slovak") -> "/holidays_sk.ics"
                else -> throw IllegalArgumentException("Unknown calendar URL: $url")
            }
            
            // Load from test resources using the real CalendarService
            realCalendarService.load(ClassPathResource(resourcePath), start, end)
        }

        val service = HolidayService(calendarService)

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