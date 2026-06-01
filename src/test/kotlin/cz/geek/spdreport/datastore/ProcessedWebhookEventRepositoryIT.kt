package cz.geek.spdreport.datastore

import com.googlecode.objectify.ObjectifyFactory
import cz.geek.objectify.ObjectifyTest
import cz.geek.spdreport.ItHelper.objectify
import cz.geek.spdreport.TestHelper.random
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant

@ObjectifyTest
class ProcessedWebhookEventRepositoryIT(factory: ObjectifyFactory) : FreeSpec({

    val repo = ProcessedWebhookEventRepository(factory)

    "records a previously unseen event id with its received timestamp" {
        val eventId = random.nextAlphanumeric(20)
        val before = Instant.now()
        factory.objectify {
            repo.recordIfNew(eventId).shouldBeTrue()
        }
        factory.objectify {
            assertSoftly(repo.load(eventId)) {
                shouldNotBeNull()
                id shouldBe eventId
                receivedAt shouldBeGreaterThanOrEqualTo before
            }
        }
    }

    "treats a repeated event id as a duplicate" {
        val eventId = random.nextAlphanumeric(20)
        factory.objectify {
            repo.recordIfNew(eventId).shouldBeTrue()
        }
        factory.objectify {
            repo.recordIfNew(eventId).shouldBeFalse()
        }
    }
})
