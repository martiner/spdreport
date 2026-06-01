package cz.geek.spdreport.web

import com.ninjasquad.springmockk.MockkBean
import cz.geek.spdreport.auth.PagerDutyUserService
import cz.geek.spdreport.datastore.ProcessedWebhookEventRepository
import cz.geek.spdreport.datastore.SettingsRepository
import cz.geek.spdreport.pagerduty.IncidentWebhookFilter
import cz.geek.spdreport.pagerduty.IncidentWebhookPayloadArgumentResolver
import cz.geek.spdreport.pagerduty.MatchedIncident
import cz.geek.spdreport.pagerduty.PagerDutyProperties
import cz.geek.spdreport.pagerduty.PagerDutySignatureVerifier
import cz.geek.spdreport.pagerduty.WebhookWebMvcConfig
import cz.geek.spdreport.service.PagerDutyWebhookService
import cz.geek.spdreport.service.SlackNotifier
import io.kotest.core.spec.style.FreeSpec
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.verify
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.info.GitProperties
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val SECRET = "test-secret"

private fun sign(body: String): String {
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(SECRET.toByteArray(Charsets.UTF_8), "HmacSHA256"))
    val bytes = mac.doFinal(body.toByteArray(Charsets.UTF_8))
    return "v1=" + bytes.joinToString("") { "%02x".format(it) }
}

private fun loadFixture(resource: String): String =
    PagerDutyWebhookControllerTest::class.java.getResource(resource)!!.readText()

@WebMvcTest
@ContextConfiguration(
    classes = [
        WebTestConfig::class,
        PagerDutyWebhookController::class,
        PagerDutyWebhookService::class,
        PagerDutySignatureVerifier::class,
        IncidentWebhookFilter::class,
        IncidentWebhookPayloadArgumentResolver::class,
        WebhookWebMvcConfig::class,
    ],
)
@EnableConfigurationProperties(PagerDutyProperties::class)
@TestPropertySource(
    properties = [
        "pagerduty.webhook.secret=test-secret",
        "pagerduty.escalation-policy.watch-id=PQ5P8WD",
    ],
)
class PagerDutyWebhookControllerTest(
    private val context: WebApplicationContext,
    @MockkBean(relaxed = true) val slackNotifier: SlackNotifier,
    @MockkBean val processedWebhookEventRepository: ProcessedWebhookEventRepository,
    @MockkBean val settingsRepository: SettingsRepository,
    @MockkBean val pagerDutyUserService: PagerDutyUserService,
    @MockkBean(relaxed = true) val gitProperties: GitProperties,
) : FreeSpec({

    lateinit var mockMvc: MockMvc

    beforeTest {
        every { processedWebhookEventRepository.recordIfNew(any()) } returns true
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    "returns 200 and notifies Slack when the signed body matches the watched policy" {
        val body = loadFixture("/pagerduty/incident-updated-add-escalation-policy.json")
        mockMvc.post("/webhooks/pagerduty/incident") {
            contentType = MediaType.APPLICATION_JSON
            header("X-PagerDuty-Signature", sign(body))
            content = body
        }.andExpect { status { isOk() } }

        verify(exactly = 1) {
            slackNotifier.send(
                MatchedIncident(
                    title = "SIREN: high error rate on payments-api",
                    htmlUrl = "https://sentinelone.pagerduty.com/incidents/PGR0VU2",
                ),
            )
        }
        confirmVerified(slackNotifier)
    }

    "returns 200 and sends a ping email when the signed body is a pagey.ping test event" {
        val body = loadFixture("/pagerduty/pagey-ping.json")
        mockMvc.post("/webhooks/pagerduty/incident") {
            contentType = MediaType.APPLICATION_JSON
            header("X-PagerDuty-Signature", sign(body))
            content = body
        }.andExpect { status { isOk() } }

        verify(exactly = 1) {
            slackNotifier.sendPing(eventId = "01GNQKVMSI0BBGALGL20WVSWRZ")
        }
        confirmVerified(slackNotifier)
    }

    "returns 200 and does not notify Slack when the signed body matches no watched policy" {
        val body = loadFixture("/pagerduty/incident-updated-other-change.json")
        mockMvc.post("/webhooks/pagerduty/incident") {
            contentType = MediaType.APPLICATION_JSON
            header("X-PagerDuty-Signature", sign(body))
            content = body
        }.andExpect { status { isOk() } }

        confirmVerified(slackNotifier)
    }

    "returns 401 when the signature header is missing" {
        val body = loadFixture("/pagerduty/incident-updated-add-escalation-policy.json")
        mockMvc.post("/webhooks/pagerduty/incident") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect { status { isUnauthorized() } }

        confirmVerified(slackNotifier)
    }

    "returns 401 when the signature is invalid" {
        val body = loadFixture("/pagerduty/incident-updated-add-escalation-policy.json")
        mockMvc.post("/webhooks/pagerduty/incident") {
            contentType = MediaType.APPLICATION_JSON
            header("X-PagerDuty-Signature", "v1=deadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeef")
            content = body
        }.andExpect { status { isUnauthorized() } }

        confirmVerified(slackNotifier)
    }

    "the webhook path is CSRF-exempt (no token attached, still reaches the handler)" {
        val body = loadFixture("/pagerduty/incident-updated-add-escalation-policy.json")
        mockMvc.post("/webhooks/pagerduty/incident") {
            contentType = MediaType.APPLICATION_JSON
            header("X-PagerDuty-Signature", sign(body))
            content = body
        }.andExpect { status { isOk() } }
    }

})
