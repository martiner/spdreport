package cz.geek.spdreport.service

import cz.geek.spdreport.pagerduty.MatchedIncident
import cz.geek.spdreport.pagerduty.PagerDutyProperties
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.mail.internet.InternetAddress
import org.springframework.mail.MailSendException

private const val CHANNEL_EMAIL = "epps-console-pd-abc1234@email.slack.com"
private const val WATCHED_POLICY = "PQ5P8WD"

private val sampleIncident = MatchedIncident(
    title = "SIREN: high error rate on payments-api",
    htmlUrl = "https://sentinelone.pagerduty.com/incidents/PGR0VU2",
)

private fun notifier(emailSender: EmailSender) = SlackNotifier(
    SlackProperties(channel = SlackProperties.Channel(email = CHANNEL_EMAIL)),
    PagerDutyProperties(escalationPolicy = PagerDutyProperties.EscalationPolicy(watchId = WATCHED_POLICY)),
    emailSender,
)

class SlackNotifierTest : FreeSpec({

    "sends an email whose subject names the policy and incident title and whose body links to the incident" {
        val emailSender = mockk<EmailSender>(relaxed = true)
        val notifier = notifier(emailSender)
        val capturedEmail = slot<Email>()
        every { emailSender.sendEmail(capture(capturedEmail)) } returns Unit

        notifier.send(sampleIncident)

        val email = capturedEmail.captured
        email.recipient shouldBe InternetAddress(CHANNEL_EMAIL)
        email.subject shouldBe ":pager: Escalation policy PQ5P8WD added to incident — SIREN: high error rate on payments-api"
        email.htmlBody shouldBe sampleIncident.htmlUrl
        verify(exactly = 1) { emailSender.sendEmail(any()) }
        confirmVerified(emailSender)
    }

    "swallows mail-send exceptions so the caller can still return 200 to PagerDuty" {
        val emailSender = mockk<EmailSender>(relaxed = true)
        every { emailSender.sendEmail(any()) } throws MailSendException("smtp down")
        val notifier = notifier(emailSender)

        notifier.send(sampleIncident)

        verify(exactly = 1) { emailSender.sendEmail(any()) }
    }

})
