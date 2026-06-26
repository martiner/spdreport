package cz.geek.spdreport.pagerduty

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
data class Incidents(
    val incidents: List<Incident>,
    val more: Boolean = false,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Incident(
    @JsonProperty("incident_number") val number: Int,
    val title: String,
    @JsonProperty("created_at") val createdAt: Instant,
    @JsonProperty("resolved_at") val resolvedAt: Instant?,
    @JsonProperty("escalation_policy") val escalationPolicy: PagerDutyReference?,
)
