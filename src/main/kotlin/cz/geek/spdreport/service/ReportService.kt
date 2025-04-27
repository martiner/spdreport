package cz.geek.spdreport.service

import cz.geek.spdreport.model.ReportData
import mu.KotlinLogging
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.component.VEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.net.URL
import java.time.ZoneId
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

private val logger = KotlinLogging.logger {}

@Service
class ReportService(
    @Value("\${holidays}") private val holidays: URL,
) {

    private val zone = ZoneId.of("Europe/Prague")

    fun create(data: ReportData): List<Report> {
        val resource = data.source()?.resource ?: return emptyList()
        logger.info { "Creating report for $data" }
        val holidays = holidays(holidays.readBytes(), data)
        return create(resource.contentAsByteArray, data, holidays)
    }

    private fun holidays(source: ByteArray, data: ReportData): Set<LocalDate> =
        load(source, data.start, data.end)
            .filter { it.description.value == "Státní svátek" }
            .map { it.start().toLocalDate() }
            .toSet()

    fun create(source: ByteArray, data: ReportData, holidays: Set<LocalDate>): List<Report> =
        load(source, data.start, data.end)
            .flatMap { DateItemGenerator.generate(it.start(), it.end(), holidays) }
            .map { (day, start, end) -> Report(
                date = day,
                start = start,
                end = end,
                name = data.name,
                number = data.number
            ) }

    private fun load(source: ByteArray, start: LocalDate, end: LocalDate): List<VEvent> =
        load(source, start.atStartOfDay(), end.atTime(23, 59))

    private fun load(source: ByteArray, start: LocalDateTime, end: LocalDateTime): List<VEvent> =
        CalendarBuilder()
            .build(InputStreamReader(ByteArrayInputStream(source)))
            .componentList
            .all
            .filterIsInstance<VEvent>()
            .filter { it.start().isBefore(end) && it.end().isAfter(start) }

    private fun VEvent.start(): LocalDateTime = getDateTimeStart<OffsetDateTime>().date.toLocalDateTime()

    private fun VEvent.end(): LocalDateTime = getDateTimeEnd<OffsetDateTime>().date.toLocalDateTime()

}
