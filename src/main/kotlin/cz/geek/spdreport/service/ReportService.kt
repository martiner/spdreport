package cz.geek.spdreport.service

import cz.geek.spdreport.auth.PagerDutyPrincipal
import cz.geek.spdreport.model.ReportData
import cz.geek.spdreport.pagerduty.Incident
import cz.geek.spdreport.pagerduty.OnCall
import cz.geek.spdreport.pagerduty.OnCalls
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

    fun create(data: ReportData, principal: PagerDutyPrincipal?): Report {
        if (principal != null) {
            return createPD(data, principal)
        }
        val source = data.source()
        if (source != null) {
            return createIcal(source.resource, data)
        }
        return Report(data.name, data.number, data.country, emptyList())
    }

    private fun createPD(data: ReportData, user: PagerDutyPrincipal): Report {
        logger.info { "Creating PD report for ${user.name} $data" }
        val onCalls = pagerDutyClient.fetchOnCalls(user, data.start, data.end)
        val report = onCalls.oncalls
            .filter { it.start != null && it.end != null }
            .map { LocalDateTimePair(it.start!!.toLocal(), it.end!!.toLocal()) }
            .let { create(it, data) }
        return report.copy(incidents = fetchIncidents(data, user, onCalls))
    }

    private fun fetchIncidents(data: ReportData, user: PagerDutyPrincipal, onCalls: OnCalls): List<ReportIncident> {
        val epIds = onCalls.oncalls.mapNotNull { it.escalationPolicy?.id }.toSet()
        val serviceIds = epIds.flatMap { pagerDutyClient.fetchServiceIds(user, it) }.distinct()
        if (serviceIds.isEmpty()) {
            logger.info { "No services for on-call escalation policies $epIds, skipping incidents" }
            return emptyList()
        }
        val fetched = pagerDutyClient.fetchIncidents(user, data.start, data.end, serviceIds)
        val incidents = fetched
            .filter { incident -> onCalls.oncalls.any { it.covers(incident) } }
            .map { ReportIncident(it.number, it.title, it.createdAt.toLocal(), it.resolvedAt?.toLocal()) }
        logger.info { "Matched ${incidents.size} of ${fetched.size} fetched incidents for ${user.name}" }
        return incidents
    }

    fun createIcal(source: Resource, data: ReportData): Report {
        logger.info { "Creating iCal report for $data" }
        return calendarService.load(source, data.start, data.end)
            .map { LocalDateTimePair(it.start(), it.end()) }
            .let { create(it, data) }
    }

    private fun create(list: List<LocalDateTimePair>, data: ReportData): Report {
        val holidays = holidayService.getHolidays(data.country, data.start, data.end)
        val items = list.flatMap { DateItemGenerator.generate(it.start, it.end, holidays) }
            .map { (day, start, end) ->
                ReportItem(
                    date = day,
                    start = start,
                    end = end,
                )
            }
        return Report(data.name, data.number, data.country, items)
    }
}

data class LocalDateTimePair(val start: LocalDateTime, val end: LocalDateTime)

private fun OnCall.covers(incident: Incident): Boolean {
    val epId = escalationPolicy?.id ?: return false
    if (epId != incident.escalationPolicy?.id) return false
    val created = incident.createdAt
    return (start == null || !created.isBefore(start)) && (end == null || !created.isAfter(end))
}
