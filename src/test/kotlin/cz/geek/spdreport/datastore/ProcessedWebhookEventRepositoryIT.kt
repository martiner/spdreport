package cz.geek.spdreport.datastore

import cz.geek.spdreport.TestHelper.random
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant

class ProcessedWebhookEventRepositoryIT : AbstractObjectifyIT({

    val repo = ProcessedWebhookEventRepository()

    "records a previously unseen event id with its received timestamp" {
        val eventId = random.nextAlphanumeric(20)
        val before = Instant.now()
        objectify {
            repo.recordIfNew(eventId).shouldBeTrue()
        }
        objectify {
            assertSoftly(repo.load(eventId)) {
                shouldNotBeNull()
                id shouldBe eventId
                receivedAt shouldBeGreaterThanOrEqualTo before
            }
        }
    }

    "treats a repeated event id as a duplicate" {
        val eventId = random.nextAlphanumeric(20)
        objectify {
            repo.recordIfNew(eventId).shouldBeTrue()
        }
        objectify {
            repo.recordIfNew(eventId).shouldBeFalse()
        }
    }
})
