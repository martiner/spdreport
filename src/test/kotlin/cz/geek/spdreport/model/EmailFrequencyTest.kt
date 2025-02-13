package cz.geek.spdreport.model

import cz.geek.spdreport.model.EmailFrequency.MONTHLY
import cz.geek.spdreport.model.EmailFrequency.NONE
import cz.geek.spdreport.model.EmailFrequency.WEEKLY
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class EmailFrequencyTest : FreeSpec({

    val monday = LocalDate.of(2023, 9, 4)

    "Should compute date range for weekly" {
        assertSoftly(WEEKLY.toDateRange(monday)) {
            start shouldBe LocalDate.of(2023, 8, 28)
            end shouldBe LocalDate.of(2023, 9, 3)
        }
    }

    "Should compute date range for monthly" {
        assertSoftly(MONTHLY.toDateRange(monday)) {
            start shouldBe LocalDate.of(2023, 8, 1)
            end shouldBe LocalDate.of(2023, 8, 31)
        }
    }

    // todo monthly with monday first of month

    "Should load cron.yaml" {
        assertSoftly(EmailFrequency.SCHEDULE) {
            shouldContain(WEEKLY, "every monday 9:00")
            shouldContain(MONTHLY, "1 of month 9:00")
            shouldContain(NONE, null)
        }
    }
})
