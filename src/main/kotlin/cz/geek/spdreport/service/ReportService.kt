package cz.geek.spdreport.service

import cz.geek.spdreport.model.ReportData
import mu.KotlinLogging
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
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
        return create(resource, data)
    }

    fun create(source: Resource, data: ReportData): List<Report> {
        val holidays = holidayService.getHolidays(data.country, data.start, data.end)
        return calendarService.load(source, data.start, data.end)
            .map { LocalDateTimePair(it.start(), it.end()) }
            .flatMap { DateItemGenerator.generate(it.start, it.end, holidays) }
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
}

data class LocalDateTimePair(val start: LocalDateTime, val end: LocalDateTime)
