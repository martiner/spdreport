package cz.geek.spdreport.pagerduty

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "pagerduty")
data class PagerDutyProperties(
    val api: Api = Api(),
    val webhook: Webhook = Webhook(),
    val escalationPolicy: EscalationPolicy = EscalationPolicy(),
) {
    data class Api(val url: String = "https://api.pagerduty.com")
    data class Webhook(val secret: String = "")
    data class EscalationPolicy(val watchId: String = "")
}
