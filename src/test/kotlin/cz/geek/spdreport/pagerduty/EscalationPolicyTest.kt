package cz.geek.spdreport.pagerduty

import tools.jackson.databind.ObjectMapper
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.autoconfigure.json.JsonTest

@JsonTest
class EscalationPolicyTest(private val objectMapper: ObjectMapper) : FreeSpec({

    "Should read" {
        assertSoftly(objectMapper.readValue(javaClass.getResourceAsStream("/pagerduty/escalation-policy.json")!!, EscalationPolicyResponse::class.java)) {
            escalationPolicy.services.mapNotNull { it.id } shouldBe listOf("PIJ90N7", "PABC123")
        }
    }

})
