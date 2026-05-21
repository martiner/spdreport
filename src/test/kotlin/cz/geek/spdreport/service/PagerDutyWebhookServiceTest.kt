package cz.geek.spdreport.service

import cz.geek.spdreport.datastore.ProcessedWebhookEventRepository
import cz.geek.spdreport.pagerduty.IncidentWebhookData
import cz.geek.spdreport.pagerduty.IncidentWebhookEvent
import cz.geek.spdreport.pagerduty.IncidentWebhookFilter
import cz.geek.spdreport.pagerduty.IncidentWebhookPayload
import cz.geek.spdreport.pagerduty.MatchedIncident
import cz.geek.spdreport.pagerduty.PagerDutyReference
import io.kotest.core.spec.style.FreeSpec
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

private fun watchedPayload(eventId: String?): IncidentWebhookPayload =
    IncidentWebhookPayload(
        event = IncidentWebhookEvent(
            id = eventId,
            eventType = WATCHED_EVENT_TYPE,
            data = IncidentWebhookData(
                incident = PagerDutyReference("PINC1", "incident", null, null, null),
                user = null,
                escalationPolicy = null,
                message = null,
                state = null,
                type = null,
            ),
        ),
    )

private fun pingPayload(eventId: String?): IncidentWebhookPayload =
    IncidentWebhookPayload(
        event = IncidentWebhookEvent(id = eventId, eventType = PING_EVENT_TYPE, data = null),
    )

class PagerDutyWebhookServiceTest : FreeSpec({

    val filter = mockk<IncidentWebhookFilter>()
    val notifier = mockk<SlackNotifier>(relaxed = true)
    val processedEvents = mockk<ProcessedWebhookEventRepository>()
    val service = PagerDutyWebhookService(filter, notifier, processedEvents)

    val matched = MatchedIncident(title = "SIREN: boom", htmlUrl = "https://pd/incidents/PINC1")

    beforeTest {
        clearMocks(filter, notifier, processedEvents)
    }

    "processes and notifies for a first-seen watched event" {
        every { processedEvents.recordIfNew("evt-1") } returns true
        every { filter.match(any()) } returns matched

        service.handle(watchedPayload("evt-1"))

        verify(exactly = 1) { processedEvents.recordIfNew("evt-1") }
        verify(exactly = 1) { notifier.send(matched) }
    }

    "drops a duplicate matched incident without notifying" {
        every { filter.match(any()) } returns matched
        every { processedEvents.recordIfNew("evt-dup") } returns false

        service.handle(watchedPayload("evt-dup"))

        verify(exactly = 1) { processedEvents.recordIfNew("evt-dup") }
        verify(exactly = 0) { notifier.send(any()) }
        confirmVerified(notifier)
    }

    "processes without deduplication when the event id is null" {
        every { filter.match(any()) } returns matched

        service.handle(watchedPayload(null))

        verify(exactly = 0) { processedEvents.recordIfNew(any()) }
        verify(exactly = 1) { notifier.send(matched) }
        confirmVerified(processedEvents)
    }

    "processes without deduplication when the event id exceeds the max length" {
        val longId = "x".repeat(MAX_EVENT_ID_LENGTH + 1)
        every { filter.match(any()) } returns matched

        service.handle(watchedPayload(longId))

        verify(exactly = 0) { processedEvents.recordIfNew(any()) }
        verify(exactly = 1) { notifier.send(matched) }
        confirmVerified(processedEvents)
    }

    "does not deduplicate ping events" {
        service.handle(pingPayload("ping-1"))

        verify(exactly = 1) { notifier.sendPing("ping-1") }
        verify(exactly = 0) { processedEvents.recordIfNew(any()) }
        confirmVerified(processedEvents)
    }

    "does not deduplicate non-matching watched events" {
        every { filter.match(any()) } returns null

        service.handle(watchedPayload("evt-nomatch"))

        verify(exactly = 0) { processedEvents.recordIfNew(any()) }
        verify(exactly = 0) { notifier.send(any()) }
        confirmVerified(processedEvents)
    }
})
