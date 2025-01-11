package cz.geek.spdreport.service

import cz.geek.spdreport.model.ReportData
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.datatest.withData
import net.fortuna.ical4j.data.ParserException
import java.net.URL
import java.time.LocalDate
import java.time.LocalTime

class ReportServiceTest : FreeSpec({

    val service = ReportService(URL("http://foo"))

    "Should create report" {
        val data = ReportData(
            name = "James",
            number = "007",
            start = LocalDate.of(2023, 9, 1),
            end = LocalDate.of(2023, 9, 30)
        )
        val list = service.create(read("/schedule.ics"), data, emptySet())
        list shouldHaveSize 12

        assertSoftly(list[0]) {
            date shouldBe LocalDate.parse("2023-09-18")
            start shouldBe LocalTime.of(17, 0)
            end shouldBe LocalTime.of(0, 0)
            name shouldBe "James"
            number shouldBe "007"
        }
        list.subList(1, 9)
            .filterIndexed { index, _ -> index % 2 == 0 }
            .forAll {
                assertSoftly(it) {
                    start shouldBe LocalTime.of(0, 0)
                    end shouldBe LocalTime.of(9, 0)
                    name shouldBe "James"
                    number shouldBe "007"
                }
            }
        list.subList(1, 9)
            .filterIndexed { index, _ -> index % 2 == 1 }
            .forAll {
                assertSoftly(it) {
                    start shouldBe LocalTime.of(17, 0)
                    end shouldBe LocalTime.of(0, 0)
                    name shouldBe "James"
                    number shouldBe "007"
                }
            }
        assertSoftly(list[9]) {
            date shouldBe LocalDate.parse("2023-09-23")
            start shouldBe LocalTime.of(0, 0)
            end shouldBe LocalTime.of(0, 0)
            name shouldBe "James"
            number shouldBe "007"
        }
        assertSoftly(list[10]) {
            date shouldBe LocalDate.parse("2023-09-24")
            start shouldBe LocalTime.of(0, 0)
            end shouldBe LocalTime.of(0, 0)
            name shouldBe "James"
            number shouldBe "007"
        }
        assertSoftly(list[11]) {
            date shouldBe LocalDate.parse("2023-09-25")
            start shouldBe LocalTime.of(0, 0)
            end shouldBe LocalTime.of(9, 0)
            name shouldBe "James"
            number shouldBe "007"
        }
    }

    "Should create report starting in :30 and ending at midnight" {
        val data = ReportData(
            name = "James",
            number = "007",
            start = LocalDate.of(2023, 8, 1),
            end = LocalDate.of(2023, 8, 20)
        )
        val list = service.create(read("/jp.ics"), data, emptySet())
        list shouldHaveSize 9

        assertSoftly(list.first()) {
            date shouldBe LocalDate.of(2023, 8, 7)
            start shouldBe LocalTime.of(17, 0)
            end shouldBe LocalTime.of(0, 0)
            name shouldBe "James"
            number shouldBe "007"
        }

        assertSoftly(list.last()) {
            date shouldBe LocalDate.of(2023, 8, 11)
            start shouldBe LocalTime.of(17, 0)
            end shouldBe LocalTime.of(0, 0)
            name shouldBe "James"
            number shouldBe "007"
        }

    }

    "Should create report for 1,5 hour event" {
        val data = ReportData(
            name = "James",
            number = "007",
            start = LocalDate.of(2023, 8, 28),
            end = LocalDate.of(2023, 8, 30)
        )
        val list = service.create(read("/jp.ics"), data, emptySet())
        list shouldHaveSize 1

        assertSoftly(list.first()) {
            date shouldBe LocalDate.of(2023, 8, 29)
            start shouldBe LocalTime.of(18, 30)
            end shouldBe LocalTime.of(19, 0)
            name shouldBe "James"
            number shouldBe "007"
        }
    }

    "Should create list with holidays" - {
        withData(
            setOf<LocalDate>() to LocalTime.of(17, 0),
            setOf(LocalDate.of(2023, 5, 1)) to LocalTime.of(9, 0),
        ) { (holidays, startTime) ->
            val data = ReportData(
                name = "James",
                number = "007",
                start = LocalDate.of(2023, 5, 1),
                end = LocalDate.of(2023, 5, 2)
            )
            val list = service.create(read("/schedule.ics"), data, holidays)
            list shouldHaveAtLeastSize 1
            assertSoftly(list[0]) {
                date shouldBe data.start
                start shouldBe startTime
                end shouldBe LocalTime.of(0, 0)
            }
        }
    }

    "Should fail parsing" {
        val data = ReportData(
            name = "James",
            number = "007",
            start = LocalDate.of(2023, 8, 1),
            end = LocalDate.of(2023, 8, 20)
        )
        shouldThrow<ParserException> {
            service.create("foo".toByteArray(), data, emptySet())
        }
    }
})

private fun read(resource: String) = requireNotNull(ReportServiceTest::class.java.getResource(resource)).readBytes()
