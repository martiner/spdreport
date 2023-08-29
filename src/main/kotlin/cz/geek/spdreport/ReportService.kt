package cz.geek.spdreport

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
    @Value("\${holidays}") private val holidays: URL,
) {

    private val zone = ZoneId.of("Europe/Prague")

    fun create(data: ReportData, url: URL): List<Report> {
        logger.info { "Creating report for $data" }
        val source = url.readBytes()
        return create(data, source)
    }

    fun create(data: ReportData, source: ByteArray): List<Report> {
        val holidays = holidays(holidays.readBytes(), data)
        return create(source, data, holidays)
    }

    private fun holidays(source: ByteArray, data: ReportData): Set<LocalDate> =
        load(source, data.start, data.end)
            .filter { it.description.value == "Státní svátek" }
            .map { it.start().toLocalDate() }
            .toSet()

    fun create(source: ByteArray, data: ReportData, holidays: Set<LocalDate>): List<Report> =
        load(source, data.start, data.end)
            .flatMap { DatePairGenerator.generate(it.start(), it.end(), holidays) }
            .map { (start, end) -> Report(
                date = start.toLocalDate(),
                start = start.toLocalTime(),
                end = end,
                name = data.name,
                number = data.number
            ) }

    private fun load(source: ByteArray, start: LocalDate, end: LocalDate): List<VEvent> =
        load(source, start.atStartOfDay(), end.atTime(23, 59))

    private fun load(source: ByteArray, start: LocalDateTime, end: LocalDateTime): List<VEvent> =
        CalendarBuilder()
            .build(InputStreamReader(ByteArrayInputStream(source)))
            .components
            .filterIsInstance<VEvent>()
            .filter { it.start().isBefore(end) && it.end().isAfter(start) }

    private fun VEvent.start() = this.startDate.toLocal()

    private fun VEvent.end() = this.endDate.date.toInstant().toLocal()

    private fun DateProperty.toLocal() = this.date.toInstant().toLocal()

    private fun Instant.toLocal() = this.atZone(zone).toLocalDateTime()

}
