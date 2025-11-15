package cz.geek.spdreport.service

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldHaveSize
import java.time.LocalDate

class CalendarServiceTest : FreeSpec({
    "Should load calendar" {
        val service = CalendarService()
        val events = service.load(javaClass.getResource("/schedule.ics")!!, start = LocalDate.of(2023, 9, 1),
            end = LocalDate.of(2023, 9, 30))
        events.shouldHaveSize(1)
    }
})