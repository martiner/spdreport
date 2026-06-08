package cz.geek.spdreport.pagerduty

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class PagerDutySignatureVerifierTest : FreeSpec({

    val secret = "test-secret-v1"
    val otherSecret = "different-secret"
    val bodyText = """{"event":{"id":"abc","event_type":"incident.updated"}}"""
    val body = bodyText.toByteArray(Charsets.UTF_8)
    val validHex = hmacHex(secret, body)
    val otherHex = hmacHex(otherSecret, body)

    val verifier = PagerDutySignatureVerifier(propsFor(secret))

    "accepts a single valid v1= signature" {
        verifier.verify(body, "v1=$validHex") shouldBe true
    }

    "accepts a multi-signature header when at least one signature matches" {
        verifier.verify(body, "v1=$otherHex,v1=$validHex") shouldBe true
        verifier.verify(body, "v1=$validHex,v1=$otherHex") shouldBe true
    }

    "rejects when the body has been mutated" {
        val tampered = bodyText.replace("abc", "xyz").toByteArray(Charsets.UTF_8)
        verifier.verify(tampered, "v1=$validHex") shouldBe false
    }

    "rejects a signature computed with the wrong secret" {
        verifier.verify(body, "v1=$otherHex") shouldBe false
    }

    "rejects null header" {
        verifier.verify(body, null) shouldBe false
    }

    "rejects empty header" {
        verifier.verify(body, "") shouldBe false
    }

    "rejects blank header" {
        verifier.verify(body, "   ") shouldBe false
    }

    "rejects header without v1= prefix" {
        verifier.verify(body, validHex) shouldBe false
    }

    "rejects header with unknown scheme prefix" {
        verifier.verify(body, "v2=$validHex") shouldBe false
    }

    "rejects garbage hex" {
        verifier.verify(body, "v1=not-hex-content") shouldBe false
    }

    "rejects a v1= entry that is valid hex but a truncated MAC" {
        verifier.verify(body, "v1=deadbeef") shouldBe false
    }

    "rejects a header containing only non-v1 entries" {
        verifier.verify(body, "v2=$validHex,v3=$validHex") shouldBe false
    }
})

private fun propsFor(secret: String) = PagerDutyProperties(webhook = PagerDutyProperties.Webhook(secret = secret))

private fun hmacHex(secret: String, body: ByteArray): String {
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
    val bytes = mac.doFinal(body)
    return bytes.joinToString("") { "%02x".format(it) }
}
