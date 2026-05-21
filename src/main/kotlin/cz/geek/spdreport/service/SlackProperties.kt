package cz.geek.spdreport.service

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "slack")
data class SlackProperties(
    val channel: Channel = Channel(),
) {
    data class Channel(val email: String = "")
}
