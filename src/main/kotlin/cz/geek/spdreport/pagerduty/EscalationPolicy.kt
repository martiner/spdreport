package cz.geek.spdreport.pagerduty

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class EscalationPolicyResponse(
    @JsonProperty("escalation_policy") val escalationPolicy: EscalationPolicy,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EscalationPolicy(
    val services: List<PagerDutyReference> = emptyList(),
)
