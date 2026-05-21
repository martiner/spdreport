package cz.geek.spdreport.pagerduty

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class IncidentWebhookFilter(
    properties: PagerDutyProperties,
) {

    private val watchedPolicyId: String = properties.escalationPolicy.watchId
    private val log = LoggerFactory.getLogger(javaClass)

    fun match(payload: IncidentWebhookPayload): MatchedIncident? {
        val data = payload.event?.data
        if (data == null) {
            log.debug("Skipping webhook: missing event.data")
            return null
        }
        if (data.escalationPolicy?.id != watchedPolicyId) {
            log.debug(
                "Skipping webhook: escalation policy {} does not match watched {}",
                data.escalationPolicy?.id,
                watchedPolicyId,
            )
            return null
        }
        val incident = data.incident
        if (incident == null) {
            log.debug("Skipping webhook: missing incident data")
            return null
        }
        if (incident.summary == null) {
            log.debug("Skipping webhook: missing incident summary")
            return null
        }
        if (incident.htmlUrl == null) {
            log.debug("Skipping webhook: missing incident htmlUrl")
            return null
        }
        log.info("Matched incident for watched escalation policy {}: {}", watchedPolicyId, incident.htmlUrl)
        return MatchedIncident(incident.summary, incident.htmlUrl)
    }
}
