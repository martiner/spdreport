package cz.geek.spdreport.pagerduty

import cz.geek.spdreport.auth.PagerDutyPrincipal
import mu.KotlinLogging
import org.springframework.core.ParameterizedTypeReference
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId
import org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver
import org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver.principal
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME
import java.time.format.DateTimeFormatterBuilder

private val logger = KotlinLogging.logger {}

@Component
class PagerDutyClient(
    clientManager: OAuth2AuthorizedClientManager,
    properties: PagerDutyProperties,
    restClientBuilder: RestClient.Builder,
) {

    private val apiUrl: String = properties.api.url

    private val acceptHeader = "Accept" to "application/vnd.pagerduty+json;version=2"

    private val format = DateTimeFormatterBuilder()
        .append(ISO_LOCAL_DATE)
        .appendLiteral('T')
        .append(ISO_LOCAL_TIME)
        .appendLiteral('Z')
        .toFormatter()

    private val client = restClientBuilder.clone()
        .defaultHeader(acceptHeader.first, acceptHeader.second)
        .build()

    private val oauthClient: RestClient = restClientBuilder.clone()
        .defaultHeader(acceptHeader.first, acceptHeader.second)
        .requestInterceptor(
            OAuth2ClientHttpRequestInterceptor(clientManager)
                .apply {
                    setPrincipalResolver(RequestAttributePrincipalResolver())
                }
        )
        .defaultRequest { request -> request.attributes(clientRegistrationId("pagerduty")) }
        .build()

    fun fetchCurrentUser(tokenValue: String): Map<String, Any> {
        val uri = UriComponentsBuilder.fromUriString(apiUrl)
            .path("/users/me")
            .build()
            .toUri()
        val resp = client.get()
            .uri(uri)
            .headers { headers -> headers.setBearerAuth(tokenValue) }
            .retrieve()
            .body(object : ParameterizedTypeReference<Map<String, Any>>() {})
            ?: error("Unable to get current user")
        @Suppress("UNCHECKED_CAST")
        return resp["user"] as Map<String, Any>? ?: error("Unable to get current user")
    }

    fun fetchOnCalls(user: PagerDutyPrincipal, start: LocalDate, end: LocalDate): OnCalls {
        val since = format.format(start.atTime(0, 0, 0))
        val until = format.format(end.atTime(23, 59, 59))
        val uri = UriComponentsBuilder.fromUriString(apiUrl)
            .path("/oncalls")
            .queryParam("since", since)
            .queryParam("until", until)
            .queryParam("user_ids[]", user.name)
            .queryParam("limit", 100)
            .build()
            .toUri()
        return oauthClient.get()
            .uri(uri)
            .attributes(principal(user))
            .retrieve()
            .body(OnCalls::class.java)
            ?: error("Empty response")
    }

    fun fetchServiceIds(user: PagerDutyPrincipal, escalationPolicyId: String): List<String> {
        logger.debug { "Fetching services for escalation policy $escalationPolicyId" }
        val uri = UriComponentsBuilder.fromUriString(apiUrl)
            .path("/escalation_policies/{id}")
            .build(escalationPolicyId)
        val response = oauthClient.get()
            .uri(uri)
            .attributes(principal(user))
            .retrieve()
            .body(EscalationPolicyResponse::class.java)
            ?: error("Empty response")
        val serviceIds = response.escalationPolicy.services.mapNotNull { it.id }
        logger.debug { "Escalation policy $escalationPolicyId has services $serviceIds" }
        return serviceIds
    }

    fun fetchIncidents(
        user: PagerDutyPrincipal,
        start: LocalDate,
        end: LocalDate,
        serviceIds: List<String>,
    ): List<Incident> {
        val since = format.format(start.atTime(0, 0, 0))
        val until = format.format(end.atTime(23, 59, 59))
        val incidents = mutableListOf<Incident>()
        var offset = 0
        do {
            logger.debug { "Fetching incidents since=$since until=$until services=$serviceIds offset=$offset" }
            val uri = UriComponentsBuilder.fromUriString(apiUrl)
                .path("/incidents")
                .queryParam("since", since)
                .queryParam("until", until)
                .queryParam("service_ids[]", serviceIds)
                .queryParam("limit", 100)
                .queryParam("offset", offset)
                .build()
                .toUri()
            val page = oauthClient.get()
                .uri(uri)
                .attributes(principal(user))
                .retrieve()
                .body(Incidents::class.java)
                ?: error("Empty response")
            logger.debug { "Fetched ${page.incidents.size} incidents at offset=$offset more=${page.more}" }
            incidents += page.incidents
            offset += 100
        } while (page.more)
        logger.info { "Fetched ${incidents.size} incidents for services $serviceIds" }
        return incidents
    }
}
