package cz.geek.spdreport.service

import cz.geek.spdreport.model.ReportData
import mu.KotlinLogging
import net.fortuna.ical4j.data.CalendarBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

private val logger = KotlinLogging.logger {}

@Service
class ReportService(
    @Value("\${holidays}") private val holidays: URL,
) {
    private val zone = ZoneId.of("Europe/Prague")
    private val datePattern = Pattern.compile("(\\d{8})(T(\\d{6}))?.*") // Matches dates like 20230430T080000

    fun create(data: ReportData): List<Report> {
        val resource = data.source()?.resource ?: return emptyList()
        logger.info { "Creating report for $data" }
        val holidays = holidays(holidays.readBytes(), data)
        return create(resource.contentAsByteArray, data, holidays)
    }

    private fun holidays(source: ByteArray, data: ReportData): Set<LocalDate> {
        val calendarStr = String(source)
        val eventMatches = "BEGIN:VEVENT(.*?)END:VEVENT".toRegex(RegexOption.DOT_MATCHES_ALL).findAll(calendarStr)
        
        return eventMatches
            .map { it.value }
            .filter { it.contains("Státní svátek") }
            .mapNotNull { extractDate(it, "DTSTART") }
            .map { it.toLocalDate() }
            .toSet()
    }

    fun create(source: ByteArray, data: ReportData, holidays: Set<LocalDate>): List<Report> {
        val calendarStr = String(source)
        val eventMatches = "BEGIN:VEVENT(.*?)END:VEVENT".toRegex(RegexOption.DOT_MATCHES_ALL).findAll(calendarStr)
        
        val events = eventMatches.map { 
            val eventStr = it.value
            val start = extractDate(eventStr, "DTSTART") ?: LocalDateTime.now()
            val end = extractDate(eventStr, "DTEND") ?: start.plusHours(1)
            Triple(eventStr, start, end)
        }.toList()
        
        val filteredEvents = events.filter { (_, start, end) -> 
            val startDate = start.toLocalDate()
            val endDate = end.toLocalDate()
            
            // Check if event overlaps with the requested date range
            (startDate.isBefore(data.end) || startDate.isEqual(data.end)) && 
            (endDate.isAfter(data.start) || endDate.isEqual(data.start))
        }
        
        return filteredEvents
            .flatMap { (_, start, end) -> 
                DateItemGenerator.generate(start, end, holidays)
            }
            .map { (day, startTime, endTime) -> 
                Report(
                    date = day,
                    start = startTime,
                    end = endTime,
                    name = data.name,
                    number = data.number
                )
            }
    }

    private fun extractDate(eventStr: String, propertyName: String): LocalDateTime? {
        val pattern = "$propertyName[:;].*?(\\d{8})(T(\\d{6}))?".toRegex()
        val match = pattern.find(eventStr)
        
        return if (match != null) {
            val dateValue = match.groupValues[1]
            val timeValue = match.groupValues[3]
            
            parseDate(dateValue, timeValue)
        } else {
            null
        }
    }
    
    private fun parseDate(dateValue: String, timeValue: String): LocalDateTime {
        try {
            val date = LocalDate.parse(dateValue, DateTimeFormatter.ofPattern("yyyyMMdd"))
            
            return if (timeValue.isNotEmpty()) {
                val time = LocalTime.parse(timeValue, DateTimeFormatter.ofPattern("HHmmss"))
                LocalDateTime.of(date, time)
            } else {
                date.atStartOfDay()
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse date: $dateValue $timeValue" }
            return LocalDateTime.now() // Fallback
        }
    }
}
