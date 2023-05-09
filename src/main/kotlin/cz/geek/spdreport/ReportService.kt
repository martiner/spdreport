package cz.geek.spdreport

import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.DateProperty
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.net.URL
import java.time.Instant
import java.time.ZoneId

@Service
class ReportService {

    private val zone = ZoneId.of("Europe/Prague")

    fun create(data: ReportData, url: URL): List<Report> {
        val source = url.readBytes()
        return create(source, data)
    }

    fun create(source: ByteArray, data: ReportData): List<Report> =
        CalendarBuilder()
            .build(InputStreamReader(ByteArrayInputStream(source)))
            .components
            .filterIsInstance<VEvent>()
            .filter { it.start().isBefore(data.end()) && it.end().isAfter(data.start()) }
            .map { Report(
                date = it.start().toLocalDate(),
                start = it.start().toLocalTime(),
                end = it.start().toLocalTime(),
                name = data.name,
                number = data.number
            ) }


    private fun VEvent.start() = this.startDate.toLocal()

    private fun VEvent.end() = this.endDate.date.toInstant().toLocal()

    private fun DateProperty.toLocal() = this.date.toInstant().toLocal()

    private fun Instant.toLocal() = this.atZone(zone).toLocalDateTime()

}
