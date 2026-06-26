package cz.geek.spdreport.pagerduty

import cz.geek.spdreport.auth.PagerDutyPrincipal
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.Matchers.startsWith
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import java.time.LocalDate

class PagerDutyClientTest : FreeSpec({

    val builder = RestClient.builder()
    val server = MockRestServiceServer.bindTo(builder).build()
    val clientManager = mockk<OAuth2AuthorizedClientManager>()

    val registration = ClientRegistration.withRegistrationId("pagerduty")
        .clientId("client")
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .redirectUri("{baseUrl}/cb")
        .authorizationUri("https://identity.pagerduty.com/oauth/authorize")
        .tokenUri("https://identity.pagerduty.com/oauth/token")
        .build()
    val token = OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "token", null, null)
    every { clientManager.authorize(any()) } returns OAuth2AuthorizedClient(registration, "PQ303VB", token)

    val client = PagerDutyClient(clientManager, PagerDutyProperties(), builder)
    val user = PagerDutyPrincipal("PQ303VB")

    afterTest { server.reset() }

    "fetchServiceIds unwraps escalation_policy.services" {
        server.expect(requestTo("https://api.pagerduty.com/escalation_policies/PQ5P8WD"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(ClassPathResource("pagerduty/escalation-policy.json"), MediaType.APPLICATION_JSON))

        client.fetchServiceIds(user, "PQ5P8WD") shouldBe listOf("PIJ90N7", "PABC123")
        server.verify()
    }

    "fetchIncidents sends since/until/service_ids/limit and follows pagination" {
        val page1 = """{"incidents":[{"incident_number":1,"title":"a","created_at":"2025-11-12T09:00:00Z","resolved_at":null,"escalation_policy":{"id":"PQ5P8WD"}}],"more":true}"""
        val page2 = """{"incidents":[{"incident_number":2,"title":"b","created_at":"2025-11-13T09:00:00Z","resolved_at":null,"escalation_policy":{"id":"PQ5P8WD"}}],"more":false}"""

        server.expect(requestTo(startsWith("https://api.pagerduty.com/incidents")))
            .andExpect(method(HttpMethod.GET))
            .andExpect(queryParam("since", "2025-11-01T00:00:00Z"))
            .andExpect(queryParam("until", "2025-11-30T23:59:59Z"))
            .andExpect(queryParam("service_ids[]", "PIJ90N7", "PABC123"))
            .andExpect(queryParam("limit", "100"))
            .andExpect(queryParam("offset", "0"))
            .andRespond(withSuccess(page1, MediaType.APPLICATION_JSON))
        server.expect(requestTo(startsWith("https://api.pagerduty.com/incidents")))
            .andExpect(queryParam("offset", "100"))
            .andRespond(withSuccess(page2, MediaType.APPLICATION_JSON))

        val incidents = client.fetchIncidents(
            user,
            LocalDate.of(2025, 11, 1),
            LocalDate.of(2025, 11, 30),
            listOf("PIJ90N7", "PABC123"),
        )
        assertSoftly(incidents) {
            shouldHaveSize(2)
            this[0].number shouldBe 1
            this[1].number shouldBe 2
        }
        server.verify()
    }

})
