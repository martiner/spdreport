package cz.geek.spdreport.pagerduty

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class IncidentWebhookPayload(
    val event: IncidentWebhookEvent?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class IncidentWebhookEvent(
    val id: String?,
    @JsonProperty("event_type") val eventType: String?,
    val data: IncidentWebhookData?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class IncidentWebhookData(
    val incident: PagerDutyReference?,
    val user: PagerDutyReference?,
    @JsonProperty("escalation_policy") val escalationPolicy: PagerDutyReference?,
    val message: String?,
    val state: String?,
    val type: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PagerDutyReference(
    val id: String?,
    val type: String?,
    val summary: String?,
    val self: String?,
    @JsonProperty("html_url") val htmlUrl: String?,
)

data class MatchedIncident(
    val title: String,
    val htmlUrl: String,
)
