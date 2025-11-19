package cz.geek.spdreport.service

import cz.geek.spdreport.model.Country
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class HolidayService(
    private val calendarService: CalendarService,
) {

    private val matcher = "Public holiday"

    fun getHolidays(country: Country, start: LocalDate, end: LocalDate): Set<LocalDate> {
        val calendarResource = getCalendarUrl(country)
        return calendarService.load(calendarResource, start, end)
            .filter { it.description.value == matcher }
            .map { it.start().toLocalDate() }
            .toSet()
    }

    private fun getCalendarUrl(country: Country): Resource =
        UrlResource("https://calendar.google.com/calendar/ical/en.${country.calendarName}%23holiday%40group.v.calendar.google.com/public/basic.ics")
}