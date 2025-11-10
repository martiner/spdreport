package cz.geek.spdreport.service

import cz.geek.spdreport.model.ReportData
import mu.KotlinLogging
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.DateProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDate
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
class ReportService(
    val holidayService: HolidayService,
    val calendarService: CalendarService
) {

    fun create(data: ReportData): List<Report> {
        val resource = data.source()?.resource ?: return emptyList()
        logger.info { "Creating report for $data" }
        val holidays = holidayService.getHolidays(data.country, data.start, data.end)
        return create(resource.url, data, holidays)
    }

    fun create(source: URL, data: ReportData, holidays: Set<LocalDate>): List<Report> =
        calendarService.load(source, data.start, data.end)
            .flatMap { DateItemGenerator.generate(it.start(), it.end(), holidays) }
            .map { (day, start, end) ->
                Report(
                    date = day,
                    start = start,
                    end = end,
                    name = data.name,
                    number = data.number,
                    country = data.country,
                )
            }

}
