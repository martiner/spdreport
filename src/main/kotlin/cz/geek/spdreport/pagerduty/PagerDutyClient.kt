package cz.geek.spdreport.pagerduty

import cz.geek.spdreport.auth.ExistingAuthentication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId
import org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver
import org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver.principal
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME
import java.time.format.DateTimeFormatterBuilder

@Component
class PagerDutyClient(
    restTemplateBuilder: RestTemplateBuilder,
    clientManager: OAuth2AuthorizedClientManager,
) {

    private val format = DateTimeFormatterBuilder()
        .append(ISO_LOCAL_DATE)
        .appendLiteral('T')
        .append(ISO_LOCAL_TIME)
        .appendLiteral('Z')
        .toFormatter()

    private val rt: RestTemplate = restTemplateBuilder.build()

    private val restClient: RestClient = RestClient.builder()
        .requestInterceptor(
            OAuth2ClientHttpRequestInterceptor(clientManager)
                .apply {
                    setPrincipalResolver(RequestAttributePrincipalResolver())
                }
        )
        .build()

    private fun pdHeaders(token: String): HttpHeaders =
        HttpHeaders().apply {
            setBearerAuth(token)
            add("Accept", "application/vnd.pagerduty+json;version=2")
        }

    /** GET /users/me */
    fun fetchCurrentUser(tokenValue: String): Map<String, Any> {
        val resp = rt.exchange(
            "https://api.pagerduty.com/users/me",
            HttpMethod.GET,
            HttpEntity<Void>(pdHeaders(tokenValue)),
            object : ParameterizedTypeReference<Map<String, Any>>() {}
        )
        @Suppress("UNCHECKED_CAST")
        return resp.body?.get("user") as Map<String, Any>? ?: error("Unable to get current user")
    }

    /** GET /oncalls?since=...&until=...&limit=100 */
    fun fetchOnCalls(user: String, start: LocalDate, end: LocalDate): OnCalls {
        val sinceUtc = start.atTime(0, 0, 0)
        val untilUtc = end.atTime(23, 59, 59)
        val since = format.format(sinceUtc)
        val until = format.format(untilUtc)
        val url = "https://api.pagerduty.com/oncalls?since=$since&until=$until&user_ids[]=$user&limit=100"
        return restClient.get()
            .uri(url)
            .header("accept", "application/vnd.pagerduty+json;version=2")
            .attributes(clientRegistrationId("pagerduty"))
            .attributes(principal(ExistingAuthentication(user)))
            .retrieve()
            .body(OnCalls::class.java)
            ?: error("Empty response")
    }
}
