package cz.geek.spdreport.pagerduty

import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.HexFormat
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val ALGORITHM = "HmacSHA256"
private const val SCHEME_PREFIX = "v1="

private val logger = KotlinLogging.logger {}

@Component
class PagerDutySignatureVerifier(
    properties: PagerDutyProperties,
) {

    private val keySpec: SecretKeySpec = SecretKeySpec(properties.webhook.secret.toByteArray(Charsets.UTF_8), ALGORITHM)

    fun verify(body: ByteArray, signatureHeader: String?): Boolean {
        val keySpec = keySpec ?: return false
        if (signatureHeader.isNullOrBlank()) return false
        val expected = computeMac(keySpec, body)
        return signatureHeader.split(',')
            .map { it.trim() }
            .filter { it.startsWith(SCHEME_PREFIX) }
            .map { it.removePrefix(SCHEME_PREFIX) }
            .mapNotNull { runCatching { HexFormat.of().parseHex(it) }.getOrNull() }
            .fold(false) { acc, candidate -> MessageDigest.isEqual(candidate, expected) or acc }
    }

    private fun computeMac(keySpec: SecretKeySpec, body: ByteArray): ByteArray {
        val mac = Mac.getInstance(ALGORITHM)
        mac.init(keySpec)
        return mac.doFinal(body)
    }
}
