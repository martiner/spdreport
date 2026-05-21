package cz.geek.spdreport.service

import cz.geek.spdreport.pagerduty.MatchedIncident
import cz.geek.spdreport.pagerduty.PagerDutyProperties
import jakarta.mail.internet.AddressException
import jakarta.mail.internet.InternetAddress
import mu.KotlinLogging
import org.springframework.mail.MailException
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class SlackNotifier(
    slackProperties: SlackProperties,
    pagerDutyProperties: PagerDutyProperties,
    private val emailSender: EmailSender,
) {

    private val channelEmail: String = slackProperties.channel.email
    private val watchedPolicyId: String = pagerDutyProperties.escalationPolicy.watchId

    fun send(incident: MatchedIncident) {
        val subject = ":pager: Escalation policy $watchedPolicyId added to incident — ${incident.title}"
        sendEmail(subject, incident.htmlUrl, "incident ${incident.htmlUrl}")
    }

    fun sendPing(eventId: String?) {
        sendEmail(
            ":satellite_antenna: PagerDuty webhook test ping",
            "PagerDuty sent a test ping to this webhook subscription.",
            "ping event $eventId",
        )
    }

    private fun sendEmail(subject: String, htmlBody: String, logContext: String) {
        try {
            val email = Email(
                recipient = InternetAddress(channelEmail),
                subject = subject,
                htmlBody = htmlBody,
            )
            emailSender.sendEmail(email)
        } catch (e: AddressException) {
            logger.error(e) { "Invalid Slack-channel email '$channelEmail' for $logContext" }
        } catch (e: MailException) {
            logger.error(e) { "Failed to send Slack-channel email for $logContext" }
        }
    }
}
