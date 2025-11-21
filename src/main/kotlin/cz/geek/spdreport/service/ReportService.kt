package cz.geek.spdreport.service

import cz.geek.spdreport.auth.PagerDutyPrincipal
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

    fun create(data: ReportData, principal: PagerDutyPrincipal?): List<Report> {
        if (principal != null) {
            return createPD(data, principal)
        }
        val source = data.source()
        if (source != null) {
            return createIcal(source.resource, data)
        }
        return emptyList()
    }

    private fun createPD(data: ReportData, user: PagerDutyPrincipal): List<Report> {
        logger.info { "Creating PD report for ${user.name} $data" }
        val response = pagerDutyClient.fetchOnCalls(user, data.start, data.end)
        return response.oncalls
            .filter { it.start != null && it.end != null }
            .map { LocalDateTimePair(it.start!!.toLocal(), it.end!!.toLocal()) }
            .let {
                create(it, data)
            }
    }

    fun createIcal(source: Resource, data: ReportData): List<Report> {
        logger.info { "Creating iCal report for $data" }
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
