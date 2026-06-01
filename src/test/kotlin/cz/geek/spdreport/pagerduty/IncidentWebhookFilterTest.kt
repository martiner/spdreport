package cz.geek.spdreport.pagerduty

import tools.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.autoconfigure.json.JsonTest

private const val WATCHED = "PQ5P8WD"

@JsonTest
class IncidentWebhookFilterTest(private val objectMapper: ObjectMapper) : FreeSpec({

    val filter = IncidentWebhookFilter(
        PagerDutyProperties(escalationPolicy = PagerDutyProperties.EscalationPolicy(watchId = WATCHED)),
    )

    fun parse(resource: String): IncidentWebhookPayload =
        objectMapper.readValue(javaClass.getResourceAsStream(resource)!!, IncidentWebhookPayload::class.java)

    fun parseJson(json: String): IncidentWebhookPayload =
        objectMapper.readValue(json, IncidentWebhookPayload::class.java)

    "matches the watched policy in the positive fixture" {
        val payload = parse("/pagerduty/incident-updated-add-escalation-policy.json")
        filter.match(payload) shouldBe MatchedIncident(
            title = "SIREN: high error rate on payments-api",
            htmlUrl = "https://sentinelone.pagerduty.com/incidents/PGR0VU2",
        )
    }

    "ignores a delivery whose escalation policy is a different one" {
        val payload = parse("/pagerduty/incident-updated-other-change.json")
        filter.match(payload) shouldBe null
    }

    "returns null when event.data is null" {
        val payload = parseJson(
            """
            {
              "event": {
                "id": "evt-nodata",
                "event_type": "incident.responder.added",
                "occurred_at": "2026-05-20T18:30:00Z",
                "data": null
              }
            }
            """.trimIndent()
        )
        filter.match(payload) shouldBe null
    }

    "returns null when escalation_policy is missing" {
        val payload = parseJson(
            """
            {
              "event": {
                "id": "evt-noep",
                "event_type": "incident.responder.added",
                "occurred_at": "2026-05-20T18:30:00Z",
                "data": {
                  "incident": {
                    "id": "PINCNOEP",
                    "type": "incident_reference",
                    "summary": "No escalation policy on the data",
                    "html_url": "https://sentinelone.pagerduty.com/incidents/PINCNOEP"
                  },
                  "message": "hi",
                  "state": "pending",
                  "type": "incident_responder"
                }
              }
            }
            """.trimIndent()
        )
        filter.match(payload) shouldBe null
    }

    "returns null when incident reference is missing on a watched-policy delivery" {
        val payload = parseJson(
            """
            {
              "event": {
                "id": "evt-noincident",
                "event_type": "incident.responder.added",
                "occurred_at": "2026-05-20T18:30:00Z",
                "data": {
                  "incident": null,
                  "escalation_policy": {
                    "id": "$WATCHED",
                    "type": "escalation_policy_reference",
                    "summary": "EPPS Console",
                    "html_url": "https://sentinelone.pagerduty.com/escalation_policies/$WATCHED"
                  },
                  "message": "hi",
                  "state": "pending",
                  "type": "incident_responder"
                }
              }
            }
            """.trimIndent()
        )
        filter.match(payload) shouldBe null
    }

})
