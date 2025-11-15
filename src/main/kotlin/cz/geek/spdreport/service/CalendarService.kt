package cz.geek.spdreport.service

import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.DateProperty
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.net.URL
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class CalendarService {

    fun load(source: URL, start: LocalDate, end: LocalDate): List<VEvent> =
        load(source, start.atStartOfDay(), end.atTime(23, 59))

    fun load(source: URL, start: LocalDateTime, end: LocalDateTime): List<VEvent> =
        CalendarBuilder()
            .build(InputStreamReader(ByteArrayInputStream(source.readBytes())))
            .components
            .filterIsInstance<VEvent>()
            .filter { it.start().isBefore(end) && it.end().isAfter(start) }

}

private val zone = ZoneId.of("Europe/Prague")

fun VEvent.start(): LocalDateTime = this.startDate.toLocal()

fun VEvent.end(): LocalDateTime = this.endDate.date.toInstant().toLocal()

fun DateProperty.toLocal(): LocalDateTime = this.date.toInstant().toLocal()

fun Instant.toLocal(): LocalDateTime = this.atZone(zone).toLocalDateTime()