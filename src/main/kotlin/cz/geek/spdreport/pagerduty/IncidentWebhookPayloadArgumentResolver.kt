package cz.geek.spdreport.pagerduty

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.server.ResponseStatusException

private const val SIGNATURE_HEADER = "X-PagerDuty-Signature"

private val log = KotlinLogging.logger {}

@Component
class IncidentWebhookPayloadArgumentResolver(
    private val verifier: PagerDutySignatureVerifier,
    private val objectMapper: ObjectMapper,
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        IncidentWebhookPayload::class.java == parameter.parameterType

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): IncidentWebhookPayload {
        val request = requireNotNull(webRequest.getNativeRequest(HttpServletRequest::class.java)) {
            "HttpServletRequest is not available"
        }
        val rawBody: ByteArray = request.inputStream.readAllBytes()
        val signature: String? = request.getHeader(SIGNATURE_HEADER)
        if (!verifier.verify(rawBody, signature)) {
            log.warn { "PagerDuty webhook signature verification failed" }
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
        return try {
            objectMapper.readValue(rawBody, IncidentWebhookPayload::class.java)
        } catch (e: JacksonException) {
            log.warn(e) { "PagerDuty webhook body could not be parsed as JSON" }
            IncidentWebhookPayload(event = null)
        }
    }
}
