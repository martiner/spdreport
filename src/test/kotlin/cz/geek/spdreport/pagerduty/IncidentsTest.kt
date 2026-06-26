package cz.geek.spdreport.pagerduty

import tools.jackson.databind.ObjectMapper
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.autoconfigure.json.JsonTest
import java.time.Instant

@JsonTest
class IncidentsTest(private val objectMapper: ObjectMapper) : FreeSpec({

    "Should read" {
        assertSoftly(objectMapper.readValue(javaClass.getResourceAsStream("/pagerduty/incidents.json")!!, Incidents::class.java)) {
            incidents.size shouldBe 2
            more shouldBe false
            assertSoftly(incidents[0]) {
                number shouldBe 1234
                title shouldBe "Database connection pool exhausted"
                createdAt shouldBe Instant.parse("2025-11-12T09:15:00Z")
                resolvedAt shouldBe Instant.parse("2025-11-12T10:42:00Z")
                escalationPolicy?.id shouldBe "PQ5P8WD"
            }
            assertSoftly(incidents[1]) {
                number shouldBe 1235
                resolvedAt.shouldBeNull()
            }
        }
    }

})
