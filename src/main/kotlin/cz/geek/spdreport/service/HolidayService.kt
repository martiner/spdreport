package cz.geek.spdreport.service

import cz.geek.spdreport.model.Country
import cz.geek.spdreport.model.CountryHolidayConfig
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class HolidayService(
    private val countryHolidays: Map<Country, CountryHolidayConfig>,
    private val calendarService: CalendarService,
) {

    fun getHolidays(country: Country, start: LocalDate, end: LocalDate): Set<LocalDate> {
        val config = countryHolidays[country] ?: return emptySet()
        return calendarService.load(config.calendar.readBytes(), start, end)
            .filter { it.description.value == config.matcher }
            .map { it.start().toLocalDate() }
            .toSet()
    }
}