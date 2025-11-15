package cz.geek.spdreport.pagerduty

import cz.geek.spdreport.auth.PagerDutyPrincipal
import org.springframework.beans.factory.annotation.Value
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

@Component
class PagerDutyClient(
    clientManager: OAuth2AuthorizedClientManager,
    @param:Value("\${pagerduty.api.url:https://api.pagerduty.com}")
    private val apiUrl: String,
) {

    private val acceptHeader = "Accept" to "application/vnd.pagerduty+json;version=2"

    private val format = DateTimeFormatterBuilder()
        .append(ISO_LOCAL_DATE)
        .appendLiteral('T')
        .append(ISO_LOCAL_TIME)
        .appendLiteral('Z')
        .toFormatter()

    private val client = RestClient.builder()
        .defaultHeader(acceptHeader.first, acceptHeader.second)
        .build()

    private val oauthClient: RestClient = RestClient.builder()
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
}
