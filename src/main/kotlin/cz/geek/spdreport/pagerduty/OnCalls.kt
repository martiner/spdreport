package cz.geek.spdreport.pagerduty

import java.time.Instant

data class OnCalls(
    val oncalls: List<OnCall>,
)

data class OnCall(
    val start: Instant?,
    val end: Instant?,
)
