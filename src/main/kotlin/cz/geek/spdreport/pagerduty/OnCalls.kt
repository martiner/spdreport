package cz.geek.spdreport.pagerduty

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class OnCalls(
    val oncalls: List<OnCall>,
)

data class OnCall(
    val start: Instant?,
    val end: Instant?,
    @JsonProperty("escalation_policy") val escalationPolicy: PagerDutyReference? = null,
)
