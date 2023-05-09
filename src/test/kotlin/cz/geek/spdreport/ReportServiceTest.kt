package cz.geek.spdreport

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldMatchEach
import io.kotest.matchers.shouldBe
import java.lang.invoke.MethodHandles
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ReportServiceTest : FreeSpec({

    val service = ReportService()

    "Should create report" {
        val data = ReportData(
            name = "James",
            number = "007",
            start = LocalDate.of(2023, 9, 1),
            end = LocalDate.of(2023, 9, 30)
        )
        val list = service.create(read(), data)
        list shouldHaveSize 8

        assertSoftly(list[0]) {
            date shouldBe LocalDate.parse("2023-09-18")
            start shouldBe LocalTime.of(9, 0)
            end shouldBe LocalTime.of(23, 59)
            name shouldBe "James"
            number shouldBe "007"
        }
        list.subList(1, 7).forAll {
            assertSoftly(it) {
                start shouldBe LocalTime.of(0, 0)
                end shouldBe LocalTime.of(23, 59)
                name shouldBe "James"
                number shouldBe "007"
            }
        }
        assertSoftly(list[7]) {
            date shouldBe LocalDate.parse("2023-09-25")
            start shouldBe LocalTime.of(0, 0)
            end shouldBe LocalTime.of(9, 0)
            name shouldBe "James"
            number shouldBe "007"
        }
    }

})

private fun read() = requireNotNull(ReportServiceTest::class.java.getResource("/schedule.ics")).readBytes()
