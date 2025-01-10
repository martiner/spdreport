package cz.geek.spdreport.datastore

import cz.geek.spdreport.TestHelper.random
import cz.geek.spdreport.model.EmailFrequency.WEEKLY
import cz.geek.spdreport.model.Settings
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class SettingsRepositoryIT : AbstractObjectifyIT({

    val id1 = random.nextAlphanumeric(10)
    val id2 = random.nextAlphanumeric(10)

    val repo = SettingsRepository()

    beforeSpec {
        objectify {
            repo.save(Settings(id = id1, fullName = "First"))
        }
    }

    "Should load settings by id" {
        objectify {
            assertSoftly(repo.load(id1)) {
                shouldNotBeNull()
                fullName shouldBe "First"
            }
        }
    }

    "Should not load settings" {
        objectify {
            assertSoftly(repo.load("unknown")) {
                shouldBeNull()
            }
        }
    }

    "Should find by frequency" {
        var initial = 0
        objectify {
            initial = repo.find(WEEKLY).size
            repo.save(Settings(id = id2, fullName = "Second", emailFrequency = WEEKLY))
        }
        objectify {
            val result = repo.find(WEEKLY)
            result shouldHaveSize (initial + 1)
            result shouldHaveSingleElement { it.id == id2 }
        }
    }
})
