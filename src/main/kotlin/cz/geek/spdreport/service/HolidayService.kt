package cz.geek.spdreport.service

import cz.geek.spdreport.model.Country
import org.springframework.stereotype.Service
import java.net.URI
import java.net.URL
import java.time.LocalDate

@Service
class HolidayService(
    private val calendarService: CalendarService,
) {

    private val matcher = "Public holiday"

    fun getHolidays(country: Country, start: LocalDate, end: LocalDate): Set<LocalDate> {
        val calendarUrl = getCalendarUrl(country)
        return calendarService.load(calendarUrl, start, end)
            .filter { it.description.value == matcher }
            .map { it.start().toLocalDate() }
            .toSet()
    }

    private fun getCalendarUrl(country: Country): URL = URI("https://calendar.google.com/calendar/ical/en.${country.calendarName}%23holiday%40group.v.calendar.google.com/public/basic.ics").toURL()
}