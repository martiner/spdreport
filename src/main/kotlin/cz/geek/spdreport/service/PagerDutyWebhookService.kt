package cz.geek.spdreport.service

import cz.geek.spdreport.datastore.ProcessedWebhookEventRepository
import cz.geek.spdreport.pagerduty.IncidentWebhookFilter
import cz.geek.spdreport.pagerduty.IncidentWebhookPayload
import mu.KotlinLogging
import org.springframework.stereotype.Service

internal const val WATCHED_EVENT_TYPE = "incident.responder.added"
internal const val PING_EVENT_TYPE = "pagey.ping"

internal const val MAX_EVENT_ID_LENGTH = 1500

private val log = KotlinLogging.logger {}

private fun String?.forLog(): String = this?.replace(Regex("\\p{Cntrl}"), "_") ?: "null"

@Service
class PagerDutyWebhookService(
    private val filter: IncidentWebhookFilter,
    private val notifier: SlackNotifier,
    private val processedEvents: ProcessedWebhookEventRepository,
) {

    fun handle(payload: IncidentWebhookPayload) {
        val event = payload.event
        if (event == null) {
            log.warn { "PagerDuty webhook ignored: payload has no event" }
            return
        }
        val eventId = event.id
        val incidentId = event.data?.incident?.id
        val eventIdLog = eventId.forLog()
        val incidentIdLog = incidentId.forLog()
        if (event.eventType == PING_EVENT_TYPE) {
            notifier.sendPing(eventId)
            log.info { "PagerDuty webhook ping received and notified: event=$eventIdLog" }
            return
        }
        if (event.eventType != WATCHED_EVENT_TYPE) {
            log.info { "PagerDuty webhook ignored: event=$eventIdLog incident=$incidentIdLog eventType=${event.eventType.forLog()} not $WATCHED_EVENT_TYPE" }
            return
        }
        val matched = filter.match(payload)
        if (matched == null) {
            log.info { "PagerDuty webhook ignored: event=$eventIdLog incident=$incidentIdLog did not match watched escalation policy" }
            return
        }

        if (eventId.isNullOrBlank() || eventId.length > MAX_EVENT_ID_LENGTH) {
            log.warn { "PagerDuty webhook cannot be deduplicated: invalid event id (event=$eventIdLog incident=$incidentIdLog); notifying anyway" }
        } else if (!processedEvents.recordIfNew(eventId)) {
            log.info { "PagerDuty webhook duplicate dropped: event=$eventIdLog incident=$incidentIdLog" }
            return
        }
        notifier.send(matched)
        log.info { "PagerDuty webhook notified: event=$eventIdLog incident=$incidentIdLog title='${matched.title}'" }
    }
}
