package cz.geek.spdreport.service

import cz.geek.spdreport.auth.User
import cz.geek.spdreport.model.ReportData
import cz.geek.spdreport.pagerduty.PagerDutyClient
import mu.KotlinLogging
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
class ReportService(
    private val holidayService: HolidayService,
    private val calendarService: CalendarService,
    private val pagerDutyClient: PagerDutyClient,
) {

    fun create(data: ReportData, principal: User? = null): List<Report> { // todo remove = null default
        logger.info { "Creating report for $data" }
        val source = data.source()
        if (source != null) {
            return create(source.resource, data)
        }
        if (principal != null) {
            return createPD(data, principal)
        }
        return emptyList()
    }

    fun createPD(data: ReportData, user: User): List<Report> {
        logger.info { "Creating PD report for $user $data" }
        val response = pagerDutyClient.fetchOnCalls(user.name, data.start, data.end)
        return response.oncalls
            .filter { it.start != null && it.end != null }
            .map { LocalDateTimePair(it.start!!.toLocal(), it.end!!.toLocal()) }
            .let {
                create(it, data)
            }
    }

    fun create(source: Resource, data: ReportData): List<Report> {
        return calendarService.load(source, data.start, data.end)
            .map { LocalDateTimePair(it.start(), it.end()) }
            .let { create(it, data) }
    }

    private fun create(list: List<LocalDateTimePair>, data: ReportData): List<Report> {
        val holidays = holidayService.getHolidays(data.country, data.start, data.end)
        return list.flatMap { DateItemGenerator.generate(it.start, it.end, holidays) }
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
