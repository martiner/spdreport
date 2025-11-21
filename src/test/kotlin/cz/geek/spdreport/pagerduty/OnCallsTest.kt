package cz.geek.spdreport.pagerduty

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.autoconfigure.json.JsonTest
import java.time.Instant

@JsonTest
class OnCallsTest(private val objectMapper: ObjectMapper) : FreeSpec({

    "Should read" {
        assertSoftly(objectMapper.readValue(javaClass.getResource("/pagerduty/oncalls.json"), OnCalls::class.java)) {
            oncalls.size shouldBe 2
            assertSoftly(oncalls[1]) {
                start shouldBe Instant.parse("2025-11-11T08:00:00Z")
                end shouldBe Instant.parse("2025-11-18T08:00:00Z")
            }
        }
    }

})
